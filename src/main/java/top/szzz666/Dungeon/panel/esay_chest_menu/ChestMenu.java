package top.szzz666.Dungeon.panel.esay_chest_menu;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.RemoveEntityPacket;
import lombok.Getter;
import lombok.Setter;
import top.szzz666.Dungeon.panel.esay_chest_menu.lib.ChestFakeInventory;


import java.util.*;

import static top.szzz666.Dungeon.tools.taskUtil.Delayed;


public class ChestMenu extends ChestFakeInventory {
    private final List<Button> buttons = new ArrayList<>();
    long id;
    @Getter
    @Setter
    private Runnable close;

    public ChestMenu(String title) {
        super(InventoryType.CHEST, null, title);
        this.setName(title);
    }

    public ChestMenu(String title, boolean autoClose) {
        super(InventoryType.CHEST, null, title);
        this.setName(title);
        super.setAutoClose(autoClose);
    }

    public ChestMenu(String title, boolean autoClose, boolean async) {
        super(InventoryType.CHEST, null, title);
        this.setName(title);
        super.setAutoClose(autoClose);
        super.setAsync(async);
    }

    public void add(int slot, Item item, Runnable runnable) {
        this.buttons.add(new Button(slot, item, runnable));
    }

    public void show(Player player) {
        this.id = Entity.entityCount++;
        Map<Integer, Item> itemMap = new LinkedHashMap<>();
        HashMap<Integer, Runnable> rs = new HashMap<>();
        List<Integer> slots = new ArrayList<>();
        for (Button button : buttons) {
            itemMap.put(button.slot, button.item);
            rs.put(button.slot, button.callback);
            slots.add(button.slot);
        }
        this.setContents(itemMap);
        this.setRs(rs);
        this.setSlots(slots);
        this.setPlayer(player);
//        player.removeAllWindows();
        Delayed(() -> player.addWindow(this), 10, true);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public void onSlotChange(int index, Item before, boolean send) {
        super.onSlotChange(index, before, send);
    }

    @Override
    public void onOpen(Player who) {
        super.onOpen(who);
        ContainerOpenPacket pk = new ContainerOpenPacket();
        pk.windowId = who.getWindowId(this);
        pk.entityId = id;
        pk.type = InventoryType.CHEST.getNetworkType();
        who.dataPacket(pk);
    }

    @Override
    public void onClose(Player who) {
        RemoveEntityPacket pk = new RemoveEntityPacket();
        pk.eid = id;
        who.dataPacket(pk);
        super.onClose(who);
        if (close != null) {
            close.run();
        }
    }

    static class Button {
        public int slot;
        public Item item;
        public Runnable callback;

        public Button(int slot, Item item, Runnable runnable) {
            this.item = item;
            this.callback = runnable;
            this.slot = slot;
        }
    }
}
