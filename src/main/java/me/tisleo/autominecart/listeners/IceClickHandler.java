package me.tisleo.autominecart.listeners;

import me.tisleo.autominecart.AutoMinecart;
import me.tisleo.autominecart.PlayerConfig;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.entity.boat.OakBoat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class IceClickHandler implements Listener {

    private final AutoMinecart plugin;
    private static final Material[] validBlocks = { Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE };

    public IceClickHandler(AutoMinecart plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (!manageValidity(p, e)) {
            return;
        }

        Location spawnLocation = e.getClickedBlock().getLocation().add(0, 1, 0);
        spawnLocation.setYaw(p.getLocation().getYaw());
        spawnLocation.setPitch(p.getLocation().getPitch());

        Boat boat = p.getWorld().spawn(spawnLocation, OakBoat.class);
        boat.getPersistentDataContainer().set(plugin.getVehicleKey(), PersistentDataType.BYTE, (byte) 1);
        boat.addPassenger(p);
    }

    /**
     * Checks whether the player is valid to create and use a new AutoMinecart. To
     * be valid, the player must:
     * <ol>
     * <li>Be inside a world where the plugin is enabled</li>
     * <li>Have permission to use the plugin</li>
     * <li>Have the plugin toggled on for them (/togglecart command)</li>
     * <li>Not be inside a vehicle</li>
     * <li>Have right-clicked a valid rail with an empty main hand</li>
     * </ol>
     * 
     * @param p the player
     * @return whether the player is valid to create and use a new AutoMinecart.
     */
    private boolean manageValidity(Player p, PlayerInteractEvent e) {
        return !plugin.getConfig().getStringList("disabled_worlds").contains(p.getWorld().getName())
                && (p.isOp() || p.hasPermission("autominecart.use"))
                && (PlayerConfig.getPlayersFileConfig().getBoolean("players." + p.getUniqueId() + ".boat.toggled"))
                && !p.isInsideVehicle()
                && (e.getAction() == Action.RIGHT_CLICK_BLOCK
                        && Arrays.asList(validBlocks).contains(e.getClickedBlock().getType())
                        && p.getInventory().getItemInMainHand().getType().equals(Material.AIR));
    }
}
