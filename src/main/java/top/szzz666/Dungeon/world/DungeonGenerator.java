package top.szzz666.Dungeon.world;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockStone;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.level.biome.BiomeSelector;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.level.generator.object.ore.OreType;
import cn.nukkit.level.generator.populator.impl.*;
import cn.nukkit.level.generator.populator.overworld.PopulatorMineshaft;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.level.generator.task.ChunkPopulationTask;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import com.google.common.collect.ImmutableList;


import java.util.HashMap;
import java.util.List;
import java.util.Map;




public class DungeonGenerator extends Generator {
    @Override
    public void generateChunk(int chunkX, int chunkZ) {
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        BaseFullChunk chunk = level.getChunk(chunkX, chunkZ);
        FastNoiseLite noise = new FastNoiseLite((int) seed);
        noise.SetNoiseType(FastNoiseLite.NoiseType.Value);
        noise.SetFrequency(0.05f);
        FastNoiseLite noisel = new FastNoiseLite((int) seed);
        noisel.SetNoiseType(FastNoiseLite.NoiseType.Value);
        noisel.SetFrequency(0.05f);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {// 设置群系
                Biome biome = selector.pickBiome(baseX | x, baseZ | z);
                chunk.setBiome(x, z, biome);
                int biomeId = biome.getId();
                // 生成底部地形（地面）
                List<Integer> biomeIds1 = List.of(17, 18, 19, 22, 28, 31, 33);
                List<Integer> biomeIds2 = List.of(3, 13, 20, 25, 34);
                int groundHeight;
                if (biomeIds1.contains(biomeId)) {
                    noise.SetFrequency(0.05f);
                    float noiseValue = noise.GetNoise(x + chunk.getX() * 16, z + chunk.getZ() * 16);
                    groundHeight = 1 + (int) ((noiseValue + 1) * 14.5);
                } else if (biomeIds2.contains(biomeId)) {
                    noise.SetFrequency(0.1f);
                    float noiseValue = noise.GetNoise(x + chunk.getX() * 16, z + chunk.getZ() * 16);
                    groundHeight = 1 + (int) ((noiseValue + 1) * 14.5);
                } else {
                    noise.SetFrequency(0.02f);
                    float noiseValue = noise.GetNoise(x + chunk.getX() * 16, z + chunk.getZ() * 16);
                    groundHeight = 1 + (int) ((noiseValue + 1) * 14.5);
                }
                // 生成地面

                int blockId1 = BlockID.STONE;
                int blockId2 = BlockID.STONE;
                if (1 == biomeId || biomeId == 3) {
                    biomeId = BlockID.MOSS_BLOCK;
                }
                if (4 <= biomeId && biomeId <= 6) {
                    blockId1 = BlockID.GRASS_BLOCK;
                }
                if (biomeId == 2 || biomeId == 16 || biomeId == 17) {
                    blockId1 = BlockID.SAND;
                    blockId2 = BlockID.SANDSTONE;
                }
                if (biomeId == 10 || biomeId == 11) {
                    blockId1 = BlockID.PACKED_ICE;
                }
                if (biomeId == 12 || biomeId == 13) {
                    blockId1 = BlockID.SNOW_BLOCK;
                }
                for (int y = 1; y <= groundHeight; y++) {
                    if (y == groundHeight) {
                        chunk.setBlock(x, y, z, blockId1);
                        if (blockId1 == BlockID.GRASS_BLOCK) {
                            chunk.setBlock(x, y + 1, z,
                                    (double) nukkitRandom.nextRange(0, 100) / 100 < 0.5 ? BlockID.TALL_GRASS : BlockID.AIR);
                        }
                    } else {
                        chunk.setBlock(x, y, z, blockId2);
                    }
                }


                // 生成顶部地形（天花板）
                float noiseValue = noisel.GetNoise(x + 1000 + chunk.getX() * 16, z + 1000 + chunk.getZ() * 16);
                int ceilingHeight = 32 + (int) ((noiseValue + 1) * 15.5);
                // 生成天花板
                for (int y = ceilingHeight; y < 64; y++) {
                    chunk.setBlock(x, y, z, Block.STONE);
                }
            }
        }
        // 生成基岩层
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.setBlock(x, 0, z, Block.BEDROCK);
                chunk.setBlock(x, 64, z, Block.BEDROCK);
            }
        }
    }

    @Override
    public void populateChunk(int chunkX, int chunkZ) {
        this.nukkitRandom.setSeed(0xdeadbeef ^ (chunkX << 8) ^ chunkZ ^ this.level.getSeed());
        for (Populator populator : this.populators) {
            populator.populate(this.level, chunkX, chunkZ, this.nukkitRandom, level.getChunk(chunkX, chunkZ));
        }
        Biome biome = EnumBiome.getBiome(level.getChunk(chunkX, chunkZ).getBiomeId(7, 7));
        biome.populateChunk(this.level, chunkX, chunkZ, this.nukkitRandom);

        populateStructure(chunkX, chunkZ);
    }


    @Override
    public void populateStructure(final int chunkX, final int chunkZ) {
        final BaseFullChunk chunk = level.getChunk(chunkX, chunkZ);
        for (final Populator populator : structurePopulators) {
            Server.getInstance().computeThreadPool.submit(new ChunkPopulationTask(level, chunk, populator));
        }
    }

    private List<Populator> populators = ImmutableList.of(
            new PopulatorOre(STONE, new OreType[]{
                    new OreType(Block.get(BlockID.COAL_ORE), 20, 17, 0, 63),
                    new OreType(Block.get(BlockID.COPPER_ORE), 17, 9, 0, 63),
                    new OreType(Block.get(BlockID.IRON_ORE), 20, 9, 0, 63),
                    new OreType(Block.get(BlockID.REDSTONE_ORE), 8, 8, 0, 16),
                    new OreType(Block.get(BlockID.LAPIS_ORE), 1, 7, 0, 30),
                    new OreType(Block.get(BlockID.GOLD_ORE), 2, 9, 0, 32),
                    new OreType(Block.get(BlockID.DIAMOND_ORE), 1, 8, 0, 16),
                    new OreType(Block.get(BlockID.DIRT), 10, 33, 40, 63),
                    new OreType(Block.get(BlockID.DIRT), 5, 100, 10, 32),
                    new OreType(Block.get(BlockID.COBBLESTONE), 10, 100, 5, 32),
                    new OreType(Block.get(BlockID.MOSSY_STONE), 10, 50, 5, 32),
                    new OreType(Block.get(BlockID.GRAVEL), 8, 33, 0, 63),
                    new OreType(Block.get(BlockID.STONE, BlockStone.GRANITE), 10, 33, 0, 63),
                    new OreType(Block.get(BlockID.STONE, BlockStone.DIORITE), 10, 33, 0, 63),
                    new OreType(Block.get(BlockID.STONE, BlockStone.ANDESITE), 10, 33, 0, 63),
                    new OreType(Block.get(BlockID.DEEPSLATE), 20, 33, 0, 8)
            }),
            new PopulatorSpring(BlockID.WATER, BlockID.STONE, 15, 8, 32),
            new PopulatorSpring(BlockID.LAVA, BlockID.STONE, 10, 16, 32)
    );
    private List<Populator> structurePopulators = ImmutableList.of(
            new PopulatorMineshaft(),
            new PopulatorDungeon(),
            new PopulatorPillar()
    );

    private ChunkManager level;
    private long seed;
    private NukkitRandom nukkitRandom;

    private BiomeSelector selector;
    private static final float[] biomeWeights = new float[25];

    static {
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                biomeWeights[i + 2 + (j + 2) * 5] = (float) (10.0F / Math.sqrt((float) (i * i + j * j) + 0.2F));
            }
        }
    }

    public int getId() {
        return TYPE_INFINITE;
    }

    public ChunkManager getChunkManager() {
        return this.level;
    }

    public Map<String, Object> getSettings() {
        return new HashMap<>();
    }

    public String getName() {
        return "dungeon";
    }

    public void init(ChunkManager level, NukkitRandom random) {
        this.level = level;
        this.seed = level.getSeed();
        this.nukkitRandom = random;
        this.nukkitRandom.setSeed(this.level.getSeed());
        this.nukkitRandom.setSeed(this.level.getSeed());
        this.selector = new BiomeSelector(this.nukkitRandom);
    }

    public Vector3 getSpawn() {
        return new Vector3(0.5, 65.0, 0.5);
    }

    public DungeonGenerator() {
        this(new HashMap<>());
    }

    public DungeonGenerator(Map<String, Object> options) {
    }


}
