package me.tisleo.autominecart.particles;

import me.tisleo.autominecart.AutoMinecart;
import me.tisleo.autominecart.PlayerConfig;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Particle;

public class VehicleParticleHandler {
    private final AutoMinecart plugin;

    public VehicleParticleHandler(AutoMinecart plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the particle effect from string name
     * @param effectName The effect name from config
     * @return The Particle or CLOUD if not found
     */
    private Particle getParticleEffect(String effectName) {
        try {
            return Particle.valueOf(effectName.toUpperCase());
        } catch (Exception e) {
            return Particle.CLOUD;
        }
    }

    /**
     * Spawns particles for a vehicle based on its type and environment
     * @param vehicle The vehicle to spawn particles for
     * @param player The player in the vehicle
     */
    public void spawnVehicleParticles(Vehicle vehicle, Player player) {
        if (!plugin.getConfig().getBoolean("particles.enabled") || 
            !player.hasPermission("autovehicles.particles") ||
            !PlayerConfig.getPlayersFileConfig().getBoolean("players." + player.getUniqueId() + ".particles.enabled", true)) {
            return;
        }

        // Get vehicle type specific particles
        String vehicleType = (vehicle instanceof Minecart) ? "minecart" : "boat";
        String effectName = plugin.getConfig().getString("particles.default_style." + vehicleType + ".effect", "CLOUD");
        int amount = plugin.getConfig().getInt("particles.default_style." + vehicleType + ".amount", 1);

        // Spawn base vehicle particles
        Location loc = vehicle.getLocation();
        Particle effect = getParticleEffect(effectName);
        if (effect != null) {
            player.spawnParticle(effect, loc, amount, 0, 0, 0, 0);
        }

        // Add environmental effects
        addEnvironmentalEffects(vehicle, player);
    }

    /**
     * Adds additional particle effects based on the environment
     * @param vehicle The vehicle
     * @param player The player
     */
    private void addEnvironmentalEffects(Vehicle vehicle, Player player) {
        Location loc = vehicle.getLocation();
        Block block = loc.getBlock();

        // Underground detection (if block above is solid)
        if (plugin.getConfig().getBoolean("particles.environment.underground.enabled") &&
            loc.clone().add(0, 2, 0).getBlock().getType().isSolid()) {
            String effectName = plugin.getConfig().getString("particles.environment.underground.effect", "SMOKE_NORMAL");
            int amount = plugin.getConfig().getInt("particles.environment.underground.amount", 1);
            
            Particle effect = getParticleEffect(effectName);
            if (effect != null) {
                player.spawnParticle(effect, loc, amount, 0, 0, 0, 0);
            }
        }

        // Ice path detection for boats
        if (vehicle instanceof Boat && 
            plugin.getConfig().getBoolean("particles.environment.ice_path.enabled") &&
            block.getType().name().contains("ICE")) {
            String effectName = plugin.getConfig().getString("particles.environment.ice_path.effect", "SNOWFLAKE");
            int amount = plugin.getConfig().getInt("particles.environment.ice_path.amount", 1);
            
            Particle effect = getParticleEffect(effectName);
            if (effect != null) {
                player.spawnParticle(effect, loc, amount, 0, 0, 0, 0);
            }
        }

        // Biome-specific effects
        if (plugin.getConfig().getBoolean("particles.environment.biomes.enabled")) {
            String biomeName = block.getBiome().name();
            ConfigurationSection biomesSection = plugin.getConfig().getConfigurationSection("particles.environment.biomes");
            
            if (biomesSection != null) {
                // Check each biome category in config
                for (String category : biomesSection.getKeys(false)) {
                    if (category.equals("enabled")) continue; // Skip the enabled flag
                    
                    // If the biome name contains any part of the category (case insensitive)
                    if (biomeName.toUpperCase().contains(category)) {
                        String path = "particles.environment.biomes." + category;
                        String effectName = plugin.getConfig().getString(path + ".effect");
                        int amount = plugin.getConfig().getInt(path + ".amount", 1);
                        
                        Particle effect = getParticleEffect(effectName);
                        if (effect != null) {
                            player.spawnParticle(effect, loc, amount, 0, 0, 0, 0);
                            break; // Use first matching biome category
                        }
                    }
                }
            }
        }
    }
}
