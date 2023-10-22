package coolclk.bedwarsgames.gui;

import coolclk.bedwarsgames.BedwarsGames;
import coolclk.bedwarsgames.util.BedwarsRelApi;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class RandomMapGui extends ChestGui {
    public RandomMapGui(InventoryHolder holder, int rows, String title) {
        super(holder, rows, title);
    }

    public static void openModeGui(String mode, Player player) {
        if (BedwarsGames.getConfiguration().getConfigurationSection("modes").contains(mode) && player.hasPermission("bedwarsgames.mode." + mode)) {
            ConfigurationSection menuConfig = BedwarsGames.getConfiguration().getConfigurationSection("modes").getConfigurationSection(mode).getConfigurationSection("menu");
            RandomMapGui chestGui = new RandomMapGui(player, menuConfig.getInt("rows"), menuConfig.getString("title"));
            chestGui.changeGuiToMain(mode, menuConfig, chestGui);
            player.openInventory(chestGui.getInventory());
        }
    }

    protected void changeGuiToMain(String mode, ConfigurationSection menuConfig, ChestGui chestGui) {
        chestGui.clearItems();
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
        chestGui.replaceItem(menuConfig.getInt("items.random.slot"), randomItem).setClickAction(() -> {
            List<Game> gameList = new ArrayList<>();
            BedwarsGames.getConfiguration().getStringList("modes." + mode + ".games").forEach(name -> {
                Game game = BedwarsRelApi.getGameByName(name);
                if (game != null) {
                    gameList.add(game);
                }
            });
            if (!gameList.isEmpty()) {
                AtomicReference<Game> selectGame = new AtomicReference<>(gameList.get(new Random().nextInt(gameList.size())));
                gameList.forEach(game -> {
                    if (game.getPlayerAmount() > selectGame.get().getPlayerAmount()) {
                        selectGame.set(game);
                    }
                });
                if (BedwarsRelApi.joinGame(((Player) chestGui.getInventory().getHolder()), selectGame.get())){
                    ((Player) chestGui.getInventory().getHolder()).sendMessage(BedwarsGames.getConfiguration().getString("prefix") + BedwarsGames.getMessage("change-game").replaceAll("\\{map}", selectGame.get().getName()));
                }
            }
        });
        chestGui.replaceItem(menuConfig.getInt("items.map.slot"), mapItem).setClickAction(() -> changeGuiToMap(mode, menuConfig, chestGui, 0));
        chestGui.replaceItem(menuConfig.getInt("items.close.slot"), closeItem).setClickAction(((Player) chestGui.getInventory().getHolder())::closeInventory);
    }

    protected void changeGuiToMap(String mode, ConfigurationSection menuConfig, ChestGui chestGui, int page) {
        chestGui.clearItems();
        List<String> gameList = BedwarsGames.getConfiguration().getStringList("modes." + mode + ".games");
        int maxPage = gameList.size() / (chestGui.getInventory().getSize() - 9);

        ItemStack pageItem = new ItemStack(Material.BOOK);
        ItemMeta pageItemMeta = pageItem.getItemMeta();
        pageItemMeta.setDisplayName(menuConfig.getString("items.page.name").replaceAll("\\{page}", String.valueOf(page + 1)).replaceAll("\\{max_page}", String.valueOf(maxPage + 1)));
        pageItemMeta.setLore(menuConfig.getStringList("items.page.lore"));
        pageItem.setItemMeta(pageItemMeta);
        chestGui.replaceItem(menuConfig.getInt("items.page.slot"), pageItem).setClickAction(() -> changeGuiToMain(mode, menuConfig, chestGui));

        if (!gameList.isEmpty()) {
            if (page - 1 >= 0) {
                ItemStack previousItem = new ItemStack(Material.ARROW);
                ItemMeta previousItemMeta = previousItem.getItemMeta();
                previousItemMeta.setDisplayName(menuConfig.getString("items.previous.name"));
                previousItemMeta.setLore(menuConfig.getStringList("items.previous.lore"));
                previousItem.setItemMeta(previousItemMeta);
                chestGui.replaceItem(menuConfig.getInt("items.previous.slot"), previousItem).setClickAction(() -> changeGuiToMap(mode, menuConfig, chestGui, page - 1));
            }

            if (page + 1 < maxPage) {
                ItemStack nextItem = new ItemStack(Material.ARROW);
                ItemMeta nextItemMeta = nextItem.getItemMeta();
                nextItemMeta.setDisplayName(menuConfig.getString("items.next.name"));
                nextItemMeta.setLore(menuConfig.getStringList("items.next.lore"));
                nextItem.setItemMeta(nextItemMeta);
                chestGui.replaceItem(menuConfig.getInt("items.next.slot"), nextItem).setClickAction(() -> changeGuiToMap(mode, menuConfig, chestGui, page + 1));
            }

            int mapSlot = page * (chestGui.getInventory().getSize() - 9);
            for (int i = 0; i < chestGui.getInventory().getSize() - 9 && mapSlot < gameList.size(); i++) {
                Game game = BedwarsRel.getInstance().getGameManager().getGame(gameList.get(i));
                ItemStack mapItem = new ItemStack(Material.STAINED_CLAY, 1, (game.getState() == GameState.WAITING ? (short) 5 : (game.getState() == GameState.RUNNING ? (short) 4 : (short) 14)));
                ItemMeta mapItemMeta = mapItem.getItemMeta();
                mapItemMeta.setDisplayName(BedwarsGames.getMessage("map-name").replaceAll("\\{name}", game.getName()));
                String playerLore = BedwarsGames.getMessage("map-players");
                playerLore = playerLore.replaceAll("\\{players}", String.valueOf(game.getPlayerAmount()));
                playerLore = playerLore.replaceAll("\\{max_players}", String.valueOf(BedwarsRelApi.getGameMaxPlayers(game)));
                mapItemMeta.setLore(Arrays.asList(
                        playerLore,
                        BedwarsGames.getMessage("map-state-" + (game.getState() == GameState.WAITING ? "waiting" :
                                (game.getState() == GameState.RUNNING ? "running" :
                                        "stopping"))), "", BedwarsGames.getMessage("map-join")));
                mapItem.setItemMeta(mapItemMeta);
                chestGui.replaceItem(mapSlot, mapItem).setClickAction(() -> {
                    switch (game.getState()) {
                        case WAITING:
                        case RUNNING: {
                            if (BedwarsRelApi.joinGame(((Player) chestGui.getInventory().getHolder()), game)) {
                                ((Player) chestGui.getInventory().getHolder()).sendMessage(BedwarsGames.getConfiguration().getString("prefix") + BedwarsGames.getMessage("change-game").replaceAll("\\{map}", game.getName()));
                            } else {
                                ((Player) chestGui.getInventory().getHolder()).sendMessage(BedwarsGames.getConfiguration().getString("prefix") + BedwarsGames.getMessage("player-in-game"));
                            }
                            break;
                        }
                        case STOPPED: {
                            ((Player) chestGui.getInventory().getHolder()).sendMessage(BedwarsGames.getConfiguration().getString("prefix") + BedwarsGames.getMessage("game-stopping"));
                            break;
                        }
                    }
                    BedwarsRelApi.joinGame((Player) chestGui.getInventory().getHolder(), game);
                    ((Player) chestGui.getInventory().getHolder()).closeInventory();
                    chestGui.clearItems();
                });
                mapSlot++;
            }
        }
    }
}
