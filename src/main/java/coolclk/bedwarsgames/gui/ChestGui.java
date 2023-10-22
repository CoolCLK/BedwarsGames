package coolclk.bedwarsgames.gui;

import coolclk.bedwarsgames.BedwarsGames;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChestGui implements Listener {
    public static class GuiItem {
        public interface Action {
            void run();
        }

        protected Action clickAction = () -> {

        };
        protected final Inventory inventory;
        protected int slot;
        protected ItemStack itemStack;

        public GuiItem(Inventory inventory, int slot, ItemStack itemStack) {
            this.inventory = inventory;
            this.slot = slot;
            this.itemStack = itemStack;
            this.getInventory().setItem(slot, itemStack);
        }

        public Action getClickAction() {
            return this.clickAction;
        }

        public void setClickAction(Action action) {
            this.clickAction = action;
        }

        public Inventory getInventory() {
            return this.inventory;
        }

        public void setItemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
            this.getInventory().setItem(this.getSlot(), itemStack);
        }

        public ItemStack getItemStack() {
            return this.itemStack;
        }

        public int getSlot() {
            return this.slot;
        }

        public void setSlot(int slot) {
            this.getInventory().setItem(this.slot, null);
            this.slot = slot;
            this.getInventory().setItem(slot, this.getItemStack());
        }
    }

    protected final Inventory inventory;
    protected final List<GuiItem> items = new ArrayList<>();

    public ChestGui(InventoryHolder holder, int rows, String title) {
        Bukkit.getServer().getPluginManager().registerEvents(this, BedwarsGames.getInstance());
        this.inventory = Bukkit.createInventory(holder, 9 * rows, title);
    }

    public void clearItems() {
        this.getItems().clear();
        this.getInventory().clear();
    }

    public GuiItem addItem(int slot, ItemStack itemStack) {
        GuiItem guiItem = new GuiItem(this.inventory, slot, itemStack);
        this.items.add(guiItem);
        return guiItem;
    }

    public List<GuiItem> getItems() {
        return this.items;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public boolean equalsInventory(Inventory inventory) {
        return Objects.equals(inventory, this.getInventory());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (this.equalsInventory(event.getInventory())) {
            for (ChestGui.GuiItem guiItem : this.getItems()) {
                if (guiItem.getSlot() == event.getSlot()) {
                    guiItem.getClickAction().run();
                }
            }
            event.setCancelled(true);
        }
    }
}
