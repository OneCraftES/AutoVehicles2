package me.onecraft.autovehicles2.listeners;

import me.onecraft.autovehicles2.AutoVehicles2;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.persistence.PersistentDataType;

public class BoatLeaveHandler implements Listener {

    private final AutoVehicles2 plugin;

    public BoatLeaveHandler(AutoVehicles2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBoatLeave(VehicleExitEvent e) {
        if (!(e.getExited() instanceof Player && e.getVehicle() instanceof Boat)) {
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

            // Portal Travel Logic:
            // If allow_portal_travel is true, we want to AVOID removing the vehicle if the
            // exit is due to portal travel.
            // However, VehicleExitEvent doesn't specify the reason.
            // But usually, if the player is just pressing shift, the vehicle is VALID.
            // If the vehicle is INVALID, we caught it above.
            // So if we are here, the vehicle is VALID.
            // If we want to allow portal travel, relying on the 'isValid' check above is
            // usually enough for the crash.
            // But if the server hasn't invalidated it yet (e.g. some portal plugins),
            // checking config is good.
            // For now, the user request "vehicles aren't preserved" implies they are being
            // removed here.
            // We'll proceed with removal ONLY if we are sure it's a manual exit or regular
            // cleanup.
            // But wait, if allow_portal_travel is true, how do we distinguish manual exit?
            // Multiverse often ejects passengers BEFORE invalidating the entity.

            // Current safe approach:
            // 1. Fix the crash (done with !isValid check).
            // 2. Just proceed with standard removal for valid vehicles.
            // NOTE: If allow_portal_travel is requested to "work without issues", fixing
            // the crash might be the main part.
            // If the user specifically wants to PRESERVE vehicles that are otherwise being
            // deleted...
            // If Multiverse teleports the vehicle, it clones it and kills the old one.
            // If we kill the old one before Multiverse does, the clone might fail or be
            // weird.
            // But if it's valid, it means it hasn't been killed yet.

            // Let's implement the standard cleanup but respecting the config:
            // If config.allow_portal_travel is TRUE, we might need to rely on the fact that
            // portal travel usually involves the entity becoming invalid shortly after.
            // But since we can't foresee the future, we'll stick to:
            // "If valid, remove it (manual exit)." - This is the standard behavior.
            // "If invalid, don't touch it." - This fixes the crash.
            // If the user says "vehicles aren't preserved", it means they ARE being removed
            // when they shouldn't.
            // Meaning they are VALID when this event fires during portal travel.

            // If so, we need a way to know.
            // For now, let's strictly fix the crash first as it blocks everything.

            // Teleport player to a safe location to avoid clipping
            Location safeLocation = e.getVehicle().getLocation().add(0, 0.6, 0);
            e.getExited().teleport(safeLocation);

            plugin.getParticleHandler().removeVehicleState(e.getVehicle().getUniqueId());

            // DELAYED REMOVAL (Fix for Portal Travel / Multiverse)
            // We wait 1 tick to see if the player is re-mounted (e.g. by
            // Multiverse-Portals)
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!e.getVehicle().isValid()) {
                    return;
                }

                // If config allows portal travel, check if it has passengers now
                if (plugin.getConfig().getBoolean("settings.allow_portal_travel", true)) {
                    if (!e.getVehicle().getPassengers().isEmpty()) {
                        return;
                    }
                }

                if (plugin.getTransitioningVehicles().contains(e.getVehicle().getUniqueId())) {
                    return;
                }

                try {
                    e.getVehicle().remove();
                } catch (IllegalStateException ignored) {
                }
            }, 1L);
        }
    }
}
