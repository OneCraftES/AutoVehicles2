package me.onecraft.autovehicles2.commands;

import me.onecraft.autovehicles2.PlayerConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandToggleParticles implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!manageValidity(command, sender)) {
            return false;
        }

        final boolean isToggled = PlayerConfig.getPlayersFileConfig().getBoolean("players." + ((Player) sender).getUniqueId() + ".particles.enabled", true);
        if (isToggled) {
            PlayerConfig.getPlayersFileConfig().set("players." + ((Player) sender).getUniqueId() + ".particles.enabled", false);
            PlayerConfig.savePlayerConfig();
            PlayerConfig.reloadPlayerConfig();

            sender.sendMessage(ChatColor.YELLOW + "Vehicle particles have been " + ChatColor.RED + "disabled" + ChatColor.YELLOW + " for you!");
        } else {
            PlayerConfig.getPlayersFileConfig().set("players." + ((Player) sender).getUniqueId() + ".particles.enabled", true);
            PlayerConfig.savePlayerConfig();
            PlayerConfig.reloadPlayerConfig();

            sender.sendMessage(ChatColor.YELLOW + "Vehicle particles have been " + ChatColor.GREEN + "enabled" + ChatColor.YELLOW + " for you!");
        }

        return true;
    }

    /**
     * Checks whether the command and sender are valid to execute the /toggleparticles command.
     * @param command the command
     * @param sender the command sender
     * @return whether the command and sender are valid
     */
    private boolean manageValidity(Command command, CommandSender sender) {
        if (!command.getName().equals("toggleparticles")) {
            return false;
        } else if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use the /toggleparticles command!");
            return false;
        } else if (!sender.hasPermission("autovehicles.particles.toggle") && !sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }

        return true;
    }
}
