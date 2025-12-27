package me.onecraft.autovehicles2.commands;

import me.onecraft.autovehicles2.PlayerConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandToggleBoat implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!manageValidity(command, sender)) {
            return false;
        }

        final boolean isToggled = PlayerConfig.getPlayersFileConfig().getBoolean("players."+((Player) sender).getUniqueId()+".boat.toggled");
        if (isToggled) {
            PlayerConfig.getPlayersFileConfig().set("players."+((Player) sender).getUniqueId()+".boat.toggled", false);
            PlayerConfig.savePlayerConfig();
            PlayerConfig.reloadPlayerConfig();

            sender.sendMessage(ChatColor.YELLOW + "AutoBoat has now been " + ChatColor.RED + "disabled" + ChatColor.YELLOW + " for you!");
        } else {
            PlayerConfig.getPlayersFileConfig().set("players."+((Player) sender).getUniqueId()+".boat.toggled", true);
            PlayerConfig.savePlayerConfig();
            PlayerConfig.reloadPlayerConfig();

            sender.sendMessage(ChatColor.YELLOW + "AutoBoat has now been " + ChatColor.GREEN + "enabled" + ChatColor.YELLOW + " for you!");
        }

        return true;
    }

    /**
     * Checks whether the command and sender are valid to execute the /toggleboat command. If at any stage the validity
     * is not met, the sender is sent a messages stating why. To use the command, the sender must:
     * <ol>
     *     <li>Be a player</li>
     *     <li>Use the correct command ("/toggleboat")</li>
     *     <li>Have permission to use the command (autovehicles2.use) or have OP</li>
     * </ol>
     * @param command the command
     * @param sender the command sender
     * @return whether the command and sender are valid for the /toggleboat command
     */
    private boolean manageValidity(Command command, CommandSender sender) {
        if (!command.getName().equals("toggleboat")) {
            return false;
        } else if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use the /toggleboat command!");
            return false;
        } else if (!sender.hasPermission("autovehicles2.use") && !sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }

        return true;
    }
}
