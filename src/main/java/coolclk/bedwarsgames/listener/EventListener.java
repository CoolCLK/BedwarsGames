package coolclk.bedwarsgames.listener;

import coolclk.bedwarsgames.BedwarsGames;
import coolclk.bedwarsgames.gui.ChestGui;
import coolclk.bedwarsgames.util.BedwarsRelApi;
import io.github.bedwarsrel.events.BedwarsGameStartedEvent;
import io.github.bedwarsrel.villager.MerchantCategory;
import io.github.bedwarsrel.villager.MerchantCategoryComparator;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EventListener implements Listener {
    private final static EventListener INSTANCE = new EventListener();

    public static EventListener getInstance() {
        return INSTANCE;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        for (ChestGui chestGui : ChestGui.getAllGui()) {
            if (chestGui.equalsInventory(event.getInventory())) {
                for (ChestGui.GuiItem guiItem : chestGui.getItems()) {
                    if (guiItem.getSlot() == event.getSlot()) {
                        guiItem.getClickAction().run();
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        new ArrayList<>(ChestGui.getAllGui()).removeIf(chestGui -> chestGui.equalsInventory(event.getInventory()));
    }

    @EventHandler
    public void onBedwarsGameStarted(BedwarsGameStartedEvent event) {
        ConfigurationSection gamesConfig = BedwarsGames.getConfiguration().getConfigurationSection("games");
        if (gamesConfig.getKeys(false).contains(event.getGame().getName())) {
            ConfigurationSection gameConfig = gamesConfig.getConfigurationSection(event.getGame().getName());
            if (gameConfig.contains("shop")) {
                File shopConfigsFolder = new File(BedwarsGames.getInstance().getDataFolder().getPath() + "/shops");
                if (shopConfigsFolder.exists() || shopConfigsFolder.mkdirs()) {
                    File gameShopFile = new File(shopConfigsFolder, gameConfig.getString("shop"));
                    FileConfiguration gameShopConfiguration = YamlConfiguration.loadConfiguration(gameShopFile);
                    Bukkit.getConsoleSender().sendMessage(BedwarsGames.getConfiguration().getString("prefix") + BedwarsGames.getMessage("replace-shop").replaceAll("\\{game}", event.getGame().getName()).replaceAll("\\{shop}", gameConfig.getString("shop")));

                    event.getGame().getItemShopCategories().clear();
                    event.getGame().getItemShopCategories().putAll(MerchantCategory.loadCategories(gameShopConfiguration));
                    List<MerchantCategory> orderedCategory = new ArrayList<>(event.getGame().getItemShopCategories().values());
                    orderedCategory.sort(new MerchantCategoryComparator());
                    event.getGame().setOrderedShopCategories(orderedCategory);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            String gameName = BedwarsRelApi.getGameOfPlayer(player).getName();
            ConfigurationSection gameConfiguration = BedwarsGames.getConfiguration().getConfigurationSection("games." + gameName);
            if (gameConfiguration != null && gameConfiguration.contains("damage." + event.getCause().name())) {
                event.setCancelled(!gameConfiguration.getBoolean("damage." + event.getCause().name()));
            }
        }
    }
}
