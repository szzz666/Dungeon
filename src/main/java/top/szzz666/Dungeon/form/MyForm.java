package top.szzz666.Dungeon.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import top.szzz666.Dungeon.entity.PermanentPlayerData;
import top.szzz666.Dungeon.entity.Team;
import top.szzz666.Dungeon.form.easy_form.Custom;
import top.szzz666.Dungeon.form.easy_form.Simple;
import top.szzz666.Dungeon.item.ItemTpMagicScroll;
import top.szzz666.Dungeon.tools.ItemMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static top.szzz666.Dungeon.DungeonMain.*;
import static top.szzz666.Dungeon.entity.PermanentPlayerData.*;
import static top.szzz666.Dungeon.entity.Team.getPlayerHasTeam;
import static top.szzz666.Dungeon.form.easy_form.Modal.confirmModal;
import static top.szzz666.Dungeon.form.easy_form.Modal.tipsModal;
import static top.szzz666.Dungeon.item.ItemTpMagicScroll.TMS_ID;
import static top.szzz666.Dungeon.panel.MyChestMenu.equipmentSelect;
import static top.szzz666.Dungeon.tools.pluginUtil.*;
import static top.szzz666.Dungeon.tools.taskUtil.Async;


public class MyForm {
    public static void mainForm(Player player) {
        Simple form = new Simple("冒险", String.format("冒险等级: %d\n冒险经验: %d/%d", getEntityLevel(player), getExp(player), getExpMax(player)), true);
        form.add("进入地下城", () -> enterDungeon(player));
        form.add("冒险者升级", () -> adventurerUpgrade(player));
        form.add("装备升级", () -> equipmentSelect(player));
        form.add("排行榜", () -> player.sendMessage(getLevelRankingAsString(10)));
        form.add("设置", () -> setting(player));
        form.show(player);
    }

    private static void setting(Player player) {
        Custom custom = new Custom("设置");
        PermanentPlayerData pd = permanentPlayerDataMap.get(player);
        custom.add("自动装备不死图腾", new ElementToggle("自动装备不死图腾", pd.autoTotem));
        custom.add("自动疾跑", new ElementToggle("自动疾跑", pd.autoSprint));
        custom.setSubmit(() -> {
            pd.autoTotem = custom.getToggleRes("自动装备不死图腾");
            pd.autoSprint = custom.getToggleRes("自动疾跑");
            pd.save();
            player.sendMessage("设置已保存！");
        });
        custom.show(player);
    }

    public static void adventurerUpgrade(Player player) {
        int exp = (int) (ec.getInt("玩家升级需要经验") * Math.pow(ec.getDouble("玩家升级经验倍率"), getEntityLevel(player)));
        double money = ec.getDouble("玩家升级需要经济") * Math.pow(ec.getDouble("玩家升级经济倍率"), getEntityLevel(player));
        confirmModal(player, String.format("需要消耗%d冒险经验和%.2f财富值, 是否升级?", exp, money), () -> mainForm(player), () -> {
            if (economyAPI.myMoney(player) >= money && getExp(player) >= exp) {
                economyAPI.reduceMoney(player, money);
                permanentPlayerDataMap.get(player).exp -= exp;
                setEntityLevel(player, getEntityLevel(player) + 1);
                permanentPlayerDataMap.get(player).level += 1;
                permanentPlayerDataMap.get(player).save();
                tipsModal(player, "冒险等级已提升！", () -> mainForm(player));
            } else {
                tipsModal(player, "你没有足够的经验或财富值！", () -> mainForm(player));
            }
        });
    }

    public static void enterDungeon(Player player) {
        Team team = getPlayerHasTeam(player);
        boolean playerHasTeam = team != null;
        String content = "§c地下城对冒险者有恶意，死亡掉落，请小心！§r\n" + (playerHasTeam ? String.format("""
                队长: %s
                队员: %s""", team.leader.getName(), team.membersToString()) : "未加入队伍");
        Simple form = new Simple("进入地下城", content, true);
        form.add("邀请组队", () -> teamUp(player));
        if (!playerHasTeam) {
            form.add("申请入队", () -> joinTeam(player));
            form.add("开始冒险", () -> startDungeon(player, team, playerHasTeam));
        }
        if (playerHasTeam) {
            if (team.isLeader(player)) {
                form.add("开始冒险", () -> startDungeon(player, team, playerHasTeam));
                form.add("踢出队员", () -> quitTeam(player, team));
                form.add("解散队伍", () -> confirmModal(player, "是否解散队伍？", team::disband));
            } else {
                form.add(team.isReady(player) ? "取消准备" : "准备", () -> confirmModal(player, "进入地下城需携带2个及以上的传送魔法卷轴，每次传送都会消耗一个，是否准备？", () -> team.ready(player)));
                form.add("退出队伍", () -> confirmModal(player, "是否退出队伍？", () -> team.removeMember(player)));
            }
        }
        form.show(player);
    }

    public static void startDungeon(Player player, Team team, boolean playerHasTeam) {
        ItemMap itemMap = getItemMapByPlayer(player);
        if (!itemMap.containsKey(Item.fromString(TMS_ID))) {
            player.sendMessage("§c你没有传送魔法卷轴，无法进入地下城。");
            return;
        }
        Level level = nkServer.getLevelByName(ec.getString("地下城世界"));
        if (level == null) {
            player.sendMessage("世界不存在");
            return;
        }
        int r = ec.getInt("玩家刷新最大范围");
        int x = 0;
        int y = 0;
        int z = 0;
        int count = 64;
        while (y == 0 && count > 0) {
            x = getRandomIntInRange(-r, r);
            z = getRandomIntInRange(-r, r);
            y = getY(level, x, z);
            count--;
        }
        if (y == 0) {
            y = 32;
        }
        Position pos = new Position(x, y, z, level);
        if (playerHasTeam) {
            if (!team.isAllReady()) {
                player.sendMessage("队伍未准备好，无法进入地下城！");
                return;
            }
            for (Player member : team.getMembers()) {
                Async(() -> ItemTpMagicScroll.use1(member, pos));
            }
            Async(() -> ItemTpMagicScroll.use1(player, pos));
        } else {
            Async(() -> ItemTpMagicScroll.use1(player, pos));
        }
    }


    public static void joinTeam(Player player) {
        Simple form = new Simple("申请入队", "选择队伍", true);
        for (String team : teams.keySet()) {
            form.add(team, () -> {
                String clickedText = form.getClickedText();
                Player captainPlayer = nkServer.getPlayer(clickedText);
                confirmModal(captainPlayer, String.format("玩家 %s 申请加入你的队伍，是否确认同意？", player.getName()), () ->
                        teams.get(captainPlayer.getName()).addMember(player));
            });
        }
        form.show(player);
    }

    public static void quitTeam(Player player, Team team) {
        Simple form = new Simple("踢出队员", "选择队员", true);
        for (Player member : team.members.keySet()) {
            form.add(member.getName(), () -> {
                confirmModal(player, "确认踢出该队员？", () -> team.removeMember(member));
            });
        }
        form.show(player);
    }

    public static void teamUp(Player player) {
        Custom form = new Custom("邀请组队", true);
        Map<UUID, Player> onlinePlayers = nkServer.getOnlinePlayers();
        for (Player p : onlinePlayers.values()) {
            PermanentPlayerData pd = permanentPlayerDataMap.get(p);
            if (pd != null) {
                if (!pd.inDungeon && !p.getName().equals(player.getName())) {
                    String name = p.getName();
                    form.add(name, new ElementToggle(name, false));
                }
            }
        }
        form.setSubmit(() -> {
            if (teams.get(player.getName()) == null) {
                Team team = new Team(player);
                teams.put(player.getName(), team);
            }
            List<String> elements = form.getElements();
            elements.remove(player.getName());
            for (String element : elements) {
                Player invitedPlayer = nkServer.getPlayer(element);
                boolean hasMember = teams.get(player.getName()).hasMember(invitedPlayer);
                if (form.getRes(element)) {
                    if (!hasMember) {
                        confirmModal(invitedPlayer, String.format("玩家 %s 邀请你共同进入地下城，是否确认同意？", player.getName()),
                                () -> teams.get(player.getName()).addMember(invitedPlayer));
                    }
                }
            }
            player.sendMessage("已发送组队邀请");
        });
        form.show(player);
    }

    public static void equipUpgrade(Player player, int index) {
        Item equipItem = player.getInventory().getItem(index);
        if (!equipItem.isSword() && !equipItem.isArmor()) {
            tipsModal(player, "该物品不是装备，无法升级！", () -> mainForm(player));
            return;
        }
        Map<String, Integer> itemMap = ec.getMap("装备升级材料");

        HashMap<Item, Integer> itemAndNum = new HashMap<>();
        for (String itemStr : itemMap.keySet()) {
            Item item = getItem(itemStr);
            itemAndNum.put(item, (int) (itemMap.get(itemStr) * Math.pow(ec.getDouble("装备升级材料倍率"), getItemLevel(equipItem))));
        }
        StringBuilder sb = new StringBuilder();
        for (Item item : itemAndNum.keySet()) {
            sb.append(" -").append(item.getName()).append(" * ").append(itemAndNum.get(item)).append("\n");
        }
        double money = ec.getDouble("装备升级需要经济") * Math.pow(ec.getDouble("装备升级经济倍率"), getItemLevel(equipItem));
        confirmModal(player, String.format("是否升级? 需要消耗%.2f财富值和材料:\n%s", money, sb), () -> mainForm(player), () -> {
            if (economyAPI.myMoney(player) >= money && checkItemsIsComplete(player, itemAndNum)) {
                economyAPI.reduceMoney(player, money);
                for (Item item : itemAndNum.keySet()) {
                    removePlayerItem(player, item, itemAndNum.get(item));
                }
                setItemLevel(equipItem, getItemLevel(equipItem) + 1);
                player.getInventory().setItem(index, equipItem);
                tipsModal(player, "装备等级已提升！", () -> mainForm(player));
            } else {
                tipsModal(player, "你没有足够的材料或财富值！", () -> mainForm(player));
            }
        });
    }
}


