package me.tisleo.autominecart.listeners;

import me.tisleo.autominecart.AutoMinecart;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.persistence.PersistentDataType;

public class MinecartLeaveHandler implements Listener {

    private final AutoMinecart plugin;

    public MinecartLeaveHandler(AutoMinecart plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMinecartLeave(VehicleExitEvent e) {
        if (!(e.getExited() instanceof Player && e.getVehicle() instanceof Minecart)) {
            return;
        }

        final Player p = ((Player) e.getExited()).getPlayer();
        if (e.getVehicle().getPersistentDataContainer().has(plugin.getVehicleKey(), PersistentDataType.BYTE)) {
            e.getVehicle().remove();

            /*
             * Need to teleport player half a block upwards because sometimes when a player
             * exits the minecart,
             * they get stuck halfway inside the block under them.
             */
            p.teleport(p.getLocation().add(0, 0.5, 0));
        }
    }
}
