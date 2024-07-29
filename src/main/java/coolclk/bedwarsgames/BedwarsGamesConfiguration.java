package coolclk.bedwarsgames;

import coolclk.bedwarsgames.util.BedwarsRelApi;
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
    private final MessageManager messageManager = new MessageManager();
    private final ModeManager modeManager = new ModeManager();
    private final GameManager gameManager = new GameManager();

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

    public static class MessageManager {
        private final ConfigurationSection messageSection = getConfiguration().getConfigurationSection(BedwarsGamesConstant.MESSAGES_SECTION);

        public String get(String key) {
            return messageSection.getString(key);
        }

        public List<String> getAll(String key) {
            return messageSection.getStringList(key);
        }
    }

    public static class ModeManager {
        public interface Mode {
            String getName();

            interface XPMode extends Mode {
                boolean isAllowNonConfiguredResources();
            }
        }

        private final List<Mode> modes = new ArrayList<>();
        private final ConfigurationSection modesSection = getConfiguration().getConfigurationSection(BedwarsGamesConstant.MODES_SECTION);

        private ModeManager() {
            for (String mode : this.modesSection.getKeys(false)) {
                if (Objects.equals(mode, BedwarsGamesConstant.MODE_XP)) {
                    this.modes.add(() -> mode);
                } else {
                    this.modes.add(new Mode.XPMode() {
                        @Override
                        public String getName() {
                            return mode;
                        }

                        @Override
                        public boolean isAllowNonConfiguredResources() {
                            return modesSection.getBoolean(BedwarsGamesConstant.MODE_XP_ALLOW_NON_CONFIGURED_RESOURCES, false);
                        }
                    });
                }
            }
        }

        public List<Mode> getModes() {
            return this.modes;
        }

        public Mode getMode(String name) {
            return getModes().stream().filter(mode -> Objects.equals(mode.getName(), name)).findAny().orElse(null);
        }

        public Mode.XPMode getXPMode() {
            return (Mode.XPMode) getMode(BedwarsGamesConstant.MODE_XP);
        }
    }

    public static class GameManager {
        public interface Game {
            io.github.bedwarsrel.game.Game getGame();

            @SuppressWarnings("unused")
            default ModeManager.Mode getMode() {
                return BedwarsGames.getConfiguration().getModeManager().getMode(BedwarsGamesConstant.MODE_DEFAULT);
            }

            default YamlConfiguration getShop() {
                return null;
            }

            default File getShopFile() {
                return null;
            }

            default YamlConfiguration getTeamShop() {
                return null;
            }

            default File getTeamShopFile() {
                return null;
            }

            default List<GameEvent> getEvents() {
                return Collections.emptyList();
            }
        }

        public interface GameEvent {
            interface DamageEvent extends GameEvent {
                default Map<EntityDamageEvent.DamageCause, Boolean> getDamages() {
                    return Collections.emptyMap();
                }
            }
        }

        private final List<Game> games = new ArrayList<>();

        @SuppressWarnings("SwitchStatementWithTooFewBranches")
        private GameManager() {
            ConfigurationSection gamesSection = getConfiguration().getConfigurationSection(BedwarsGamesConstant.GAMES_SECTION);
            for (final String game : gamesSection.getKeys(false)) {
                final ConfigurationSection gameSection = gamesSection.getConfigurationSection(game);
                this.games.add(new Game() {
                    {
                        File shopFolder = new File(PluginUtil.getPluginInstance(BedwarsGames.class).getDataFolder().getPath() + "/shops");
                        if (shopFolder.exists() || shopFolder.mkdirs()) {
                            this.shopFile = FileBuilder.create(shopFolder, gameSection.getString(BedwarsGamesConstant.GAME_SHOP_SECTION)).ifPredicate(File::exists).orFile(null).build();
                            this.teamShopFile = FileBuilder.create(shopFolder, gameSection.getString(BedwarsGamesConstant.GAME_TEAM_SHOP_SECTION)).ifPredicate(File::exists).orFile(null).build();
                        } else {
                            this.shopFile = null;
                            this.teamShopFile = null;
                        }
                        this.shop = this.shopFile != null ? YamlConfiguration.loadConfiguration(this.shopFile) : null;
                        this.teamShop = this.teamShopFile != null ? YamlConfiguration.loadConfiguration(this.teamShopFile) : null;

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
                        return BedwarsRelApi.getGameByName(game);
                    }

                    @Override
                    public ModeManager.Mode getMode() {
                        return BedwarsGames.getConfiguration().getModeManager().getMode(gameSection.getString(BedwarsGamesConstant.GAME_MODE_SECTION));
                    }

                    private final YamlConfiguration shop;
                    @Override
                    public YamlConfiguration getShop() {
                        return this.shop;
                    }

                    private final File shopFile;
                    @Override
                    public File getShopFile() {
                        return this.shopFile;
                    }

                    private final File teamShopFile;
                    @Override
                    public File getTeamShopFile() {
                        return this.teamShopFile;
                    }

                    private final YamlConfiguration teamShop;
                    @Override
                    public YamlConfiguration getTeamShop() {
                        return this.teamShop;
                    }

                    private final List<GameEvent> gameEvents = new ArrayList<>();
                    @Override
                    public List<GameEvent> getEvents() {
                        return this.gameEvents;
                    }
                });
            }
        }

        public List<Game> getGames() {
            return this.games;
        }

        public Game get(io.github.bedwarsrel.game.Game game) {
            return this.get(game.getName());
        }

        public Game get(String name) {
            return this.getGames().stream().filter(game -> Objects.equals(game.getGame().getName(), name)).findAny().orElse(null);
        }
    }
}
