package coolclk.bedwarsgames.gui;

import coolclk.bedwarsgames.BedwarsGames;
import coolclk.bedwarsgames.util.BedwarsRelApi;
import coolclk.bedwarsgames.util.PluginUtil;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import ldcr.BedwarsXP.BedwarsXP;
import ldcr.BedwarsXP.Config;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class RandomMapGui extends ChestGui {
    public RandomMapGui(InventoryHolder holder, int rows, String title) {
        super(holder, rows, title, PluginUtil.getPluginInstance(BedwarsGames.class));
    }

    public static void openSelectorGui(String selector, Player player) {
        if (selector == null) {
            selector = "_GLOBAL_";
        }
        if (PluginUtil.getPluginInstance(BedwarsGames.class).getConfig().getConfigurationSection("selectors").contains(selector) && player.hasPermission("bedwarsgames.selector." + selector)) {
            ConfigurationSection menuConfig = PluginUtil.getPluginInstance(BedwarsGames.class).getConfig().getConfigurationSection("selectors").getConfigurationSection(selector).getConfigurationSection("menu");
            RandomMapGui chestGui = new RandomMapGui(player, menuConfig.getInt("rows"), menuConfig.getString("title"));
            chestGui.changeGuiToMain(selector, menuConfig);
            player.openInventory(chestGui.getInventory());
        }
    }

    protected void changeGuiToMain(String selector, ConfigurationSection menuConfig) {
        this.clearItems();
        ItemStack randomItem = new ItemStack(Material.BED), mapItem = new ItemStack(Material.SIGN), closeItem = new ItemStack(Material.BARRIER);
        ItemMeta randomItemMeta = randomItem.getItemMeta(), mapItemMeta = mapItem.getItemMeta(), closeItemMeta = closeItem.getItemMeta();
        randomItemMeta.setDisplayName(menuConfig.getString("items.random.name"));
        randomItemMeta.setLore(menuConfig.getStringList("items.random.lore"));
        mapItemMeta.setDisplayName(menuConfig.getString("items.map.name"));
        mapItemMeta.setLore(menuConfig.getStringList("items.map.lore"));
        closeItemMeta.setDisplayName(menuConfig.getString("items.close.name"));
        closeItemMeta.setLore(menuConfig.getStringList("items.close.lore"));
        randomItem.setItemMeta(randomItemMeta);
        mapItem.setItemMeta(mapItemMeta);
        closeItem.setItemMeta(closeItemMeta);
        this.replaceItem(menuConfig.getInt("items.random.slot"), randomItem).setClickAction(() -> {
            List<Game> gameList = new ArrayList<>();
            BedwarsGames.getConfiguration().getMessageManager().getAll("selectors." + selector + ".games").forEach(name -> {
                Game game = BedwarsRelApi.getGameByName(name);
                if (game != null) {
                    gameList.add(game);
                }
            });
            if (!gameList.isEmpty()) {
                AtomicReference<Game> selectGame = new AtomicReference<>(gameList.get(new Random().nextInt(gameList.size())));
                gameList.sort(Comparator.comparingInt(Game::getPlayerAmount));
                gameList.forEach(game -> {
                    if (game.getState() == GameState.WAITING && (selectGame.get().getState() != GameState.WAITING || game.getPlayerAmount() > selectGame.get().getPlayerAmount())) {
                        selectGame.set(game);
                    }
                });
                if (selectGame.get() != null && selectGame.get().getState() == GameState.WAITING) {
                    if (BedwarsRelApi.joinGame(((Player) this.getInventory().getHolder()), selectGame.get())) {
                        ((Player) this.getInventory().getHolder()).sendMessage(BedwarsGames.getConfiguration().getPrefix() + BedwarsGames.getConfiguration().getMessageManager().get("change-game").replaceAll("\\{map}", selectGame.get().getName()));
                    }
                } else {
                    ((Player) this.getInventory().getHolder()).sendMessage(BedwarsGames.getConfiguration().getPrefix() + BedwarsGames.getConfiguration().getMessageManager().get("map-not-found"));
                }
            }
        });
        this.replaceItem(menuConfig.getInt("items.map.slot"), mapItem).setClickAction(() -> changeGuiToMap(selector, menuConfig, 0));
        this.replaceItem(menuConfig.getInt("items.close.slot"), closeItem).setClickAction(((Player) this.getInventory().getHolder())::closeInventory);
    }

    protected void changeGuiToMap(String selector, ConfigurationSection menuConfig, int page) {
        this.clearItems();
        List<Game> gameList = new ArrayList<>();
        if (selector.equals("_GLOBAL_")) {
            gameList.addAll(BedwarsRel.getInstance().getGameManager().getGames());
        } else {
            BedwarsGames.getConfiguration().getMessageManager().getAll("selectors." + selector + ".games").forEach(name -> gameList.add(BedwarsRel.getInstance().getGameManager().getGame(name)));
        }
        int maxPage = gameList.size() / (this.getInventory().getSize() - 9);

        ItemStack pageItem = new ItemStack(Material.BOOK);
        ItemMeta pageItemMeta = pageItem.getItemMeta();
        pageItemMeta.setDisplayName(menuConfig.getString("items.page.name").replaceAll("\\{page}", String.valueOf(page + 1)).replaceAll("\\{max_page}", String.valueOf(maxPage + 1)));
        pageItemMeta.setLore(menuConfig.getStringList("items.page.lore"));
        pageItem.setItemMeta(pageItemMeta);
        this.replaceItem(menuConfig.getInt("items.page.slot"), pageItem).setClickAction(() -> changeGuiToMain(selector, menuConfig));

        if (!gameList.isEmpty()) {
            if (page - 1 >= 0) {
                ItemStack previousItem = new ItemStack(Material.ARROW);
                ItemMeta previousItemMeta = previousItem.getItemMeta();
                previousItemMeta.setDisplayName(menuConfig.getString("items.previous.name"));
                previousItemMeta.setLore(menuConfig.getStringList("items.previous.lore"));
                previousItem.setItemMeta(previousItemMeta);
                this.replaceItem(menuConfig.getInt("items.previous.slot"), previousItem).setClickAction(() -> changeGuiToMap(selector, menuConfig, page - 1));
            }

            if (page + 1 < maxPage) {
                ItemStack nextItem = new ItemStack(Material.ARROW);
                ItemMeta nextItemMeta = nextItem.getItemMeta();
                nextItemMeta.setDisplayName(menuConfig.getString("items.next.name"));
                nextItemMeta.setLore(menuConfig.getStringList("items.next.lore"));
                nextItem.setItemMeta(nextItemMeta);
                this.replaceItem(menuConfig.getInt("items.next.slot"), nextItem).setClickAction(() -> changeGuiToMap(selector, menuConfig, page + 1));
            }

            generateMapSlots(gameList, 0, page * (this.getInventory().getSize() - 9), this.getInventory().getSize() - 9);
        }
    }
    
    protected void generateMapSlots(List<Game> games, @SuppressWarnings("SameParameterValue") int startSlot, int startIndex, int size) {
        for (int i = startIndex; i < size && i < games.size(); i++) {
            Game game = games.get(i);
            ItemStack mapItem = new ItemStack(Material.STAINED_CLAY, 1, (game.getState() == GameState.WAITING ? (short) 5 : (game.getState() == GameState.RUNNING ? (short) 4 : (short) 14)));
            ItemMeta mapItemMeta = mapItem.getItemMeta();
            mapItemMeta.setDisplayName(BedwarsGames.getConfiguration().getMessageManager().get("map-name").replaceAll("\\{name}", game.getName()));
            List<String> mapLore = new ArrayList<>();
            String playerLore = BedwarsGames.getConfiguration().getMessageManager().get("map-players");
            playerLore = playerLore.replaceAll("\\{players}", String.valueOf(game.getPlayerAmount()));
            playerLore = playerLore.replaceAll("\\{max_players}", String.valueOf(BedwarsRelApi.getGameMaxPlayers(game)));
            String mapMode = "";
            if (PluginUtil.getPluginInstance(BedwarsGames.class).getConfig().contains("games." + game.getName() + ".mode")) {
                String keyName = "modes." + PluginUtil.getPluginInstance(BedwarsGames.class).getConfig().getString("games." + game.getName() + ".mode") + ".name";
                if (PluginUtil.getPluginInstance(BedwarsGames.class).getConfig().contains(keyName)) {
                    mapMode = PluginUtil.getPluginInstance(BedwarsGames.class).getConfig().getString(keyName);
                }
            }
            if (PluginUtil.isPluginEnabled(BedwarsXP.class) && Config.isGameEnabledXP(game.getName())) {
                if (PluginUtil.getPluginInstance(BedwarsGames.class).getConfig().contains("modes.xp-mode.name")) {
                    if (mapMode.isEmpty()) {
                        mapMode = BedwarsGames.getConfiguration().getMessageManager().get("modes.xp-mode.name");
                    } else {
                        mapMode += " " + BedwarsGames.getConfiguration().getMessageManager().get("map-mode-extra").replaceAll("\\{mode}", BedwarsGames.getConfiguration().getMessageManager().get("modes.xp-mode.name"));
                    }
                }
            }
            if (!mapMode.isEmpty() || PluginUtil.getPluginInstance(BedwarsGames.class).getConfig().contains("modes._DEFAULT_.name")) {
                mapLore.add(BedwarsGames.getConfiguration().getMessageManager().get("map-mode").replaceAll("\\{mode}", mapMode.isEmpty() ? PluginUtil.getPluginInstance(BedwarsGames.class).getConfig().getString("modes._DEFAULT_.name") : mapMode));
            }
            mapLore.add(playerLore);
            mapLore.add(BedwarsGames.getConfiguration().getMessageManager().get("map-state-" + (game.getState() == GameState.WAITING ? "waiting" :
                    (game.getState() == GameState.RUNNING ? "running" :
                            "stopping"))));BedwarsGames.getConfiguration().getMessageManager().get("map-state-" + (game.getState() == GameState.WAITING ? "waiting" :
                    (game.getState() == GameState.RUNNING ? "running" :
                            "stopping")));
            mapLore.add("");
            mapLore.add(BedwarsGames.getConfiguration().getMessageManager().get("map-join"));
            mapItemMeta.setLore(mapLore);
            mapItem.setItemMeta(mapItemMeta);
            this.replaceItem(startSlot, mapItem).setClickAction(() -> {
                switch (game.getState()) {
                    case WAITING: {
                        if (BedwarsRelApi.joinGame(((Player) this.getInventory().getHolder()), game)) {
                            ((Player) this.getInventory().getHolder()).sendMessage(BedwarsGames.getConfiguration().getPrefix() + BedwarsGames.getConfiguration().getMessageManager().get("change-game").replaceAll("\\{map}", game.getName()));
                        } else {
                            ((Player) this.getInventory().getHolder()).sendMessage(BedwarsGames.getConfiguration().getPrefix() + BedwarsGames.getConfiguration().getMessageManager().get("player-in-game"));
                        }
                        break;
                    }
                    case RUNNING: {
                        ((Player) this.getInventory().getHolder()).sendMessage(BedwarsGames.getConfiguration().getPrefix() + BedwarsGames.getConfiguration().getMessageManager().get("game-running"));
                        break;
                    }
                    case STOPPED: {
                        ((Player) this.getInventory().getHolder()).sendMessage(BedwarsGames.getConfiguration().getPrefix() + BedwarsGames.getConfiguration().getMessageManager().get("game-stopped"));
                        break;
                    }
                }
                BedwarsRelApi.joinGame((Player) this.getInventory().getHolder(), game);
                ((Player) this.getInventory().getHolder()).closeInventory();
                this.clearItems();
            });
            startSlot++;
        }
    }
}
