package me.tisleo.autominecart.particles;

import me.tisleo.autominecart.AutoMinecart;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class VehicleParticleHandler {
    private final AutoMinecart plugin;
    
    // Maximum speeds in blocks per second
    private static final double MAX_MINECART_SPEED = 0.4; // Vanilla max speed
    private static final double MAX_BOAT_WATER_SPEED = 0.2;
    private static final double MAX_BOAT_BLUE_ICE_SPEED = 1.5;
    private static final double MAX_BOAT_PACKED_ICE_SPEED = 0.8;
    private static final double MAX_BOAT_ICE_SPEED = 0.5;
    private static final double MIN_SPEED_THRESHOLD = 0.10; // 10% of max speed threshold
    private static final double MAX_LOCATION_OFFSET = 1.0; // Maximum blocks to offset particle spawn ahead of vehicle
    private static final double MINECART_MAX_LOCATION_OFFSET = 0.5; // Smaller max offset for minecarts
    private static final double MIN_LOCATION_OFFSET = 0.2; // Minimum blocks to offset
    private static final double VERTICAL_OFFSET = 0.5; // Offset above the vehicle
    private static final double TICKS_PER_SECOND = 20.0;
    private static final double MAX_SPEED_RATIO = 1.5; // Cap for speed ratio

    private Location lastBoatLocation = null;
    private long lastBoatTime = 0;
    private double lastCalculatedSpeed = 0.0;

    public VehicleParticleHandler(AutoMinecart plugin) {
        this.plugin = plugin;
    }

    /**
     * Calculate actual speed of vehicle
     */
    private double calculateVehicleSpeed(Vehicle vehicle) {
        if (vehicle instanceof Boat) {
            Location currentLoc = vehicle.getLocation();
            long currentTime = System.currentTimeMillis();
            
            if (lastBoatLocation != null && lastBoatLocation.getWorld().equals(currentLoc.getWorld())) {
                double dx = currentLoc.getX() - lastBoatLocation.getX();
                double dz = currentLoc.getZ() - lastBoatLocation.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                double timeDiff = (currentTime - lastBoatTime) / 1000.0;
                
                if (timeDiff > 0) {
                    lastCalculatedSpeed = (distance / timeDiff) / TICKS_PER_SECOND;
                    lastBoatLocation = currentLoc.clone();
                    lastBoatTime = currentTime;
                    return lastCalculatedSpeed;
                }
            }
            
            lastBoatLocation = currentLoc.clone();
            lastBoatTime = currentTime;
            
            if (lastCalculatedSpeed > 0) {
                return lastCalculatedSpeed;
            }
            Vector velocity = vehicle.getVelocity();
            return Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
        }
        return 0.0;
    }

    /**
     * Get the maximum speed for the current vehicle and surface
     */
    private double getMaxSpeed(Vehicle vehicle) {
        if (vehicle instanceof Minecart) {
            return MAX_MINECART_SPEED;
        } else if (vehicle instanceof Boat) {
            Block blockBelow = vehicle.getLocation().clone().subtract(0, 1, 0).getBlock();
            Material surface = blockBelow.getType();
            
            if (surface == Material.BLUE_ICE) {
                return MAX_BOAT_BLUE_ICE_SPEED;
            } else if (surface == Material.PACKED_ICE) {
                return MAX_BOAT_PACKED_ICE_SPEED;
            } else if (surface == Material.ICE) {
                return MAX_BOAT_ICE_SPEED;
            } else {
                return MAX_BOAT_WATER_SPEED;
            }
        }
        return 0.0;
    }

    /**
     * Checks if the vehicle is moving fast enough to spawn particles
     */
    private boolean isAboveSpeedThreshold(Vehicle vehicle) {
        if (vehicle instanceof Minecart) {
            return true; // Minecarts always show particles
        }
        
        double speed = calculateVehicleSpeed(vehicle);
        double maxSpeed = getMaxSpeed(vehicle);
        
        if (maxSpeed <= 0) return false;
        
        double speedRatio = speed / maxSpeed;
        return speedRatio > MIN_SPEED_THRESHOLD;
    }

    /**
     * Calculate particle amount based on vehicle speed
     * Config amount is treated as maximum value
     */
    private int calculateParticleAmount(Vehicle vehicle, int maxAmount) {
        double speed = calculateVehicleSpeed(vehicle);
        double maxSpeed = getMaxSpeed(vehicle);
        
        if (maxSpeed <= 0) return 1;
        
        double speedRatio = Math.min(speed / maxSpeed, 1.0);
        
        return Math.max(1, (int)(1 + (speedRatio * (maxAmount - 1))));
    }

    /**
     * Calculate particle speed based on vehicle speed
     * Config speed is treated as maximum value
     */
    private double calculateParticleSpeed(Vehicle vehicle, double maxParticleSpeed) {
        double speed = calculateVehicleSpeed(vehicle);
        double maxSpeed = getMaxSpeed(vehicle);
        
        if (maxSpeed <= 0) return maxParticleSpeed * 0.1;
        
        double speedRatio = Math.min(speed / maxSpeed, 1.0);
        
        return maxParticleSpeed * (0.1 + (speedRatio * 0.9));
    }

    /**
     * Calculate the adjusted spawn location based on vehicle speed and direction
     */
    private Location calculateSpawnLocation(Vehicle vehicle, Location baseLocation) {
        double speed = calculateVehicleSpeed(vehicle);
        double maxSpeed = getMaxSpeed(vehicle);
        
        if (maxSpeed <= 0 || speed <= 0) return baseLocation;

        Vector velocity = vehicle.getVelocity();
        Vector direction;
        
        if (velocity.lengthSquared() > 0.0001) {
            direction = velocity.clone().normalize();
        } else {
            float yaw = vehicle.getLocation().getYaw();
            double radiansYaw = Math.toRadians(yaw);
            direction = new Vector(-Math.sin(radiansYaw), 0, Math.cos(radiansYaw));
        }

        double speedRatio = Math.min(speed / maxSpeed, MAX_SPEED_RATIO);
        double maxOffset = (vehicle instanceof Minecart) ? MINECART_MAX_LOCATION_OFFSET : MAX_LOCATION_OFFSET;
        double offset = MIN_LOCATION_OFFSET + (speedRatio * (maxOffset - MIN_LOCATION_OFFSET));

        Location spawnLoc = vehicle.getLocation().clone();
        spawnLoc.add(0, VERTICAL_OFFSET, 0);
        
        Vector offsetVector;
        if (vehicle instanceof Minecart) {
            offsetVector = direction.multiply(-offset);
        } else {
            offsetVector = direction.multiply(offset);
        }
        
        spawnLoc.add(offsetVector);
        return spawnLoc;
    }

    /**
     * Spawns particles for a vehicle based on its type and environment
     * @param vehicle The vehicle to spawn particles for
     * @param player The player in the vehicle
     */
    public void spawnVehicleParticles(Vehicle vehicle, Player player) {
        if (!plugin.getConfig().getBoolean("particles.enabled")) return;
        
        if (!player.hasPermission("autominecart.particles")) return;

        Location loc = vehicle.getLocation();
        Block block = loc.getBlock();
        
        if (!isAboveSpeedThreshold(vehicle)) {
            return;
        }

        if (vehicle instanceof Boat) {
            Location particleLoc = loc.clone();
            
            Block blockBelow = loc.clone().subtract(0, 1, 0).getBlock();
            boolean onIce = blockBelow.getType().name().contains("ICE") || block.getType().name().contains("ICE");

            if (loc.getBlock().isLiquid()) {
                particleLoc.add(0, 0.5, 0);
            }

            if (onIce && (player.isOp() || player.hasPermission("autominecart.particles.environment"))) {
                if (plugin.getConfig().getBoolean("particles.environment.ice_path.enabled")) {
                    String icePathEffect = "particles.environment.ice_path";
                    spawnParticleEffect(player, particleLoc, icePathEffect);
                }
            } else {
                String boatEffectPath = "particles.default_style.boat";
                spawnParticleEffect(player, particleLoc, boatEffectPath);
            }
        } else if (vehicle instanceof Minecart) {
            String minecartEffectPath = "particles.default_style.minecart";
            spawnParticleEffect(player, loc, minecartEffectPath);
            
            if (plugin.getConfig().getBoolean("particles.environment.underground.enabled") && 
                (player.isOp() || player.hasPermission("autominecart.particles.environment")) &&
                loc.clone().add(0, 2, 0).getBlock().getType().isSolid()) {
                String undergroundEffectPath = "particles.environment.underground";
                spawnParticleEffect(player, loc, undergroundEffectPath);
            }
        }

        if (plugin.getConfig().getBoolean("particles.environment.biomes.enabled") && 
            (player.isOp() || player.hasPermission("autominecart.particles.environment"))) {
            String biomeName = block.getBiome().name();
            ConfigurationSection biomesSection = plugin.getConfig().getConfigurationSection("particles.environment.biomes");
            
            if (biomesSection != null) {
                for (String category : biomesSection.getKeys(false)) {
                    if (category.equals("enabled")) continue;
                    
                    if (biomeName.toUpperCase().contains(category)) {
                        String biomeEffectPath = "particles.environment.biomes." + category;
                        spawnParticleEffect(player, loc, biomeEffectPath);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Spawns a particle effect at a location using config settings
     * @param player The player to show particles to (unused now that particles are shown to all nearby players)
     * @param location The location to spawn particles at
     * @param configPath The config path for particle settings
     */
    private void spawnParticleEffect(Player player, Location location, String configPath) {
        if (!plugin.getConfig().contains(configPath + ".effect")) {
            return;
        }

        try {
            String effectName = plugin.getConfig().getString(configPath + ".effect");
            Particle effect = Particle.valueOf(effectName);
            Vehicle vehicle = (Vehicle) player.getVehicle();
            
            if (vehicle != null) {
                if (vehicle instanceof Minecart) {
                    // Use config values for minecarts
                    Location spawnLoc = vehicle.getLocation().clone().add(0, VERTICAL_OFFSET, 0);
                    Vector direction = getMinecartDirection(vehicle);
                    
                    // Get values from config, with defaults if not specified
                    int amount = plugin.getConfig().getInt(configPath + ".amount", 3);
                    double speed = plugin.getConfig().getDouble(configPath + ".speed", 0.05);
                    double spreadX = plugin.getConfig().getDouble(configPath + ".offset_x", 0.2);
                    double spreadY = plugin.getConfig().getDouble(configPath + ".offset_y", 0.1);
                    double spreadZ = plugin.getConfig().getDouble(configPath + ".offset_z", 0.2);
                    double offset = plugin.getConfig().getDouble(configPath + ".particle_offset", 0.5);
                    
                    spawnLoc.add(direction.multiply(-offset));
                    
                    location.getWorld().spawnParticle(
                        effect,
                        spawnLoc.getX(),
                        spawnLoc.getY(),
                        spawnLoc.getZ(),
                        amount,
                        spreadX,
                        spreadY,
                        spreadZ,
                        speed
                    );
                } else {
                    // Dynamic calculations only for boats
                    double speed = calculateVehicleSpeed(vehicle);
                    double maxSpeed = getMaxSpeed(vehicle);
                    double speedRatio = speed / maxSpeed;
                    
                    if (speedRatio <= MIN_SPEED_THRESHOLD) {
                        return;
                    }
                    
                    int maxAmount = plugin.getConfig().getInt(configPath + ".amount", 1);
                    double maxParticleSpeed = plugin.getConfig().getDouble(configPath + ".speed", 0.1);
                    int adjustedAmount = calculateParticleAmount(vehicle, maxAmount);
                    double adjustedSpeed = calculateParticleSpeed(vehicle, maxParticleSpeed);
                    
                    double spreadX = plugin.getConfig().getDouble(configPath + ".offset_x", 0.2);
                    double spreadY = plugin.getConfig().getDouble(configPath + ".offset_y", 0.1);
                    double spreadZ = plugin.getConfig().getDouble(configPath + ".offset_z", 0.2);
                    
                    Location spawnLoc = calculateSpawnLocation(vehicle, location);
                    
                    location.getWorld().spawnParticle(
                        effect,
                        spawnLoc.getX(),
                        spawnLoc.getY(),
                        spawnLoc.getZ(),
                        adjustedAmount,
                        spreadX,
                        spreadY,
                        spreadZ,
                        adjustedSpeed
                    );
                }
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle effect in config: " + configPath);
        }
    }

    private Vector getMinecartDirection(Vehicle vehicle) {
        float yaw = vehicle.getLocation().getYaw();
        double radiansYaw = Math.toRadians(yaw);
        return new Vector(-Math.sin(radiansYaw), 0, Math.cos(radiansYaw));
    }
}
