package coolclk.bedwarsgames.util;

import io.github.bedwarsrel.game.Game;
import ldcr.BedwarsXP.Config;
import org.bukkit.Material;

import java.util.Map;

public class BedwarsXPApi {
    public static boolean isGameEnabledXP(Game game) {
        return isGameEnabledXP(game.getName());
    }

    public static boolean isGameEnabledXP(String game) {
        return Config.isGameEnabledXP(game);
    }

    public static Map<Material, Integer> getResources() {
        return Config.resources;
    }

    public static boolean containsResource(Material material) {
        return getResources().containsKey(material);
    }
}
