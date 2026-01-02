package me.onecraft.autovehicles2.listeners;

import me.onecraft.autovehicles2.AutoVehicles2;
import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.persistence.PersistentDataType;

public class MinecartLeaveHandler implements Listener {

    private final AutoVehicles2 plugin;

    public MinecartLeaveHandler(AutoVehicles2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMinecartLeave(VehicleExitEvent e) {
        if (!(e.getExited() instanceof Player && e.getVehicle() instanceof Minecart)) {
            return;
        }

        if (e.getVehicle().getPersistentDataContainer().has(plugin.getVehicleKey(), PersistentDataType.BYTE)) {

            // Fix for "Already retired" crash:
            // If the vehicle is already invalid (e.g., being removed by server for portal
            // travel or chunk unload),
            // we should not try to remove it again or teleport the player.
            if (!e.getVehicle().isValid()) {
                plugin.getParticleHandler().removeVehicleState(e.getVehicle().getUniqueId());
                return;
            }

            // Check if player is teleporting (Portal Travel)
            if (plugin.getTeleportingPlayers().contains(e.getExited().getUniqueId())) {
                // Do NOT remove the vehicle, let it travel with the player
                return;
            }

            // Check if vehicle itself is transitioning (e.g. Portal Travel)
            if (plugin.getTransitioningVehicles().contains(e.getVehicle().getUniqueId())) {
                return;
            }

            // Teleport player to a safe location to avoid clipping
            Location safeLocation = e.getVehicle().getLocation().add(0, 0.5, 0);
            e.getExited().teleport(safeLocation);

            plugin.getParticleHandler().removeVehicleState(e.getVehicle().getUniqueId());

            // DELAYED REMOVAL (Fix for Portal Travel / Multiverse)
            // We wait 1 tick to see if the player is re-mounted (e.g. by
            // Multiverse-Portals)
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!e.getVehicle().isValid()) {
                    return; // Already removed (e.g. by portal)
                }

                // If config allows portal travel, check if it has passengers now
                if (plugin.getConfig().getBoolean("settings.allow_portal_travel", true)) {
                    if (!e.getVehicle().getPassengers().isEmpty()) {
                        // Player is back in the vehicle (or another player is), DO NOT REMOVE
                        return;
                    }
                }

                // If checking transitioningVehicles set from PortalListener
                if (plugin.getTransitioningVehicles().contains(e.getVehicle().getUniqueId())) {
                    return;
                }

                // If still empty and valid, remove it
                try {
                    e.getVehicle().remove();
                } catch (IllegalStateException ignored) {
                }
            }, 1L);
        }
    }
}
