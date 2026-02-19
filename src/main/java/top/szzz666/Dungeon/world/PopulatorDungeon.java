package top.szzz666.Dungeon.world;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.generator.loot.DungeonChest;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.level.generator.task.BlockActorSpawnTask;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.plugin.InternalPlugin;

import static top.szzz666.Dungeon.tools.pluginUtil.containerCreateItems;


public class PopulatorDungeon extends Populator {

    @Override
    public void populate(final ChunkManager level, final int chunkX, final int chunkZ, final NukkitRandom random, final FullChunk chunk) {
        // 生成概率
        if ((double) random.nextRange(0, 100) / 100 < 0.05) {
            final int sourceX = chunkX << 4;
            final int sourceZ = chunkZ << 4;

            for (int chance = 0; chance < 4; ++chance) {
                final int x = sourceX +  random.nextBoundedInt(16) + 8;
                final int y = random.nextRange(12, 32);
                final int z = sourceZ +  random.nextBoundedInt(16) + 8;

                //底部不是固体方块
                if (!Block.solid[level.getBlockIdAt(x, y - 1, z)]) {
                    continue;
                }

                //最好露天
                if (level.getBlockIdAt(x, y + 3, z) != BlockID.AIR) {
                    continue;
                }

                //尺寸
                final int rx = random.nextRange(2, 8);
                final int rz = random.nextRange(2, 8);
                final int x1 = -rx;
                final int z1 = -rz;

                //墙壁
                for (int dx = x1; dx <= rx; ++dx) {
                    for (int dy = -1; dy <= 4; ++dy) {
                        for (int dz = z1; dz <= rz; ++dz) {
                            final int tx = x + dx;
                            final int ty = y + dy;
                            final int tz = z + dz;
                            if (dx != x1 && dy != -1 && dz != z1 && dx != rx && dy != 4 && dz != rz) {
                                level.setBlockAt(tx, ty, tz, BlockID.AIR);
                            } else if (Block.solid[level.getBlockIdAt(tx, ty, tz)]) {
                                if ((double) random.nextRange(0, 100) / 100 < 0.5) {
                                    level.setBlockAt(tx, ty, tz, BlockID.MOSS_STONE);
                                } else {
                                    level.setBlockAt(tx, ty, tz, BlockID.STONE_BRICK);
                                }
                            }
                        }
                    }
                }

                //箱子
                for (int i = 0; i < 3; ++i) {
                    final int tx = x + random.nextBoundedInt(5) - 2;
                    final int tz = z + random.nextBoundedInt(5) - 2;
                    if (level.getBlockIdAt(tx, y, tz) == BlockID.AIR && level.getBlockIdAt(tx, y - 1, tz) != BlockID.AIR) {
                        level.setBlockAt(tx, y, tz, BlockID.CHEST);
                        final CompoundTag chest = BlockEntity.getDefaultCompound(new Vector3(tx, y, tz), BlockEntity.CHEST);
                        final ListTag<CompoundTag> items = new ListTag<>("Items");
                        DungeonChest.get().create(items, random);//这个方法就是给箱子里放物品的，是mot自带的
                        containerCreateItems(items);
                        chest.putList(items);
                        Server.getInstance().getScheduler().scheduleTask(InternalPlugin.INSTANCE, new BlockActorSpawnTask(chunk.getProvider().getLevel(), chest));
                        break;
                    }
                }

            }
        }
    }
}
