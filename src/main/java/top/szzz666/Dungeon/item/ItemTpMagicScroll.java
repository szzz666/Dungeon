package top.szzz666.Dungeon.item;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.level.Position;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;
import cn.nukkit.scheduler.TaskHandler;
import top.szzz666.Dungeon.entity.PermanentPlayerData;
import top.szzz666.Dungeon.tools.TaskData;

import java.util.concurrent.ConcurrentHashMap;

import static top.szzz666.Dungeon.DungeonMain.*;
import static top.szzz666.Dungeon.event.Listeners.allowTpPlayers;
import static top.szzz666.Dungeon.form.MyForm.enterDungeon;
import static top.szzz666.Dungeon.form.easy_form.Modal.confirmModal;
import static top.szzz666.Dungeon.tools.pluginUtil.*;
import static top.szzz666.Dungeon.tools.taskUtil.Delayed;
import static top.szzz666.Dungeon.tools.taskUtil.Repeating;

public class ItemTpMagicScroll extends ItemCustom {
    public static String TMS_ID = "s_rpg:tp_magic_scroll";
    public static int tick = 200;
    public static ConcurrentHashMap<Player, TaskData> counter = new ConcurrentHashMap<>();

    public ItemTpMagicScroll() {
        super(TMS_ID, "§r§e传送魔法卷轴", "tp_magic_scroll");
    }


    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition
                .simpleBuilder(this, CreativeItemCategory.ITEMS)
                .allowOffHand(false)
                .handEquipped(false)
                .canDestroyInCreative(true)
                .foil(true).build();
    }


    @Override
    public int getMaxStackSize() {
        return 1;
    }


    @Override
    public boolean isSword() {
        return false;
    }

    public static void use(Player player, Position pos) {
        if (counter.get(player) != null && counter.get(player).inProgress) {
            return;
        }
        PermanentPlayerData pd = permanentPlayerDataMap.get(player);
        if (pos == null && !pd.inDungeon) {
            enterDungeon(player);
            return;
        }
        if (removePlayerItem(player, Item.fromString(TMS_ID), 1)) {
            counter.put(player, new TaskData(1, Repeating(() -> {
                TaskData taskData = counter.get(player);
                int current = taskData.current;
                TaskHandler taskHandler = taskData.taskHandler;
                if (taskData.inProgress) {
                    taskData.current = current + 1;
                    player.sendActionBar("使用§r§e传送魔法卷轴§r中 " + addPar(current, tick, 50, false));
                }
                if (current > tick) {
                    taskData.inProgress = false;
                    taskHandler.cancel();
                }

            }, 1, false)));
            Delayed(() -> MagicScrollTp(player, pos, pd), tick, true);
        }
    }

    public static void use1(Player player, Position pos) {
        if (counter.get(player) != null) {
            return;
        }
        PermanentPlayerData pd = permanentPlayerDataMap.get(player);
        if (pos == null && !pd.inDungeon) {
            enterDungeon(player);
            return;
        }
        if (removePlayerItem(player, Item.fromString(TMS_ID), 1)) {
            counter.put(player, new TaskData());
            int current = 0;
            while (current < tick) {
                player.sendActionBar("使用§r§e传送魔法卷轴§r中 " + addPar(current, tick, 50, false));
                current++;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            counter.remove(player);
            // 玩家已下线，直接返回
            if (!player.isOnline() || player.isClosed()) {
                return;
            }
            if (!player.isAlive()) {
                return;
            }
            MagicScrollTp(player, pos, pd);
        }
    }

    public static void MagicScrollTp(Player player, Position pos, PermanentPlayerData pd) {
        allowTpPlayers.add(player);
        if (pd.inDungeon) {
            if (player.getSpawn().getLevel().getName().equals(ec.getString("地下城世界"))) {
                Position defaultSpawn = Server.getInstance().getDefaultLevel().getSpawnLocation();
                player.teleport(defaultSpawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
            } else {
                player.teleport(player.getSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
            pd.inDungeon = false;
            nkServer.broadcastMessage(String.format("§e玩家 %s 离开了地下城", player.getName()));
        } else if (pos != null) {
            player.teleport(pos, PlayerTeleportEvent.TeleportCause.PLUGIN);
            pd.inDungeon = true;
            nkServer.broadcastMessage(String.format("§e玩家 %s 进入了地下城", player.getName()));
        }
        allowTpPlayers.remove(player);
        pd.save();
    }
}
