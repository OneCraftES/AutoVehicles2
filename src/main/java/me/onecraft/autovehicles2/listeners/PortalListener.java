package me.onecraft.autovehicles2.listeners;

import me.onecraft.autovehicles2.AutoVehicles2;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.entity.Vehicle;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class PortalListener implements Listener {

    private final AutoVehicles2 plugin;

    public PortalListener(AutoVehicles2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (!plugin.getConfig().getBoolean("settings.allow_portal_travel", true)) {
            return;
        }

        if (!e.getPlayer().isInsideVehicle()) {
            return;
        }

        // Check if destination world is disabled
        List<String> disabledWorlds = plugin.getConfig().getStringList("disabled_worlds");
        if (e.getTo() != null && e.getTo().getWorld() != null) {
            if (disabledWorlds.contains(e.getTo().getWorld().getName())) {
                return; // Let standard removal happen
            }
        }

        // Tag player as teleporting so LeaveHandler knows to preserve the vehicle
        plugin.getTeleportingPlayers().add(e.getPlayer().getUniqueId());

        // Remove from set after a short delay (1 tick) to ensure we don't leak memory
        // The VehicleExitEvent typically fires immediately during this tick
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getTeleportingPlayers().remove(e.getPlayer().getUniqueId());
        }, 1L);
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent e) {
        handleVehicleTransition(e.getEntity(), e.getTo() != null ? e.getTo().getWorld().getName() : null);
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent e) {
        handleVehicleTransition(e.getEntity(), e.getTo() != null ? e.getTo().getWorld().getName() : null);
    }

    private void handleVehicleTransition(org.bukkit.entity.Entity entity, String toWorldName) {
        if (!plugin.getConfig().getBoolean("settings.allow_portal_travel", true)) {
            return;
        }

        if (!(entity instanceof Vehicle)) {
            return;
        }

        // Check if it's one of our vehicles
        if (!entity.getPersistentDataContainer().has(plugin.getVehicleKey(), PersistentDataType.BYTE)) {
            return;
        }

        // check disabled worlds
        List<String> disabledWorlds = plugin.getConfig().getStringList("disabled_worlds");
        if (toWorldName != null && disabledWorlds.contains(toWorldName)) {
            return; // Don't preserve it
        }

        // Mark as transitioning
        plugin.getTransitioningVehicles().add(entity.getUniqueId());

        // Cleanup after 1 tick
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getTransitioningVehicles().remove(entity.getUniqueId());
        }, 1L);
    }
}
