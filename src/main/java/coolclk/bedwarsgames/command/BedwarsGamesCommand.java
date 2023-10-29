package coolclk.bedwarsgames.command;

import coolclk.bedwarsgames.BedwarsGames;
import coolclk.bedwarsgames.gui.RandomMapGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BedwarsGamesCommand implements CommandExecutor, TabCompleter {
    private final static BedwarsGamesCommand INSTANCE = new BedwarsGamesCommand();

    public static CommandExecutor getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> messages = new ArrayList<>();
        if (strings.length >= 1) {
            switch (strings[0]) {
                case "help": {
                    messages.addAll(BedwarsGames.getMessages("help-message"));
                    break;
                }
                case "selector": {
                    if (commandSender instanceof Player) {
                        RandomMapGui.openSelectorGui(strings.length > 1 ? strings[1] : null, (Player) commandSender);
                    } else {
                        messages.add(BedwarsGames.getMessage("not-player"));
                    }
                    break;
                }
                case "reload": {
                    if (commandSender.hasPermission("bedwarsgames.admin")) {
                        messages.add(BedwarsGames.getMessage("reloading"));
                        try {
                            BedwarsGames.getInstance().reloadConfig();
                            messages.add(BedwarsGames.getMessage("reloaded"));
                        } catch (Exception e) {
                            messages.add(BedwarsGames.getMessage("reload-error"));
                            e.printStackTrace();
                        }
                    }
                    break;
                }
                default: {
                    messages.add(BedwarsGames.getMessage("unknown-command"));
                    break;
                }
            }
        } else {
            messages.add(BedwarsGames.getMessage("unknown-command"));
        }
        messages.forEach(message -> commandSender.sendMessage(BedwarsGames.getConfiguration().getString("prefix") + message));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        final List<String> tab = new ArrayList<>();
        if (strings.length <= 1) {
            tab.addAll(Arrays.asList("help", "reload", "selector"));
        } else if (strings[0].equals("selector") && strings.length == 2) {
            tab.addAll(BedwarsGames.getInstance().getConfig().getConfigurationSection("modes").getKeys(false));
        }
        tab.removeIf(t -> !t.startsWith(strings[strings.length - 1]));
        return tab;
    }
}
