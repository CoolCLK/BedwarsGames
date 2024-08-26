package coolclk.bedwarsgames.util;

import io.github.bedwarsrel.game.Game;
import ldcr.BedwarsXP.BedwarsXP;
import ldcr.BedwarsXP.Config;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class BedwarsXPApi {
    public static Class<? extends JavaPlugin> getPluginClass() {
        return BedwarsXP.class;
    }

    public static boolean isPluginEnable() {
        return PluginUtil.isPluginEnabled(getPluginClass());
    }

    public static String getPluginName() {
        return PluginUtil.getPluginInstance(getPluginClass()).getName();
    }

    public static boolean isGameEnabledXP(Game game) {
        return isGameEnabledXP(game != null ? game.getName() : null);
    }

    public static boolean isGameEnabledXP(String game) {
        return Config.isGameEnabledXP(game);
    }

    public static Map<Material, Integer> getResources() {
        return Config.resources;
    }

    public static Integer getResourceXP(Material material) {
        return getResources().get(material);
    }

    public static boolean containsResource(Material material) {
        return getResources().containsKey(material) && getResourceXP(material) > 0;
    }
}
