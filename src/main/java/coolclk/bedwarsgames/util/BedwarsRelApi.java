package coolclk.bedwarsgames.util;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.Team;
import org.bukkit.entity.Player;

import java.util.Iterator;

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
        int max = 0;
        Team team;
        for (Iterator<Team> var2 = game.getTeams().values().iterator(); var2.hasNext(); max += team.getMaxPlayers()) {
            team = var2.next();
        }
        return max;
    }
}
