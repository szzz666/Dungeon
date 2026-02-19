package top.szzz666.Dungeon.panel;

import cn.nukkit.Player;
import cn.nukkit.inventory.PlayerInventory;
import top.szzz666.Dungeon.panel.esay_chest_menu.BigChestMenu;

import static top.szzz666.Dungeon.form.MyForm.equipUpgrade;


public class MyChestMenu {
    public static void equipmentSelect(Player player) {
        BigChestMenu menu = new BigChestMenu("选择要升级的装备", true, true);
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            int finalI = i;
            menu.add(i, inventory.getItem(i), () -> equipUpgrade(player, finalI));
        }
        menu.show(player);
    }


}
