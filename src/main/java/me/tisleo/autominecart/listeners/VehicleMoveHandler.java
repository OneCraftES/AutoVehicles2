package me.tisleo.autominecart.listeners;

import me.tisleo.autominecart.AutoMinecart;
import me.tisleo.autominecart.particles.VehicleParticleHandler;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

public class VehicleMoveHandler implements Listener {

    private final AutoMinecart plugin;
    private final Vector multiplier = new Vector(1.5, 1, 1.5);
    private final VehicleParticleHandler particleHandler;

    public VehicleMoveHandler(AutoMinecart plugin) {
        this.plugin = plugin;
        this.particleHandler = new VehicleParticleHandler(plugin);
    }

    @EventHandler
    public void onVehicleUpdate(VehicleMoveEvent e) {
        if (e.getVehicle().getPassengers().isEmpty() || !(e.getVehicle() instanceof Minecart)) {
            return;
        }

        for (Entity entity : e.getVehicle().getPassengers()) {
            if (!(entity instanceof Player && plugin.getMinecartUsers().contains(entity))) {
                return;
            }

            Minecart minecart = (Minecart) e.getVehicle();
            minecart.setVelocity(minecart.getVelocity().multiply(multiplier));
            
            // Spawn particles
            particleHandler.spawnVehicleParticles(minecart, (Player) entity);
        }
    }
}
