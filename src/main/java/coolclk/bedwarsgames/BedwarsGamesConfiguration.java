package coolclk.bedwarsgames;

import coolclk.bedwarsgames.util.BedwarsRelApi;
import coolclk.bedwarsgames.util.BedwarsXPApi;
import coolclk.bedwarsgames.util.FileBuilder;
import coolclk.bedwarsgames.util.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.EntityDamageEvent;

import java.io.File;
import java.util.*;

public class BedwarsGamesConfiguration {
    private final MessageManager messageManager;
    private final ModeManager modeManager;
    private final SelectorManager selectorManager;
    private final GameManager gameManager;

    public BedwarsGamesConfiguration() throws ConfigurationException {
        this.messageManager = new MessageManager(this);
        this.modeManager = new ModeManager(this);
        this.gameManager = new GameManager(this);
        this.selectorManager = new SelectorManager(this);
    }

    private static Configuration getConfiguration() {
        return PluginUtil.getPluginInstance(BedwarsGames.class).getConfig();
    }

    public String getPrefix() {
        return getConfiguration().getString(BedwarsGamesConstant.PREFIX_SECTION);
    }

    public MessageManager getMessageManager() {
        return this.messageManager;
    }

    public ModeManager getModeManager() {
        return this.modeManager;
    }

    public GameManager getGameManager() {
        return this.gameManager;
    }

    public SelectorManager getSelectorManager() {
        return this.selectorManager;
    }

    private static class Manager {
        private final BedwarsGamesConfiguration configurations;

        private Manager(BedwarsGamesConfiguration configurations) {
            this.configurations = configurations;
        }

        @SuppressWarnings("unused")
        public BedwarsGamesConfiguration getConfigurations() {
            return configurations;
        }
    }

    public static class MessageManager extends Manager {
        private final ConfigurationSection messageSection = getConfiguration().getConfigurationSection(BedwarsGamesConstant.MESSAGES_SECTION);

        private MessageManager(BedwarsGamesConfiguration configurations) {
            super(configurations);
        }

        public String get(String key) {
            return messageSection.getString(key);
        }

        public List<String> getAll(String key) {
            return messageSection.getStringList(key);
        }
    }

    public static class ModeManager extends Manager {
        public interface Mode {
            String getId();

            default String getDisplayName() {
                return getConfiguration().getConfigurationSection(BedwarsGamesConstant.MODES_SECTION).getConfigurationSection(this.getId()).getString(BedwarsGamesConstant.MODE_NAME_SECTION);
            }

            interface XPMode extends Mode {
                boolean isAllowNonConfiguredResources();
            }
        }

        private final List<Mode> modes = new ArrayList<>();

        private ModeManager(BedwarsGamesConfiguration configurations) throws ConfigurationException {
            super(configurations);
            final ConfigurationSection modesSection = getConfiguration().getConfigurationSection(BedwarsGamesConstant.MODES_SECTION);
            boolean hasDefault = false, hasXP = !BedwarsXPApi.isPluginEnable();
            for (String mode : modesSection.getKeys(false)) {
                if (Objects.equals(mode, BedwarsGamesConstant.MODE_XP)) {
                    this.modes.add(new Mode.XPMode() {
                        @Override
                        public String getId() {
                            return mode;
                        }

                        @Override
                        public boolean isAllowNonConfiguredResources() {
                            return modesSection.getBoolean(BedwarsGamesConstant.MODE_XP_ALLOW_NON_CONFIGURED_RESOURCES, false);
                        }
                    });
                    hasXP = true;
                } else {
                    this.modes.add(() -> mode);
                    if (Objects.equals(mode, BedwarsGamesConstant.MODE_DEFAULT)) {
                        hasDefault = true;
                    }
                }
            }
            if (!hasDefault) {
                throw new ConfigurationException("The " + BedwarsGamesConstant.MODE_DEFAULT + " mode was not found");
            }
            if (!hasXP) {
                throw new ConfigurationException("Although BedwarsXP was enabled, the " + BedwarsGamesConstant.MODE_XP + " mode was not found");
            }
        }

        public List<Mode> getModes() {
            return this.modes;
        }

        public Mode getMode(String name) {
            return getModes().stream().filter(mode -> Objects.equals(mode.getId(), name)).findAny().orElse(null);
        }

        public Mode.XPMode getXPMode() {
            return (Mode.XPMode) getMode(BedwarsGamesConstant.MODE_XP);
        }
    }

    public static class SelectorManager extends Manager {
        private final List<MapSelector> selectors = new ArrayList<>();

        public static class MapSelector {
            public static class SelectorMenu {
                private final String title;
                private final int rows;
                private Map<String, ConfigurationSection> items; // Maybe I will serialize it in the future...

                private SelectorMenu(String title, int rows, Map<String, ConfigurationSection> items) {
                    this(title, rows);
                    this.setItems(items);
                }

                private SelectorMenu(String title, int rows) {
                    this.title = title;
                    this.rows = rows;
                }

                public String getTitle() {
                    return this.title;
                }

                public int getRows() {
                    return this.rows;
                }

                public Map<String, ConfigurationSection> getItems() {
                    return this.items;
                }

                public void setItems(Map<String, ConfigurationSection> items) {
                    this.items = items;
                }
            }

            private final String name;
            private final List<GameManager.Game> games;
            private SelectorMenu menu = null;

            private MapSelector(String name, List<GameManager.Game> games, SelectorMenu menu) {
                this(name, games);
                this.setMenu(menu);
            }

            private MapSelector(String name, List<GameManager.Game> games) {
                this(name);
                this.getGames().addAll(games);
            }

            private MapSelector(String name) {
                this.name = name;
                this.games = new ArrayList<>();
            }

            public String getName() {
                return this.name;
            }

            public List<GameManager.Game> getGames() {
                return this.games;
            }

            public void setMenu(SelectorMenu menu) {
                this.menu = menu;
            }

            public SelectorMenu getMenu() {
                return this.menu;
            }
        }

        private SelectorManager(BedwarsGamesConfiguration configurations) {
            super(configurations);
            final ConfigurationSection selectorsSection = getConfiguration().getConfigurationSection(BedwarsGamesConstant.SELECTORS_SECTION);
            for (String name : selectorsSection.getKeys(false)) {
                final ConfigurationSection selectorSection = selectorsSection.getConfigurationSection(name);
                List<GameManager.Game> selectorGames = new ArrayList<>();
                if (selectorSection.contains(BedwarsGamesConstant.SELECTOR_GAMES_SECTION)) {
                    for (String gameName : selectorSection.getStringList(BedwarsGamesConstant.SELECTOR_GAMES_SECTION)) {
                        final GameManager.Game game = configurations.getGameManager().get(gameName);
                        if (game != null) selectorGames.add(game);
                    }
                }
                if (selectorGames.isEmpty() && Objects.equals(name, BedwarsGamesConstant.SELECTOR_GLOBAL)) {
                    selectorGames.addAll(configurations.getGameManager().getGames());
                }
                final ConfigurationSection menuSection = selectorSection.getConfigurationSection(BedwarsGamesConstant.SELECTOR_MENU_SECTION);
                final ConfigurationSection menuItemsSection = menuSection.getConfigurationSection(BedwarsGamesConstant.SELECTOR_MENU_ITEMS_SECTION);
                Map<String, ConfigurationSection> menuItems = new HashMap<>();
                for (String key : menuItemsSection.getKeys(false)) {
                    menuItems.put(key, menuItemsSection.getConfigurationSection(key));
                }
                this.selectors.add(new MapSelector(name, selectorGames, new MapSelector.SelectorMenu(menuSection.getString(BedwarsGamesConstant.SELECTOR_MENU_TITLE_SECTION), menuSection.getInt(BedwarsGamesConstant.SELECTOR_MENU_ROWS_SECTION), menuItems)));
            }
        }

        public List<MapSelector> getSelectors() {
            return this.selectors;
        }

        public MapSelector getSelector(String name) {
            return this.getSelectors().stream().filter(selector -> Objects.equals(selector.getName(), name)).findAny().orElse(null);
        }

        public boolean hasSelector(String name) {
            return getSelector(name) != null;
        }
    }

    public static class GameManager extends Manager {
        public interface Game {
            default BedwarsGamesConfiguration configuration() {
                return BedwarsGames.getConfiguration();
            }

            io.github.bedwarsrel.game.Game getGame();

            @SuppressWarnings("unused")
            default ModeManager.Mode getMode() {
                return this.configuration().getModeManager().getMode(BedwarsGamesConstant.MODE_DEFAULT);
            }

            default YamlConfiguration getShop() {
                return null;
            }

            default File getShopFile() {
                return null;
            }

            default List<GameEvent> getEvents() {
                return Collections.emptyList();
            }

            default XPGame asXPGame() {
                return (XPGame) this;
            }
        }

        public interface XPGame extends Game {
            default boolean isAllowNonConfiguredResources() {
                return BedwarsGames.getConfiguration().getModeManager().getXPMode().isAllowNonConfiguredResources();
            }
        }

        public interface GameEvent {
            interface DamageEvent extends GameEvent {
                default Map<EntityDamageEvent.DamageCause, Boolean> getDamages() {
                    return Collections.emptyMap();
                }
            }
        }

        private static class GameInstance implements XPGame {
            private final YamlConfiguration shop;
            private File shopFile;
            private final List<GameEvent> gameEvents = new ArrayList<>();

            private final String game;
            private final ConfigurationSection gameSection;

            @SuppressWarnings("SwitchStatementWithTooFewBranches")
            public GameInstance(String game, ConfigurationSection gameSection) {
                this.game = game;
                this.gameSection = gameSection;

                File shopFolder = new File(PluginUtil.getPluginInstance(BedwarsGames.class).getDataFolder().getPath() + "/shops");
                if (shopFolder.exists() || shopFolder.mkdirs()) {
                    this.shopFile = gameSection.contains(BedwarsGamesConstant.GAME_SHOP_SECTION) ? FileBuilder.create(shopFolder, gameSection.getString(BedwarsGamesConstant.GAME_SHOP_SECTION)).ifPredicate(File::exists).orFile(null).build() : null;
                }
                this.shop = this.shopFile != null ? YamlConfiguration.loadConfiguration(this.shopFile) : null;

                if (gameSection.contains(BedwarsGamesConstant.GAME_EVENTS_SECTION)) {
                    final ConfigurationSection eventsSection = gameSection.getConfigurationSection(BedwarsGamesConstant.GAME_EVENTS_SECTION);
                    for (String type : eventsSection.getKeys(false)) {
                        final ConfigurationSection eventSection = eventsSection.getConfigurationSection(type);
                        switch (type) {
                            case BedwarsGamesConstant.GAME_EVENT_DAMAGE_SECTION: {
                                final Map<EntityDamageEvent.DamageCause, Boolean> damages = new HashMap<>();
                                for (String key : eventSection.getKeys(false)) {
                                    try {
                                        EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.valueOf(key);
                                        damages.put(cause, eventSection.getBoolean(key));
                                    } catch (IllegalArgumentException e) {
                                        Bukkit.getConsoleSender().sendMessage(BedwarsGames.getConfiguration().getPrefix() + BedwarsGames.getConfiguration().getMessageManager().get("unknown-damage-cause").replaceAll("\\{cause}", key).replaceAll("\\{game}", game));
                                    }
                                }
                                this.gameEvents.add(new GameEvent.DamageEvent() {
                                    @Override
                                    public Map<EntityDamageEvent.DamageCause, Boolean> getDamages() {
                                        return damages;
                                    }
                                });
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public io.github.bedwarsrel.game.Game getGame() {
                return BedwarsRelApi.getGameByName(this.game);
            }

            @Override
            public ModeManager.Mode getMode() {
                return BedwarsGames.getConfiguration().getModeManager().getMode(this.gameSection.getString(BedwarsGamesConstant.GAME_MODE_SECTION));
            }

            @Override
            public YamlConfiguration getShop() {
                return this.shop;
            }

            @Override
            public File getShopFile() {
                return this.shopFile;
            }

            @Override
            public List<GameEvent> getEvents() {
                return this.gameEvents;
            }

            @Override
            public boolean isAllowNonConfiguredResources() {
                if (this.gameSection.contains(BedwarsGamesConstant.GAME_XP_ALLOW_NON_CONFIGURED_RESOURCES)) {
                    return this.gameSection.getBoolean(BedwarsGamesConstant.GAME_XP_ALLOW_NON_CONFIGURED_RESOURCES);
                }
                return XPGame.super.isAllowNonConfiguredResources();
            }
        }

        private final List<Game> games = new ArrayList<>();

        private GameManager(BedwarsGamesConfiguration configurations) {
            super(configurations);
            ConfigurationSection gamesSection = getConfiguration().getConfigurationSection(BedwarsGamesConstant.GAMES_SECTION);
            for (final String game : gamesSection.getKeys(false)) {
                final ConfigurationSection gameSection = gamesSection.getConfigurationSection(game);
                this.games.add(new GameInstance(game, gameSection));
            }
        }

        public List<Game> getGames() {
            return this.games;
        }

        public Game get(io.github.bedwarsrel.game.Game game) {
            return this.get(game.getName());
        }

        public Game get(String name) {
            return this.getGames().stream().filter(game -> Objects.equals(game.getGame().getName(), name)).findAny().orElse(() -> BedwarsRelApi.getGameByName(name));
        }
    }
}
