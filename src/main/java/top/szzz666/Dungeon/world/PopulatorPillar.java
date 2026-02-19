package top.szzz666.Dungeon.world;


import cn.nukkit.block.BlockID;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;




public class PopulatorPillar extends Populator {
    @Override
    public void populate(final ChunkManager level, final int chunkX, final int chunkZ, final NukkitRandom random, final FullChunk chunk) {
        // 生成概率
        if ((double) random.nextRange(0, 100) / 100 < 0.1) {
            final int sourceX = chunkX << 4;
            final int sourceZ = chunkZ << 4;

            final int x = sourceX + random.nextBoundedInt(16) + 8;
            final int z = sourceZ + random.nextBoundedInt(16) + 8;

            // 固定柱子高度
            final int height = 64;
            // 柱子半径
            final int radius = random.nextRange(3, 6);
            // 生成柱子（只替换空气方块）
            for (int dy = 1; dy < height; dy++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        // 检查是否在圆形范围内
                        if (dx * dx + dz * dz <= radius * radius) {
                            int tx = x + dx;
                            int tz = z + dz;
                            // 只替换空气方块
                            if (level.getBlockIdAt(tx, dy, tz) != BlockID.STONE) {
                                // 填充柱子
                                if ((double) random.nextRange(0, 100) / 100 < 0.5) {
                                    level.setBlockAt(tx, dy, tz, BlockID.MOSS_STONE);
                                } else {
                                    level.setBlockAt(tx, dy, tz, BlockID.STONE_BRICK);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
