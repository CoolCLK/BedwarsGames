package coolclk.bedwarsgames.listener;

import coolclk.bedwarsgames.BedwarsGames;
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
        if (BedwarsGames.getConfiguration().getModeManager().getXPMode().isAllowNonConfiguredResources()) {
            if (BedwarsRelApi.isInGame(event.getPlayer()) && BedwarsXPApi.isGameEnabledXP(BedwarsRelApi.getGameOfPlayer(event.getPlayer()))) {
                if (!BedwarsXPApi.containsResource(event.getItem().getItemStack().getType())) {
                    if (event.isCancelled()) {
                        event.setCancelled(false);
                    }
                }
            }
        }
    }
}
