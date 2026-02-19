package top.szzz666.Dungeon.event;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityArmorChangeEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.level.Position;
import top.szzz666.Dungeon.entity.PermanentPlayerData;
import top.szzz666.Dungeon.entity.Team;
import top.szzz666.Dungeon.item.ItemTpMagicScroll;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static top.szzz666.Dungeon.DungeonMain.*;
import static top.szzz666.Dungeon.entity.Team.getPlayerHasTeam;
import static top.szzz666.Dungeon.entity.Team.isSameTeam;
import static top.szzz666.Dungeon.item.ItemTpMagicScroll.TMS_ID;
import static top.szzz666.Dungeon.tools.pluginUtil.*;
import static top.szzz666.Dungeon.tools.taskUtil.Async;
import static top.szzz666.Dungeon.tools.taskUtil.Delayed;


public class Listeners implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Async(() -> {
            Player player = event.getPlayer();
            permanentPlayerDataMap.put(player, new PermanentPlayerData(player));
            PermanentPlayerData pd = permanentPlayerDataMap.get(player);
            if (pd == null){
                return;
            }
            setEntityLevel(player, pd.level);
        });
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Async(() -> {
            Player player = event.getPlayer();
            Team playerTeam = getPlayerHasTeam(player);
            if (playerTeam != null) {
                if (playerTeam.isLeader(player)) {
                    playerTeam.disband();
                } else {
                    playerTeam.removeMember(player);
                }
            }
            PermanentPlayerData pd = permanentPlayerDataMap.get(player);
            if (pd == null) {
                return;
            }
            pd.save();
            permanentPlayerDataMap.remove(player);
        });
    }

    public static List<Player> allowTpPlayers = new CopyOnWriteArrayList<>();

    @EventHandler
    public void onPlayerTp(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (allowTpPlayers.contains(player) ||
                event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL ||
                event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            return;
        }
        String from = event.getFrom().level.getName();
        String to = event.getTo().level.getName();
        String levelName = ec.getString("地下城世界");
        if ((from.equals(levelName) || to.equals(levelName)) && !player.isCreative()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Delayed(() -> ArmorLevel(player), 10, true);
        if (event.isFirstSpawn()) {
            return;
        }
        if (event.getRespawnPosition().getLevel().getName().equals(ec.getString("地下城世界"))) {
            Position defaultSpawn = Server.getInstance().getDefaultLevel().getSpawnLocation();
            event.setRespawnPosition(defaultSpawn);
            player.setSpawn(defaultSpawn);
        }

    }


    @EventHandler
    public void onArmorChange(EntityArmorChangeEvent event) {
        Async(() -> {
            if (event.getEntity() instanceof Player player) {
                ArmorLevel(player);
            }
        });

    }

    private void ArmorLevel(Player player) {
        PlayerInventory inventory = player.getInventory();
        if (inventory == null) {
            return;
        }
        Item[] armor = inventory.getArmorContents();
        int bonusHealth = 0;
        for (Item item : armor) {
            int itemLevel = getItemLevel(item);
            bonusHealth += itemLevel * ec.getInt("盔甲每级提升血量");
        }
        player.setMaxHealth(20 + bonusHealth);
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        PermanentPlayerData pd = permanentPlayerDataMap.get(player);
        if (pd == null){
            return;
        }
        if (pd.inDungeon && !player.isCreative()) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Async(() -> {
            Player player = event.getPlayer();
            PermanentPlayerData pd = permanentPlayerDataMap.get(player);
            if (pd == null){
                return;
            }
            if (pd.autoSprint) {
                if (!player.isSneaking() && !player.isSprinting()) {
                    player.setSprinting(true);
                }
            }
        });
    }

    @EventHandler
    public void playerDamage(EntityDamageByEntityEvent event) {
        //原伤害
        float damage = event.getDamage();
        //受到伤害者
        Entity entity = event.getEntity();
        //伤害者
        Entity damager = event.getDamager();
        if (damager instanceof Player player) {
            Item itemInHand = player.getInventory().getItemInHand();
            damage = (float) (getItemLevel(itemInHand) * ec.getDouble("剑每级提升攻击") + damage);
        }
        if (entity instanceof Player player) {
            if (damager instanceof Player player2 && isSameTeam(player, player2)) {
                player2.sendMessage("你不能攻击队友");
                event.setCancelled(true);
                return;
            }
        }
        // 等级压制
        float newDamage = damage;
        if (damager.getLevel().getName().equals(ec.getString("地下城世界"))) {
            float multiplier = (float) ec.getDouble("等级压制倍率");
            int entityLevel = getEntityLevel(entity);
            int damagerLevel = getEntityLevel(damager);
            newDamage = (damagerLevel > entityLevel)
                    ? damage * (1 + (damagerLevel - entityLevel) * multiplier)
                    : damage / (1 + (entityLevel - damagerLevel) * multiplier);
//            nkConsole("伤害者等级：" + damagerLevel + " 受害者等级：" + entityLevel + " 伤害倍率：" + multiplier + " 伤害：" + damage + " 新伤害：" + newDamage);
            event.setDamage(newDamage);
        }
        // 显示伤害
        float health = entity.getHealth() - newDamage;
        Async(() -> {
            if (damager instanceof Player player) {
                int maxHealth = entity.getMaxHealth();
                player.sendActionBar("◤ " + entity.getName() + " " + addPar(health, maxHealth, 50, false));
            }
        });
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Async(() -> {
            Entity entity = event.getEntity();
            if (entity instanceof Player player) {
                PermanentPlayerData pd = permanentPlayerDataMap.get(player);
                if (pd == null){
                    return;
                }
                if (pd.autoTotem) {
                    checkAndMoveTotem(player);
                }
            }
        });
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Item[] drops = event.getDrops().clone();
        Player player = event.getEntity();
        if (player.getLevel().getName().equals(ec.getString("地下城世界"))) {
            event.setDrops(null);
            Async(() -> {
                PermanentPlayerData pd = permanentPlayerDataMap.get(player);
                if (pd == null){
                    return;
                }
                pd.inDungeon = false;
                nkServer.broadcastMessage(String.format("§e玩家 %s 离开了地下城", player.getName()));
                pd.save();
                Position pos = player.getPosition();
                List<Item> items = List.of(drops);
                createChest(pos, items, String.format("§b玩家 %s 的掉落物", player.getName()));
            });
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        EntityDamageEvent lastDamage = entity.getLastDamageCause();
        if (lastDamage instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) lastDamage).getDamager();
            if (damager instanceof Player) {
                Item[] drops = event.getDrops();
                List<Item> dropList = new ArrayList<>(Arrays.asList(drops));
                int entityLevel = getEntityLevel(entity);
                for (Item i : dropList) {
                    int c = (int) (ec.getDouble("等级原掉落材料倍率") - 1) * entityLevel * i.count;
                    i.count = i.count + c;
                }
                Map<String, Double> dropsMap = ec.getMap("掉落材料");
                for (String key : dropsMap.keySet()) {
//                    nkConsole(key);
                    Item i = getItem(key);
                    double d = (dropsMap.get(key) * entityLevel);
                    i.count = d > 1 ? (int) d : checkProbability(d) ? 1 : 0;
                    dropList.add(i);
                }
                Item[] newDrops = dropList.toArray(new Item[0]);
//                for (int i = 0; i < newDrops.length; i++) {
//                    nkConsole(newDrops[i].toString());
//                }
                event.setDrops(newDrops);
                Async(() -> {
                    int exp = (int) ec.getDouble("等级击杀经验倍率") * entityLevel;
                    PermanentPlayerData permanentPlayerData = permanentPlayerDataMap.get(damager);
                    permanentPlayerData.exp += exp;
                    permanentPlayerData.save();
                    ((Player) damager).sendMessage("击败 " + entity.getName() + "  冒险经验 +" + exp);
                });
            }
        }
    }

//    @EventHandler
//    public void onInventoryTransaction(PlayerItemConsumeEvent  event) {
//        Async(() -> {
//            Player player = event.getPlayer();
//            player.sendMessage("§a11111");
//            PermanentPlayerData pd = permanentPlayerDataMap.get(player);
//            if (pd.autoTotem) {
//                checkAndMoveTotem(player);
//            }
//        });
//    }


//    static boolean flag = true;
//    static Vector3 pos1;
//    static Vector3 pos2;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Async(() -> {
            Player player = event.getPlayer();
            if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
                    && Objects.equals(player.getInventory().getItemInHand().getNamespaceId(), TMS_ID)) {
                ItemTpMagicScroll.use1(player, null);
            }
        });
//        Player player = event.getPlayer();
//        if (player.getInventory().getItemInHand().getId() == ItemID.WOODEN_AXE) {
//            if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
//                if (flag) {
//                    double x = event.getBlock().x;
//                    double y = event.getBlock().y;
//                    double z = event.getBlock().z;
//                    pos1 = new Vector3(x, y, z);
//                    player.sendMessage("你第一次点击了方块(" + x + "," + y + "," + z + ")");
//                } else {
//                    double x = event.getBlock().x;
//                    double y = event.getBlock().y;
//                    double z = event.getBlock().z;
//                    pos2 = new Vector3(x, y, z);
//                    player.sendMessage("你第二次点击了方块(" + x + "," + y + "," + z + ")");
//                }
//                flag = !flag;
//            }
//        }
    }


    /**
     * 检查并移动不死图腾到副手
     */
    private void checkAndMoveTotem(Player player) {
        PlayerInventory inventory = player.getInventory();
        if(inventory == null){
            return;
        }
        // 在背包中寻找不死图腾
        int totemSlot = findTotemInInventory(inventory);
        if (totemSlot != -1 && player.getOffhandInventory().getItem(0).getId() == Item.AIR) {
            // 移动图腾到副手
            Item totem = inventory.getItem(totemSlot);
            inventory.clear(totemSlot);
            player.getOffhandInventory().setItem(0, totem);
            player.sendMessage("§a自动装备不死图腾到副手");
        }
    }

    /**
     * 在背包中查找不死图腾
     */
    private int findTotemInInventory(PlayerInventory inventory) {
        for (int i = 0; i < 41; i++) {
            Item item = inventory.getItem(i);
            if (item.getId() == ItemID.TOTEM) {
                return i;
            }
        }
        return -1;
    }


}
