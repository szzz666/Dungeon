package top.szzz666.Dungeon.tools;

import cn.nukkit.item.Item;

import java.util.HashMap;
import java.util.Objects;

import static top.szzz666.Dungeon.DungeonMain.NukkitType;
import static top.szzz666.Dungeon.tools.pluginUtil.itemNBTToString;


public class ItemMap extends HashMap<Item, Integer> {
    public static boolean isSameItem(Item item1, Item item2) {
        if (NukkitType) {
            return Objects.equals(item1.getNamespaceId(), item2.getNamespaceId())
                    && item1.getDamage() == item2.getDamage()
                    && Objects.equals(itemNBTToString(item1), itemNBTToString(item2));
        }
        return item1.getId() == item2.getId()
                && item1.getDamage() == item2.getDamage()
                && Objects.equals(itemNBTToString(item1), itemNBTToString(item2));
    }


    public boolean containsKey(Item key) {
        for (Item i : this.keySet()) {
            if (isSameItem(i, key)) {
                return true;
            }
        }
        return false;
    }

    public Integer get(Item key) {
        for (Item i : this.keySet()) {
            if (isSameItem(i, key)) {
                return super.get(i);
            }
        }
        return 0;
    }

    @Override
    public Integer put(Item key, Integer value) {
        if (key.getId() == Item.AIR) {
            return null;
        }
        for (Item i : this.keySet()) {
            if (isSameItem(i, key)) {
                return super.put(i, this.get(i) + value);
            }
        }
        return super.put(key, value);
    }
}
