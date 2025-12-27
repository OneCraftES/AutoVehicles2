package me.onecraft.autovehicles2.listeners;

import me.onecraft.autovehicles2.AutoVehicles2;
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
            plugin.getParticleHandler().removeVehicleState(e.getVehicle().getUniqueId());
            e.getVehicle().remove();
        }
    }
}
