package coolclk.bedwarsgames;

import coolclk.bedwarsgames.command.BedwarsGamesCommand;
import coolclk.bedwarsgames.listener.BedwarsScoreBoardAddonListener;
import coolclk.bedwarsgames.listener.BedwarsXPListener;
import coolclk.bedwarsgames.listener.BedwarsRelListener;
import coolclk.bedwarsgames.util.PluginUtil;
import io.github.bedwarsrel.BedwarsRel;
import ldcr.BedwarsXP.BedwarsXP;
import me.ram.bedwarsscoreboardaddon.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
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
        if (checkDependent(BedwarsRel.class, true)) {
            Bukkit.getPluginManager().registerEvents(BedwarsRelListener.getInstance(), this);
        } else return;
        if (checkDependent(BedwarsXP.class, false)) {
            Bukkit.getPluginManager().registerEvents(BedwarsXPListener.getInstance(), this);
        }
        if (checkDependent(Main.class, false)) {
            Bukkit.getPluginManager().registerEvents(BedwarsScoreBoardAddonListener.getInstance(), this);
        }
        Bukkit.getPluginCommand("bedwarsgames").setExecutor(BedwarsGamesCommand.getInstance());
        Bukkit.getPluginCommand("bedwarsgames").setTabCompleter((TabCompleter) BedwarsGamesCommand.getInstance());
        Bukkit.getPluginCommand("bg").setExecutor(BedwarsGamesCommand.getInstance());
        Bukkit.getPluginCommand("bg").setTabCompleter((TabCompleter) BedwarsGamesCommand.getInstance());
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

    private boolean checkDependent(Class<? extends JavaPlugin> clazz, boolean necessary) {
        boolean b = PluginUtil.isPluginEnabled(clazz);
        if (b) {
            Bukkit.getConsoleSender().sendMessage(getConfiguration().getPrefix() + getConfiguration().getMessageManager().get("found-depend").replaceAll("\\{plugin}", PluginUtil.getPluginName(clazz)));
        } else if (necessary) {
            Bukkit.getConsoleSender().sendMessage(getConfiguration().getPrefix() + getConfiguration().getMessageManager().get("no-depend").replaceAll("\\{plugin}", PluginUtil.getPluginName(clazz)));
            Bukkit.getPluginManager().disablePlugin(this);
        }
        return b;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.configuration = new BedwarsGamesConfiguration();
    }

    public static BedwarsGamesConfiguration getConfiguration() {
        return PluginUtil.getPluginInstance(BedwarsGames.class).configuration;
    }
}
