package coolclk.bedwarsgames;

import coolclk.bedwarsgames.command.BedwarsGamesCommand;
import coolclk.bedwarsgames.listener.BedwarsXPListener;
import coolclk.bedwarsgames.listener.BedwarsRelListener;
import coolclk.bedwarsgames.util.BedwarsRelApi;
import coolclk.bedwarsgames.util.BedwarsXPApi;
import coolclk.bedwarsgames.util.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public final class BedwarsGames extends JavaPlugin {
    private BedwarsGamesConfiguration configuration;

    @Override
    public void onEnable() {
        try {
            this.setupDataFolder(Locale.getDefault());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.reloadConfig();
        Bukkit.getConsoleSender().sendMessage(getConfiguration().getPrefix() + getConfiguration().getMessageManager().get("plugin-enable"));
        if (checkDependent(BedwarsRelApi.isPluginEnable(), BedwarsRelApi.getPluginName(), true)) {
            Bukkit.getPluginManager().registerEvents(BedwarsRelListener.getInstance(), this);
            if (checkDependent(BedwarsXPApi.isPluginEnable(), BedwarsXPApi.getPluginName(), false)) {
                Bukkit.getPluginManager().registerEvents(BedwarsXPListener.getInstance(), this);
            }
            Bukkit.getPluginCommand("bedwarsgames").setExecutor(BedwarsGamesCommand.getInstance());
            Bukkit.getPluginCommand("bedwarsgames").setTabCompleter(BedwarsGamesCommand.getInstance());
            Bukkit.getPluginCommand("bg").setExecutor(BedwarsGamesCommand.getInstance());
            Bukkit.getPluginCommand("bg").setTabCompleter(BedwarsGamesCommand.getInstance());
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(getConfiguration().getPrefix() + getConfiguration().getMessageManager().get("plugin-disable"));
    }

    private void setupDataFolder(Locale locale) throws IOException {
        File configFile = new File(this.getDataFolder(), "config.yml");
        File shopsFolder = new File(this.getDataFolder(), "shops");
        switch (locale.toString()) {
            case "en_US":
            case "zh_CN": {
                if (!configFile.exists() || !configFile.isFile()) {
                    if (configFile.createNewFile()) {
                        PluginUtil.saveResource(this.getClass(), "locale/" + locale + "/config.yml", "config.yml");
                    }
                }
                if (!shopsFolder.exists() || !shopsFolder.isDirectory()) {
                    if (configFile.createNewFile()) {
                        PluginUtil.saveResource(this.getClass(), "locale/" + locale + "/shops", "shops/");
                    }
                }
                break;
            }
            default: {
                this.setupDataFolder(new Locale("en", "US"));
                break;
            }
        }
    }

    private boolean checkDependent(boolean b, String plugin, boolean necessary) {
        if (b) {
            Bukkit.getConsoleSender().sendMessage(getConfiguration().getPrefix() + getConfiguration().getMessageManager().get("found-depend").replaceAll("\\{plugin}", plugin));
        } else if (necessary) {
            Bukkit.getConsoleSender().sendMessage(getConfiguration().getPrefix() + getConfiguration().getMessageManager().get("no-depend").replaceAll("\\{plugin}", plugin));
            Bukkit.getPluginManager().disablePlugin(this);
        }
        return b;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        try {
            this.configuration = new BedwarsGamesConfiguration();
        } catch (ConfigurationException e) {
            throw new RuntimeException("There is a exception while reloading to a new configuration", e);
        }
    }

    public static BedwarsGamesConfiguration getConfiguration() {
        return PluginUtil.getPluginInstance(BedwarsGames.class).configuration;
    }
}
