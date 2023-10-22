package coolclk.bedwarsgames.util;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import org.bukkit.entity.Player;

public class BedwarsRelApi {
    public static Game getGameByName(String name) {
        return BedwarsRel.getInstance().getGameManager().getGame(name);
    }

    public static Game getGameOfPlayer(Player player) {
        return BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
    }

    public static void joinGame(Player player, Game game) {
        if (getGameOfPlayer(player) == null) {
            game.playerJoins(player);
        }
    }

    public static int getGameMaxPlayers(Game game) {
        return game.getMaxPlayers();
    }
}
