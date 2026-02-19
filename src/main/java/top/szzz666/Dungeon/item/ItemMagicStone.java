package top.szzz666.Dungeon.item;

import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;

public class ItemMagicStone extends ItemCustom {
    public ItemMagicStone() {
        super("s_rpg:magic_stone", "魔石", "magic_stone");
    }


    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition
                .simpleBuilder(this, CreativeItemCategory.ITEMS)
                .allowOffHand(false)
                .handEquipped(false)
                .canDestroyInCreative(true)
                .build();
    }


    @Override
    public int getMaxStackSize() {
        return 64;
    }


    @Override
    public boolean isSword() {
        return false;
    }
}
