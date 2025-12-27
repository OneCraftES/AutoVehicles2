package me.tisleo.autominecart.listeners;

import me.tisleo.autominecart.AutoMinecart;
import me.tisleo.autominecart.particles.VehicleParticleHandler;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.persistence.PersistentDataType;

public class BoatMoveHandler implements Listener {

    private final AutoMinecart plugin;
    private final VehicleParticleHandler particleHandler;

    public BoatMoveHandler(AutoMinecart plugin) {
        this.plugin = plugin;
        this.particleHandler = new VehicleParticleHandler(plugin);
    }

    @EventHandler
    public void onVehicleUpdate(VehicleMoveEvent e) {
        if (e.getVehicle().getPassengers().isEmpty() || !(e.getVehicle() instanceof Boat)) {
            return;
        }

        Boat boat = (Boat) e.getVehicle();
        for (Entity entity : boat.getPassengers()) {
            if (entity instanceof Player
                    && boat.getPersistentDataContainer().has(plugin.getVehicleKey(), PersistentDataType.BYTE)) {
                // Only spawn particles, don't modify speed
                particleHandler.spawnVehicleParticles(boat, (Player) entity);
                break; // Found a valid player, no need to check others
            }
        }
    }
}
