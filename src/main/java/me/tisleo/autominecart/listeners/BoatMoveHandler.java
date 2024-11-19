package me.tisleo.autominecart.listeners;

import me.tisleo.autominecart.AutoMinecart;
import me.tisleo.autominecart.particles.VehicleParticleHandler;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

public class BoatMoveHandler implements Listener {

    private final AutoMinecart plugin;
    private final Vector multiplier = new Vector(1.5, 1, 1.5);
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

        for (Entity entity : e.getVehicle().getPassengers()) {
            if (!(entity instanceof Player && plugin.getMinecartUsers().contains(entity))) {
                return;
            }

            Boat boat = (Boat) e.getVehicle();
            boat.setVelocity(boat.getVelocity().multiply(multiplier));
            
            // Spawn particles
            particleHandler.spawnVehicleParticles(boat, (Player) entity);
        }
    }
}
