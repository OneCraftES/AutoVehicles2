package me.onecraft.autovehicles2.listeners;

import me.onecraft.autovehicles2.AutoVehicles2;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class VehicleMoveHandler implements Listener {
    private final AutoVehicles2 plugin;
    private final Vector multiplier = new Vector(1.5, 1, 1.5);

    public VehicleMoveHandler(AutoVehicles2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVehicleUpdate(VehicleMoveEvent e) {
        if (e.getVehicle().getPassengers().isEmpty() || !(e.getVehicle() instanceof Minecart)) {
            return;
        }

        for (Entity entity : e.getVehicle().getPassengers()) {
            if (entity instanceof Player && e.getVehicle().getPersistentDataContainer().has(plugin.getVehicleKey(),
                    PersistentDataType.BYTE)) {
                Minecart minecart = (Minecart) e.getVehicle();
                // Apply custom speed
                minecart.setVelocity(minecart.getVelocity().multiply(multiplier));

                // Spawn particles
                plugin.getParticleHandler().spawnVehicleParticles(minecart, (Player) entity);
            }
        }
    }
}
