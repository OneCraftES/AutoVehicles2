package me.onecraft.autovehicles2.listeners;

import me.onecraft.autovehicles2.AutoVehicles2;
import me.onecraft.autovehicles2.PlayerConfig;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.boat.OakBoat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.persistence.PersistentDataType;

public class WaterClickHandler implements Listener {

    private final AutoVehicles2 plugin;

    public WaterClickHandler(AutoVehicles2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (!validatePlayer(e, p)) {
            return;
        }

        // Raytrace logic with underwater fallback
        org.bukkit.util.RayTraceResult result;
        if (p.getEyeLocation().getBlock().getType() == Material.WATER) {
            // Underwater: Try hitting solid blocks first (e.g. slabs)
            result = p.rayTraceBlocks(5, FluidCollisionMode.NEVER);
            if (result == null || result.getHitBlock() == null || !isWaterOrWaterloggedSlab(result.getHitBlock())) {
                // Fallback: Allow hitting water itself
                result = p.rayTraceBlocks(5, FluidCollisionMode.ALWAYS);
            }
        } else {
            // Above water: Hit water surface
            result = p.rayTraceBlocks(5, FluidCollisionMode.ALWAYS);
        }

        if (result == null || result.getHitBlock() == null) {
            return;
        }

        Block hitBlock = result.getHitBlock();

        if (!isWaterOrWaterloggedSlab(hitBlock)) {
            return;
        }

        // Use the exact hit position for spawning
        Location spawnLoc = result.getHitPosition().toLocation(p.getWorld());

        // Keep player's rotation
        spawnLoc.setYaw(p.getLocation().getYaw());
        spawnLoc.setPitch(p.getLocation().getPitch());

        Boat boat = p.getWorld().spawn(spawnLoc, OakBoat.class);
        boat.getPersistentDataContainer().set(plugin.getVehicleKey(), PersistentDataType.BYTE, (byte) 1);
        boat.addPassenger(p);
    }

    /**
     * Checks whether the player is valid to create and use a new AutoVehicles2. To
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
     * @return whether the player is valid to create and use a new AutoVehicles2.
     */
    private boolean validatePlayer(PlayerInteractEvent e, Player p) {
        return (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)
                && !plugin.getConfig().getStringList("disabled_worlds").contains(p.getWorld().getName())
                && (p.isOp() || p.hasPermission("autovehicles2.use"))
                && (PlayerConfig.getPlayersFileConfig().getBoolean("players." + p.getUniqueId() + ".boat.toggled"))
                && !p.isInsideVehicle()
                && p.getInventory().getItemInMainHand().getType().equals(Material.AIR);
    }

    private boolean isWaterOrWaterloggedSlab(Block b) {
        if (b.getType() == Material.WATER) {
            return true;
        }
        if (b.getBlockData() instanceof org.bukkit.block.data.Waterlogged) {
            org.bukkit.block.data.Waterlogged waterlogged = (org.bukkit.block.data.Waterlogged) b.getBlockData();
            if (waterlogged.isWaterlogged()) {
                if (b.getBlockData() instanceof org.bukkit.block.data.type.Slab) {
                    return ((org.bukkit.block.data.type.Slab) b.getBlockData())
                            .getType() == org.bukkit.block.data.type.Slab.Type.BOTTOM;
                }
            }
        }
        return false;
    }

}
