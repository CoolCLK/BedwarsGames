package coolclk.bedwarsgames.listener;

import coolclk.bedwarsgames.BedwarsGames;
import coolclk.bedwarsgames.BedwarsGamesConfiguration;
import io.github.bedwarsrel.events.BedwarsGameStartedEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BedwarsScoreBoardAddonListener implements Listener {
    private final static BedwarsScoreBoardAddonListener INSTANCE = new BedwarsScoreBoardAddonListener();

    public static BedwarsScoreBoardAddonListener getInstance() {
        return INSTANCE;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedwarsGameStarted(BedwarsGameStartedEvent event) {
        BedwarsGamesConfiguration.GameManager.Game game = BedwarsGames.getConfiguration().getGameManager().get(event.getGame());
        if (game != null && game.getTeamShopFile() != null) {
            Bukkit.getConsoleSender().sendMessage(BedwarsGames.getConfiguration().getPrefix() + BedwarsGames.getConfiguration().getMessageManager().get("replace-team-shop").replaceAll("\\{game}", event.getGame().getName()).replaceAll("\\{shop}", game.getTeamShop().getName()));
        }
    }
}
