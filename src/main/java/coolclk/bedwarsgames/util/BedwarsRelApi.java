package coolclk.bedwarsgames.util;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import org.bukkit.entity.Player;

public class BedwarsRelApi {
    public static Game getGameByName(String name) {
        return BedwarsRel.getInstance().getGameManager().getGame(name);
    }

    public static Game getGameOfPlayer(Player player) {
        return BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
    }

    public static boolean isInGame(Player player) {
        return getGameOfPlayer(player) != null;
    }

    public static boolean joinGame(Player player, Game game) {
        if (getGameOfPlayer(player) == null) {
            return game.playerJoins(player);
        }
        return false;
    }
    
    public static int getGameMaxPlayers(Game game) {
        return game.getMaxPlayers();
    }
}
