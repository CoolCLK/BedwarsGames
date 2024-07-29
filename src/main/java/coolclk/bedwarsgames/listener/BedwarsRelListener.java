package coolclk.bedwarsgames.listener;

import coolclk.bedwarsgames.BedwarsGames;
import coolclk.bedwarsgames.BedwarsGamesConfiguration;
import coolclk.bedwarsgames.util.BedwarsRelApi;
import io.github.bedwarsrel.events.BedwarsGameStartedEvent;
import io.github.bedwarsrel.villager.MerchantCategory;
import io.github.bedwarsrel.villager.MerchantCategoryComparator;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.List;

public class BedwarsRelListener implements Listener {
    private final static BedwarsRelListener INSTANCE = new BedwarsRelListener();

    public static BedwarsRelListener getInstance() {
        return INSTANCE;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedwarsGameStarted(BedwarsGameStartedEvent event) {
        BedwarsGamesConfiguration.GameManager.Game game = BedwarsGames.getConfiguration().getGameManager().get(event.getGame());
        if (game != null && game.getShopFile() != null) {
            Bukkit.getConsoleSender().sendMessage(BedwarsGames.getConfiguration().getPrefix() + BedwarsGames.getConfiguration().getMessageManager().get("replace-shop").replaceAll("\\{game}", event.getGame().getName()).replaceAll("\\{shop}", game.getShopFile().getName()));
            event.getGame().getItemShopCategories().clear();
            event.getGame().getItemShopCategories().putAll(MerchantCategory.loadCategories(game.getShop()));
            List<MerchantCategory> orderedCategory = new ArrayList<>(event.getGame().getItemShopCategories().values());
            orderedCategory.sort(new MerchantCategoryComparator());
            event.getGame().setOrderedShopCategories(orderedCategory);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER && BedwarsRelApi.isInGame((Player) event.getEntity())) {
            BedwarsGamesConfiguration.GameManager.Game game = BedwarsGames.getConfiguration().getGameManager().get(BedwarsRelApi.getGameOfPlayer((Player) event.getEntity()));
            if (game != null) {
                game.getEvents().stream().filter(e -> e instanceof BedwarsGamesConfiguration.GameManager.GameEvent.DamageEvent).findAny().ifPresent(e -> {
                    if (((BedwarsGamesConfiguration.GameManager.GameEvent.DamageEvent) e).getDamages().containsKey(event.getCause())) {
                        event.setCancelled(((BedwarsGamesConfiguration.GameManager.GameEvent.DamageEvent) e).getDamages().get(event.getCause()));
                    }
                });
            }
        }
    }
}
