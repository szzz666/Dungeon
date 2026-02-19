package top.szzz666.Dungeon.tools;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.TextFormat;
import io.leego.banana.BananaUtils;
import io.leego.banana.Font;
import lombok.SneakyThrows;
import top.szzz666.Dungeon.entity.PermanentPlayerData;
import top.szzz666.Dungeon.panel.esay_chest_menu.lib.AbstractFakeInventory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static top.szzz666.Dungeon.DungeonMain.*;

public class pluginUtil {
    public static void resetInDungeon(PermanentPlayerData pd, Player player) {
        pd.inDungeon = player.getLevel().getName().equals(ec.getString("地下城世界"));
    }

    public static void multCmd(CommandSender sender, String command) {
        nkServer.getCommandMap().dispatch(sender, command);
    }

    public static void createChest(Position pos, List<Item> items, String CustomName) {
        final int size = 27;
        int size1 = items.size();
        Level level = pos.level;
        if (size1 > size) {
            Position pos0 = new Position(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ(), level);
            Position pos1 = new Position(pos.getFloorX() - 1, pos.getFloorY(), pos.getFloorZ(), level);
            level.setBlock(pos0, Block.get(BlockID.CHEST));
            level.setBlock(pos1, Block.get(BlockID.CHEST));
            CompoundTag chest = BlockEntity.getDefaultCompound(pos0, BlockEntity.CHEST);
            ListTag<CompoundTag> Items = new ListTag<>("Items");
            for (int i = 0; i < size; i++) {
                Item item = items.get(i);
                if (item != null) {
                    Items.add(i, NBTIO.putItemHelper(item, i));
                }
            }
            chest.putString("CustomName", CustomName);
            chest.putBoolean("pairlead", true);
            chest.putInt("pairx", pos.getFloorX() - 1);
            chest.putInt("pairz", pos.getFloorZ());
            chest.putList(Items);
            CompoundTag chest1 = BlockEntity.getDefaultCompound(pos1, BlockEntity.CHEST);
            ListTag<CompoundTag> Items1 = new ListTag<>("Items");
            for (int i = 0; i < size1 - size; i++) {
                Item item = items.get(i + size);
                if (item != null) {
                    Items1.add(i, NBTIO.putItemHelper(item, i));
                }
            }
            chest1.putString("CustomName", CustomName);
            chest1.putBoolean("pairlead", false);
            chest1.putInt("pairx", pos.getFloorX());
            chest1.putInt("pairz", pos.getFloorZ());
            chest1.putList(Items1);
            BlockEntity.createBlockEntity(BlockEntity.CHEST, pos0, chest);
            BlockEntity.createBlockEntity(BlockEntity.CHEST, pos1, chest1);
        } else {
            Position pos0 = new Position(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ(), level);
            level.setBlock(pos0, Block.get(BlockID.CHEST));
            CompoundTag chest = BlockEntity.getDefaultCompound(pos0, BlockEntity.CHEST);
            ListTag<CompoundTag> Items = new ListTag<>("Items");
            for (int i = 0; i < size && i < size1; i++) {
                Item item = items.get(i);
                if (item != null) {
                    Items.add(i, NBTIO.putItemHelper(item, i));
                }
            }
            chest.putString("CustomName", CustomName);
            chest.putList(Items);
            BlockEntity.createBlockEntity(BlockEntity.CHEST, pos0, chest);
        }
    }

    public static void containerCreateItems(final ListTag<CompoundTag> list) {
        double probability = ec.getDouble("物品刷新概率");
        int totalWeight = 0;
        List<HashMap<String, Object>> WMT = new ArrayList<>();
        ArrayList<HashMap<String, Object>> containerItems = ec.get("容器物品");
        for (HashMap<String, Object> map : containerItems) {
            Object weight = map.get("权重");
            WMT.add(map);
            totalWeight += (int) weight;
        }
        final CompoundTag[] tags = new CompoundTag[list.size()];
        // 遍历每个格子
        for (int i = 0; i < list.size(); i++) {
            if (checkProbability(probability)) {
                if (WMT.isEmpty() || totalWeight <= 0) {
                    return;
                }
                int randomWeight = random.nextInt(totalWeight) + 1;
                int cumulativeWeight = 0;
                for (HashMap<String, Object> ItemInfo : WMT) {
                    int weight = (int) ItemInfo.get("权重");
                    cumulativeWeight += weight;
                    if (randomWeight <= cumulativeWeight) {
                        Item item = getItem((String) ItemInfo.get("物品"));
                        if (item != null) {
                            item.setCount(getRandomIntInRange((Integer) ItemInfo.get("最小数量"), (Integer) ItemInfo.get("最大数量")));
                            tags[i] = NBTIO.putItemHelper(item, i);
                        }
                        break;
                    }
                }

            }
        }
        // 填充最终列表
        for (int i = 0; i < list.size(); i++) {
            if (tags[i] != null) {
                list.add(i, tags[i]);
            }
        }
    }

    public static Item addEnchantmentViaNBT(Item item, int enchantmentId, int level) {
        CompoundTag tag = item.getNamedTag() == null ? new CompoundTag() : item.getNamedTag();
        ListTag<CompoundTag> enchantments = tag.contains("ench") ? tag.getList("ench", CompoundTag.class) : new ListTag<>("ench");
        CompoundTag enchantmentTag = new CompoundTag().putShort("id", enchantmentId).putShort("lvl", level);
        enchantments.add(enchantmentTag);
        tag.putList(enchantments);
        item.setNamedTag(tag);
        return item;
    }

    public static void setItemLevel(Item item, int level) {
        item.setLore("§r§6Lv." + level);
        CompoundTag tag = item.getNamedTag() == null ? new CompoundTag() : item.getNamedTag();
        tag.putInt("level", level);
        item.setNamedTag(tag);
    }

    public static int getItemLevel(Item item) {
        if (item.getNamedTag() == null) {
            return 0;
        }
        return item.getNamedTag().getInt("level");
    }

    public static Random random = new Random();

    public static int getRandomIntInRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("最小值不能大于最大值");
        }
        return random.nextInt(max - min + 1) + min;
    }

    private static boolean hasEnoughSpace(Level level, int x, int y, int z) {
        // 检查3x3x2的空间是否为空
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (noAir(level.getBlock(x + dx, y, z + dz).getId()) || noAir(level.getBlock(x + dx, y + 1, z + dz).getId())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean noAir(int blockId) {
        return blockId != Block.AIR && blockId != Block.TALL_GRASS && blockId != Block.FLOWER;
    }

    public static int getY(Level level, int x, int z) {
        for (int y = 0; y < 64; y++) {
            if (level.getBlock(x, y - 1, z).isSolid()) {
                if (hasEnoughSpace(level, x, y, z)) {
                    return y;
                }
            }
        }
        return 0;
    }

    public static boolean checkProbability(double probability) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("概率必须在0.0到1.0之间");
        }
        return random.nextDouble() < probability;
    }

    public static boolean removePlayerItem(Player player, Item targetItem, int count) {
        int mss = targetItem.getMaxStackSize();
        ItemMap itemMap = getItemMapByPlayer(player);
        Integer maxCount = itemMap.get(targetItem);
        if (maxCount < count) {
            return false;
        }
        int rest = maxCount - count;
        int stack = rest / mss;
        int remain = rest % mss;
        PlayerInventory inventory = player.getInventory();
        inventory.remove(targetItem);
        for (int i = 0; i < stack; i++) {
            Item item = targetItem.clone();
            item.setCount(mss);
            inventory.addItem(item);
        }
        Item item = targetItem.clone();
        item.setCount(remain);
        inventory.addItem(item);
        return true;
    }

    public static boolean checkItemsIsComplete(Player player, HashMap<Item, Integer> items) {
        for (Item item : items.keySet()) {
            ItemMap itemMap = getItemMapByPlayer(player);
            Integer maxCount = itemMap.get(item);
            if (maxCount < items.get(item)) {
                return false;
            }
        }
        return true;
    }

    public static ItemMap getItemMapByPlayer(Player player) {
        Inventory inventory = player.getInventory();
        ItemMap itemMap = new ItemMap();
        for (int i = 0; i < inventory.getSize(); i++) {
            Item item = inventory.getItem(i);
            itemMap.put(item, item.getCount());
        }
        return itemMap;
    }

    public static String itemNBTToString(Item item) {
        try {
            CompoundTag nbt = item.getNamedTag();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            NBTIO.write(nbt, dos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    public static Item getItem(String s) {
        String[] parts = s.split(":");
        try {
            int[] nums = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                nums[i] = Integer.parseInt(parts[i]);
            }
            switch (nums.length) {
                case 1:
                    return Item.get(nums[0]);
                case 2:
                    return Item.get(nums[0], nums[1]);
                case 3:
                    return Item.get(nums[0], nums[1], nums[2]);
            }
        } catch (NumberFormatException e) {
            return Item.fromString(s);
        }
        return Item.AIR_ITEM;
    }

    public static boolean isDependencyLoaded(String pluginName) {
        PluginManager pluginManager = nkServer.getPluginManager();
        Plugin plugin = pluginManager.getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    public static void setEntityLevel(Entity entity, int level) {
        entity.namedTag.putInt("level", level);
    }


    public static int getEntityLevel(Entity entity) {
        return entity.namedTag.getInt("level");
    }

    public static boolean checkServer() {
        boolean ver = false;
        //双核心兼容
        try {
            Class<?> c = Class.forName("cn.nukkit.Nukkit");
            c.getField("NUKKIT_PM1E");
            ver = true;

        } catch (ClassNotFoundException | NoSuchFieldException ignore) {
        }
        try {
            Class<?> c = Class.forName("cn.nukkit.Nukkit");
            "Nukkit PetteriM1 Edition".equalsIgnoreCase(c.getField("NUKKIT").get(c).toString());
            ver = true;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignore) {
        }

        AbstractFakeInventory.IS_PM1E = ver;
        if (ver) {
            nkConsole("当前插件运行在: Nukkit MOT 核心上");
        } else {
            nkConsole("当前插件运行在: Nukkit 核心上");
        }
        return ver;
    }

    //Banana
    @SneakyThrows
    public static void pluginNameLineConsole() {
        lineConsole(BananaUtils.bananaify(plugin.getName(), Font.SMALL));
    }

    //将输入的字符串按行打印到控制台。
    public static void lineConsole(String s) {
        String[] lines = s.split("\n");
        for (String line : lines) {
            nkConsole(line);
        }
    }

    //使用nk插件的控制台输出
    public static void nkConsole(String msg) {
        plugin.getLogger().info(TextFormat.colorize('&', msg));
    }

    /**
     * 进度条
     *
     * @param now  当前值
     * @param max  最大值
     * @param show 显示数值
     * @return 进度条
     */
    public static String addPar(double now, double max, int length, boolean show) {
        BigDecimal rate = BigDecimal.valueOf(now / max * length);

        StringBuilder s = new StringBuilder();
        for (int i = 1; i <= length; i++) {
            if (i < rate.intValue()) {
                s.append("§a|");
            } else if (i == rate.intValue()) {
                s.append("§e§l|");
            } else {
                s.append("§r§7|");
            }
        }

        // 显示百分比数值
        if (show) {
            BigDecimal i = BigDecimal.valueOf(now / max * 100).setScale(1, RoundingMode.HALF_UP);
            s.append(" ");
            s.append(i.doubleValue());
            s.append("%");
        }

        return s.toString();
    }

    public static void nkConsole(String msg, int typeNum) {
        if (typeNum == 1) {
            plugin.getLogger().warning(TextFormat.colorize('&', msg));
        } else if (typeNum == 2) {
            plugin.getLogger().error(TextFormat.colorize('&', msg));
        } else {
            plugin.getLogger().info(TextFormat.colorize('&', msg));
        }
    }
}
