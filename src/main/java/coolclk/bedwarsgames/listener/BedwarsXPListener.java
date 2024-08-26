package coolclk.bedwarsgames.listener;

import coolclk.bedwarsgames.BedwarsGames;
import coolclk.bedwarsgames.BedwarsGamesConfiguration;
import coolclk.bedwarsgames.util.BedwarsRelApi;
import coolclk.bedwarsgames.util.BedwarsXPApi;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class BedwarsXPListener implements Listener {
    private final static BedwarsXPListener INSTANCE = new BedwarsXPListener();

    public static BedwarsXPListener getInstance() {
        return INSTANCE;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(@SuppressWarnings("deprecation") PlayerPickupItemEvent event) {
        if (BedwarsRelApi.isInGame(event.getPlayer())) {
            BedwarsGamesConfiguration.GameManager.Game game = BedwarsGames.getConfiguration().getGameManager().get(BedwarsRelApi.getGameOfPlayer(event.getPlayer()));
            if (game.getMode() == BedwarsGames.getConfiguration().getModeManager().getXPMode() && game.asXPGame().isAllowNonConfiguredResources()) {
                if (BedwarsXPApi.isGameEnabledXP(BedwarsRelApi.getGameOfPlayer(event.getPlayer()))) {
                    if (!BedwarsXPApi.containsResource(event.getItem().getItemStack().getType())) {
                        event.getPlayer().getInventory().addItem(event.getItem().getItemStack());
                    }
                }
            }
        }
    }
}
