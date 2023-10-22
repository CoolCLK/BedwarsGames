package coolclk.bedwarsgames;

import coolclk.bedwarsgames.command.BedwarsGamesCommand;
import coolclk.bedwarsgames.listener.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class BedwarsGames extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getConsoleSender().sendMessage(getConfiguration().getString("prefix") + getMessage("plugin-enable"));
        if (checkDepend("BedwarsRel")) {
            Bukkit.getPluginCommand("bedwarsgames").setExecutor(BedwarsGamesCommand.getInstance());
            Bukkit.getPluginCommand("bedwarsgames").setTabCompleter((TabCompleter) BedwarsGamesCommand.getInstance());
            Bukkit.getPluginCommand("bg").setExecutor(BedwarsGamesCommand.getInstance());
            Bukkit.getPluginCommand("bg").setTabCompleter((TabCompleter) BedwarsGamesCommand.getInstance());
            Bukkit.getPluginManager().registerEvents(EventListener.getInstance(), this);
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(getConfiguration().getString("prefix") + getMessage("plugin-disable"));
    }

    private boolean checkDepend(String... plugins) {
        boolean success = true;
        for (String plugin : plugins) {
            if (Bukkit.getPluginManager().isPluginEnabled(plugin)) {
                Bukkit.getConsoleSender().sendMessage(getConfiguration().getString("prefix") + getMessage("found-depend").replaceAll("\\{plugin}", plugin));
            } else {
                Bukkit.getConsoleSender().sendMessage(getConfiguration().getString("prefix") + getMessage("no-depend").replaceAll("\\{plugin}", plugin));
                success = false;
            }
        }
        return success;
    }

    public static BedwarsGames getInstance() {
        return BedwarsGames.getPlugin(BedwarsGames.class);
    }

    public static FileConfiguration getConfiguration() {
        return getInstance().getConfig();
    }

    public static String getMessage(String key) {
        return getConfiguration().getConfigurationSection("messages").getString(key);
    }
}
