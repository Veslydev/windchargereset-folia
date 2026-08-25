package dev.vesly.windchargeresetfolia;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

final class ReloadCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;
    private final ConfigManager configManager;

    ReloadCommand(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        if (arguments.length == 1 && arguments[0].equalsIgnoreCase("reload")) {
            if (!hasReloadPermission(sender)) {
                sender.sendMessage(configManager.settings().noPermissionMessage());
                return true;
            }

            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
                try {
                    Settings settings = configManager.reload();
                    sendOnSenderContext(sender, settings.reloadMessage());
                } catch (RuntimeException exception) {
                    plugin.getLogger().warning("Could not reload config: " + exception.getMessage());
                    sendOnSenderContext(sender, ChatColor.RED + "Could not reload WindCharge Reset config. See the server log.");
                }
            });
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length != 1 || !hasReloadPermission(sender)) {
            return List.of();
        }
        return "reload".startsWith(arguments[0].toLowerCase()) ? List.of("reload") : List.of();
    }

    private boolean hasReloadPermission(CommandSender sender) {
        return sender.hasPermission("windchargereset.reload") || sender.hasPermission("windchargestop.reload");
    }

    private void sendOnSenderContext(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            player.getScheduler().run(plugin, scheduledTask -> sender.sendMessage(message), () -> {
                // The command sender retired before the reload completed.
            });
            return;
        }
        Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> sender.sendMessage(message));
    }
}
