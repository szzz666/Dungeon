package top.szzz666.Dungeon.biology;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static top.szzz666.Dungeon.DungeonMain.ec;
import static top.szzz666.Dungeon.DungeonMain.nkServer;
import static top.szzz666.Dungeon.tools.pluginUtil.*;
import static top.szzz666.Dungeon.tools.pluginUtil.getRandomIntInRange;
import static top.szzz666.Dungeon.tools.taskUtil.Repeating;

public class Refresh {
    private static void spawnMobsInChunk(Level level, Player player) {
        int safeZoneRadius = ec.getInt("安全区域半径");
        int brushMonsterRange = ec.getInt("刷怪范围");
        int count = 64;
        int y = 0;
        int x = 0;
        int z = 0;
        while (y == 0) {
            x = player.getFloorX() + getRandomIntInRange(-brushMonsterRange, brushMonsterRange);
            z = player.getFloorZ() + getRandomIntInRange(-brushMonsterRange, brushMonsterRange);
            if (isInRange(player, x, z, safeZoneRadius * safeZoneRadius)) {
                continue;
            }
            y = getY(level, x, z);
            count--;
            if (count < 0) {
                return;
            }
        }
        int lv = 0;
        while (lv == 0) {
            int mobLv = ec.getInt("怪物等级浮动");
            int playerLv = getEntityLevel(player);
            int minLv = playerLv - mobLv;
            int maxLv = playerLv + mobLv;
            lv = getRandomIntInRange(minLv, maxLv);
            if (lv < ec.getInt("怪物最低等级") || lv > ec.getInt("怪物最高等级")) {
                lv = 0;
            }
        }
        Position pos = new Position(x, y, z, level);
        HashMap<String, Object> m = selectMobByWeight();
        Entity e;
        if (m != null) {
            e = Entity.createEntity((String) m.get("类型"), pos);
            if (e == null) {
                nkConsole("无法创建怪物: " + m.get("类型"), 1);
                return;
            }
            setEntityLevel(e, lv);
            e.setNameTag("Lv." + lv + " " + m.get("名称"));
            e.setNameTagAlwaysVisible(true);
            level.addEntity(e);
            e.spawnToAll();
        }
    }

    public static int totalWeight = 0;
    public static List<HashMap<String, Object>> WMT = new ArrayList<>();

    public static HashMap<String, Object> selectMobByWeight() {
        if (WMT.isEmpty() || totalWeight <= 0) {
            return null;
        }
        int randomWeight = random.nextInt(totalWeight) + 1;
        int cumulativeWeight = 0;
        for (HashMap<String, Object> mobInfo : WMT) {
            int weight = (int) mobInfo.get("权重");
            cumulativeWeight += weight;
            if (randomWeight <= cumulativeWeight) {
                return mobInfo;
            }
        }
        return null;
    }

    public static void StartRefresh() {
        ArrayList<HashMap<String, Object>> monsterBrushingType = ec.get("刷怪类型");
        for (HashMap<String, Object> hashMap : monsterBrushingType) {
            Object weight = hashMap.get("权重");
            WMT.add(hashMap);
            totalWeight += (int) weight;
        }
        Repeating(() -> {
            String worldName = ec.getString("地下城世界");
            Level level = nkServer.getLevelByName(worldName);
            if (level == null) {
                return;
            }
            Map<Long, Player> players = level.getPlayers();
            if (players.isEmpty()) {
                return;
            }
            for (Player player : players.values()) {
                checkAndSpawnMobsNearPlayer(player);
            }
        }, ec.getInt("刷怪检测间隔"), true);
    }

    private static void checkAndSpawnMobsNearPlayer(Player player) {
        Level level = player.getLevel();
        int centerChunkX = player.getChunkX();
        int centerChunkZ = player.getChunkZ();
        int radius = ec.getInt("刷怪检测范围");
        int chunkRadius = radius / 16;
        int totalMobs = 0;

        // 清理刷怪范围外的怪物
        int cleaningScope = ec.getInt("刷怪清理范围");
        int cleaningMaxScope = ec.getInt("刷怪清理最大范围");
        int cleanScopeSq = cleaningScope * cleaningScope;
        int cleanMaxScopeSq = cleaningMaxScope * cleaningMaxScope;
        for (Entity entity : level.getEntities()) {
            if (!(entity instanceof EntityMob)) {
                continue; // 提前跳过非怪物实体
            }
            int dx = (int) entity.x - (int) player.x;
            int dz = (int) entity.z - (int) player.z;
            int distanceSq = dx * dx + dz * dz;
            if (distanceSq <= cleanMaxScopeSq && distanceSq > cleanScopeSq) {
                entity.close();
            }
        }

        // 统计数量
        for (int x = -chunkRadius; x <= chunkRadius; x++) {
            for (int z = -chunkRadius; z <= chunkRadius; z++) {
                int chunkX = centerChunkX + x;
                int chunkZ = centerChunkZ + z;
                totalMobs = totalMobs + getEntityCountInChunk(level, chunkX, chunkZ);
//                nkConsole("刷怪数量: " + totalMobs);

            }
        }
        // 刷怪
        if (totalMobs < ec.getInt("刷怪检测数量")) {
            for (int i = 0; i < ec.getInt("刷怪检测数量") - totalMobs; i++) {
                spawnMobsInChunk(level, player);
            }
        }
    }

    public static int getEntityCountInChunk(Level level, int chunkX, int chunkZ) {
        int count = 0;
        Map<Long, Entity> chunkEntities = level.getChunkEntities(chunkX, chunkZ);
        for (Entity entity : chunkEntities.values()) {
            if (entity instanceof EntityMob) {
                count++;
            }
        }
        return count;
    }

    public static boolean isInRange(int x1, int z1, int x2, int z2, int rangeSquared) {
        int dx = x1 - x2;
        int dz = z1 - z2;
        return (dx * dx + dz * dz) <= rangeSquared;
    }

    public static boolean isInRange(Player player, int x, int z, int rangeSquared) {
        int dx = player.getFloorX() - x;
        int dz = player.getFloorZ() - z;
        return (dx * dx + dz * dz) <= rangeSquared;
    }

}
