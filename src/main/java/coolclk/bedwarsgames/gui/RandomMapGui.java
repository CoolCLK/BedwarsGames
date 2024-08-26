package coolclk.bedwarsgames.gui;

import coolclk.bedwarsgames.BedwarsGames;
import coolclk.bedwarsgames.BedwarsGamesConstant;
import coolclk.bedwarsgames.BedwarsGamesConfiguration;
import coolclk.bedwarsgames.util.BedwarsRelApi;
import coolclk.bedwarsgames.util.BedwarsXPApi;
import coolclk.bedwarsgames.util.PluginUtil;
import io.github.bedwarsrel.game.GameState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class RandomMapGui extends ChestGui {
    private final BedwarsGamesConfiguration.SelectorManager.MapSelector selector;

    private RandomMapGui(InventoryHolder holder, BedwarsGamesConfiguration.SelectorManager.MapSelector selector) {
        super(holder, selector.getMenu().getRows(), selector.getMenu().getTitle(), PluginUtil.getPluginInstance(BedwarsGames.class));
        this.selector = selector;
    }

    public static void openSelectorGui(String selectorName, Player player) {
        if (selectorName == null) {
            selectorName = BedwarsGamesConstant.SELECTOR_GLOBAL;
        }
        if (BedwarsGames.getConfiguration().getSelectorManager().hasSelector(selectorName) && player.hasPermission("bedwarsgames.selector." + selectorName)) {
            BedwarsGamesConfiguration.SelectorManager.MapSelector selector = BedwarsGames.getConfiguration().getSelectorManager().getSelector(selectorName);
            RandomMapGui chestGui = new RandomMapGui(player, selector);
            chestGui.changeGuiToMain();
            player.openInventory(chestGui.getInventory());
        }
    }

    protected void changeGuiToMain() {
        this.clearItems();
        ItemStack randomItem = new ItemStack(Material.BED), mapItem = new ItemStack(Material.SIGN), closeItem = new ItemStack(Material.BARRIER);
        ItemMeta randomItemMeta = randomItem.getItemMeta(), mapItemMeta = mapItem.getItemMeta(), closeItemMeta = closeItem.getItemMeta();
        randomItemMeta.setDisplayName(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_RANDOM).getString(BedwarsGamesConstant.SELECTOR_MENU_ITEM_NAME));
        randomItemMeta.setLore(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_RANDOM).getStringList(BedwarsGamesConstant.SELECTOR_MENU_ITEM_LORE));
        mapItemMeta.setDisplayName(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_MAP).getString(BedwarsGamesConstant.SELECTOR_MENU_ITEM_NAME));
        mapItemMeta.setLore(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_MAP).getStringList(BedwarsGamesConstant.SELECTOR_MENU_ITEM_LORE));
        closeItemMeta.setDisplayName(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_CLOSE).getString(BedwarsGamesConstant.SELECTOR_MENU_ITEM_NAME));
        closeItemMeta.setLore(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_CLOSE).getStringList(BedwarsGamesConstant.SELECTOR_MENU_ITEM_LORE));
        randomItem.setItemMeta(randomItemMeta);
        mapItem.setItemMeta(mapItemMeta);
        closeItem.setItemMeta(closeItemMeta);
        this.replaceItem(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_RANDOM).getInt(BedwarsGamesConstant.SELECTOR_MENU_ITEM_SLOT), randomItem).setClickAction(() -> {
            List<BedwarsGamesConfiguration.GameManager.Game> gameList = this.selector.getGames();
            if (!gameList.isEmpty()) {
                AtomicReference<BedwarsGamesConfiguration.GameManager.Game> selectGame = new AtomicReference<>(gameList.get(new Random().nextInt(gameList.size())));
                gameList.sort(Comparator.comparingInt(value -> value.getGame().getPlayerAmount()));
                gameList.forEach(game -> {
                    if (game.getGame().getState() == GameState.WAITING && (selectGame.get().getGame().getState() != GameState.WAITING || game.getGame().getPlayerAmount() > selectGame.get().getGame().getPlayerAmount())) {
                        selectGame.set(game);
                    }
                });
                if (selectGame.get() != null && selectGame.get().getGame().getState() == GameState.WAITING) {
                    if (BedwarsRelApi.joinGame(((Player) this.getInventory().getHolder()), selectGame.get().getGame())) {
                        ((Player) this.getInventory().getHolder()).sendMessage(BedwarsGames.getConfiguration().getPrefix() + BedwarsGames.getConfiguration().getMessageManager().get("change-game").replaceAll("\\{map}", selectGame.get().getGame().getName()));
                    }
                } else {
                    ((Player) this.getInventory().getHolder()).sendMessage(BedwarsGames.getConfiguration().getPrefix() + BedwarsGames.getConfiguration().getMessageManager().get("map-not-found"));
                }
            }
        });
        this.replaceItem(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_MAP).getInt(BedwarsGamesConstant.SELECTOR_MENU_ITEM_SLOT), mapItem).setClickAction(() -> changeGuiToMap(0));
        this.replaceItem(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_CLOSE).getInt(BedwarsGamesConstant.SELECTOR_MENU_ITEM_SLOT), closeItem).setClickAction(((Player) this.getInventory().getHolder())::closeInventory);
    }

    protected void changeGuiToMap(int page) {
        this.clearItems();
        List<BedwarsGamesConfiguration.GameManager.Game> gameList = this.selector.getGames();
        int maxPage = gameList.size() / (this.getInventory().getSize() - 9);

        ItemStack pageItem = new ItemStack(Material.BOOK);
        ItemMeta pageItemMeta = pageItem.getItemMeta();
        pageItemMeta.setDisplayName(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_PAGE).getString(BedwarsGamesConstant.SELECTOR_MENU_ITEM_NAME).replaceAll("\\{page}", String.valueOf(page + 1)).replaceAll("\\{max_page}", String.valueOf(maxPage + 1)));
        pageItemMeta.setLore(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_PAGE).getStringList(BedwarsGamesConstant.SELECTOR_MENU_ITEM_LORE));
        pageItem.setItemMeta(pageItemMeta);
        this.replaceItem(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_PAGE).getInt(BedwarsGamesConstant.SELECTOR_MENU_ITEM_SLOT), pageItem).setClickAction(this::changeGuiToMain);

        if (!gameList.isEmpty()) {
            if (page - 1 >= 0) {
                ItemStack previousItem = new ItemStack(Material.ARROW);
                ItemMeta previousItemMeta = previousItem.getItemMeta();
                previousItemMeta.setDisplayName(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_PREVIOUS).getString(BedwarsGamesConstant.SELECTOR_MENU_ITEM_NAME));
                previousItemMeta.setLore(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_PREVIOUS).getStringList(BedwarsGamesConstant.SELECTOR_MENU_ITEM_LORE));
                previousItem.setItemMeta(previousItemMeta);
                this.replaceItem(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_PREVIOUS).getInt(BedwarsGamesConstant.SELECTOR_MENU_ITEM_SLOT), previousItem).setClickAction(() -> changeGuiToMap(page - 1));
            }

            if (page + 1 < maxPage) {
                ItemStack nextItem = new ItemStack(Material.ARROW);
                ItemMeta nextItemMeta = nextItem.getItemMeta();
                nextItemMeta.setDisplayName(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_NEXT).getString(BedwarsGamesConstant.SELECTOR_MENU_ITEM_NAME));
                nextItemMeta.setLore(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_NEXT).getStringList(BedwarsGamesConstant.SELECTOR_MENU_ITEM_LORE));
                nextItem.setItemMeta(nextItemMeta);
                this.replaceItem(this.selector.getMenu().getItems().get(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_NEXT).getInt(BedwarsGamesConstant.SELECTOR_MENU_ITEM_SLOT), nextItem).setClickAction(() -> changeGuiToMap(page + 1));
            }

            generateMapSlots(gameList, 0, page * (this.getInventory().getSize() - 9), this.getInventory().getSize() - 9);
        }
    }
    
    protected void generateMapSlots(List<BedwarsGamesConfiguration.GameManager.Game> games, @SuppressWarnings("SameParameterValue") int startSlot, int startIndex, int size) {
        for (int i = startIndex; i < size && i < games.size(); i++) {
            BedwarsGamesConfiguration.GameManager.Game game = games.get(i);
            ItemStack mapItem = new ItemStack(Material.STAINED_CLAY, 1, (game.getGame().getState() == GameState.WAITING ? (short) 5 : (game.getGame().getState() == GameState.RUNNING ? (short) 4 : (short) 14)));
            ItemMeta mapItemMeta = mapItem.getItemMeta();
            mapItemMeta.setDisplayName(BedwarsGames.getConfiguration().getMessageManager().get("map-name").replaceAll("\\{name}", game.getGame().getName()));
            List<String> mapLore = new ArrayList<>();
            String playerLore = BedwarsGames.getConfiguration().getMessageManager().get("map-players");
            playerLore = playerLore.replaceAll("\\{players}", String.valueOf(BedwarsRelApi.getGamePlayerAmount(game.getGame())));
            playerLore = playerLore.replaceAll("\\{max_players}", String.valueOf(BedwarsRelApi.getGameMaxPlayers(game.getGame())));
            String mapDisplayMode = game.getMode().getDisplayName();
            if (BedwarsXPApi.isPluginEnable() && BedwarsXPApi.isGameEnabledXP(game.getGame().getName())) {
                if (game.getMode() != BedwarsGames.getConfiguration().getModeManager().getXPMode()) {
                    mapDisplayMode += " ";
                    mapDisplayMode += BedwarsGames.getConfiguration().getMessageManager().get("map-mode-extra").replaceAll("\\{mode}", BedwarsGames.getConfiguration().getModeManager().getXPMode().getDisplayName());
                }
            }
            mapLore.add(BedwarsGames.getConfiguration().getMessageManager().get("map-mode").replaceAll("\\{mode}", mapDisplayMode));
            mapLore.add(playerLore);
            mapLore.add(BedwarsGames.getConfiguration().getMessageManager().get("map-state-" + (game.getGame().getState() == GameState.WAITING ? "waiting" :
                    (game.getGame().getState() == GameState.RUNNING ? "running" :
                            "stopping"))));BedwarsGames.getConfiguration().getMessageManager().get("map-state-" + (game.getGame().getState() == GameState.WAITING ? "waiting" :
                    (game.getGame().getState() == GameState.RUNNING ? "running" :
                            "stopping")));
            mapLore.add("");
            mapLore.add(BedwarsGames.getConfiguration().getMessageManager().get("map-join"));
            mapItemMeta.setLore(mapLore);
            mapItem.setItemMeta(mapItemMeta);
            this.replaceItem(startSlot, mapItem).setClickAction(() -> {
                switch (game.getGame().getState()) {
                    case WAITING: {
                        if (BedwarsRelApi.joinGame(((Player) this.getInventory().getHolder()), game.getGame())) {
                            ((Player) this.getInventory().getHolder()).sendMessage(BedwarsGames.getConfiguration().getPrefix() + BedwarsGames.getConfiguration().getMessageManager().get("change-game").replaceAll("\\{map}", game.getGame().getName()));
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
                BedwarsRelApi.joinGame((Player) this.getInventory().getHolder(), game.getGame());
                ((Player) this.getInventory().getHolder()).closeInventory();
                this.clearItems();
            });
            startSlot++;
        }
    }
}
