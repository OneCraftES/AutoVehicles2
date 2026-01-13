package me.onecraft.autovehicles2.particles;

import me.onecraft.autovehicles2.AutoVehicles2;
import me.onecraft.autovehicles2.PlayerConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VehicleParticleHandler {
    private final AutoVehicles2 plugin;

    // Constants
    private static final double MAX_MINECART_SPEED = 0.4;
    private static final double MAX_BOAT_WATER_SPEED = 0.2;
    private static final double MAX_BOAT_BLUE_ICE_SPEED = 1.5;
    private static final double MAX_BOAT_PACKED_ICE_SPEED = 0.8;
    private static final double MAX_BOAT_ICE_SPEED = 0.5;
    private static final double MIN_SPEED_THRESHOLD = 0.10;
    private static final double MAX_LOCATION_OFFSET = 1.0;
    private static final double MINECART_MAX_LOCATION_OFFSET = 0.5;
    private static final double MIN_LOCATION_OFFSET = 0.2;
    private static final double VERTICAL_OFFSET = 0.5;
    private static final double TICKS_PER_SECOND = 20.0;
    private static final double MAX_SPEED_RATIO = 1.5;

    // Per-vehicle state tracking
    private static class VehicleState {
        Location lastLocation;
        long lastTime;
        double lastSpeed;

        VehicleState(Location loc, long time) {
            this.lastLocation = loc.clone();
            this.lastTime = time;
            this.lastSpeed = 0.0;
        }
    }

    private final Map<UUID, VehicleState> vehicleStates = new HashMap<>();

    // Config Caching
    private static class ParticleParams {
        final Particle effect;
        final int amount;
        final double speed;
        final double offsetX;
        final double offsetY;
        final double offsetZ;
        final Double particleOffset; // Unique to minecarts

        ParticleParams(Particle effect, int amount, double speed, double offsetX, double offsetY, double offsetZ,
                Double particleOffset) {
            this.effect = effect;
            this.amount = amount;
            this.speed = speed;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.particleOffset = particleOffset;
        }
    }

    private final Map<String, ParticleParams> configCache = new HashMap<>();

    public VehicleParticleHandler(AutoVehicles2 plugin) {
        this.plugin = plugin;
    }

    /**
     * Spawns particles for a vehicle based on its type and environment
     */
    public void spawnVehicleParticles(Vehicle vehicle, Player player) {
        if (!plugin.getConfig().getBoolean("particles.enabled", true))
            return;
        if (!player.hasPermission("autovehicles2.particles"))
            return;
        if (!PlayerConfig.getPlayersFileConfig().getBoolean("players." + player.getUniqueId() + ".particles.enabled",
                true))
            return;

        double speed = updateAndGetVehicleSpeed(vehicle);
        double maxSpeed = getMaxSpeed(vehicle);

        if (maxSpeed <= 0)
            return;

        double speedRatio = (vehicle instanceof Minecart) ? 1.0 : (speed / maxSpeed);
        if (speedRatio <= MIN_SPEED_THRESHOLD && !(vehicle instanceof Minecart))
            return;

        Location loc = vehicle.getLocation();
        Block block = loc.getBlock();

        Block blockBelow = loc.clone().subtract(0, 1, 0).getBlock();
        BlockData blockData = blockBelow.getBlockData();

        // 1. Core Vehicle Particles
        if (vehicle instanceof Boat) {
            handleBoatParticles(vehicle, player, speed, maxSpeed, loc, block, blockData);
        } else if (vehicle instanceof Minecart) {
            handleMinecartParticles(vehicle, player, loc, blockData);
        }

        // 2. Biome Particles
        if (plugin.getConfig().getBoolean("particles.environment.biomes.enabled", true) &&
                (player.isOp() || player.hasPermission("autovehicles2.particles.environment"))) {
            handleBiomeParticles(vehicle, block, loc, blockData);
        }
    }

    private void handleBoatParticles(Vehicle vehicle, Player player, double speed, double maxSpeed, Location loc,
            Block block, BlockData blockData) {
        boolean onIce = blockData.getMaterial().name().contains("ICE") || block.getType().name().contains("ICE");

        if (onIce && (player.isOp() || player.hasPermission("autovehicles2.particles.environment"))) {
            if (plugin.getConfig().getBoolean("particles.environment.ice_path.enabled", true)) {
                spawnDynamicParticle(vehicle, "particles.environment.ice_path", loc, speed, maxSpeed, blockData);
            }
        } else {
            spawnDynamicParticle(vehicle, "particles.default_style.boat", loc, speed, maxSpeed, blockData);
        }
    }

    private void handleMinecartParticles(Vehicle vehicle, Player player, Location loc, BlockData blockData) {
        spawnStaticParticle(vehicle, "particles.default_style.minecart", loc, blockData);

        if (plugin.getConfig().getBoolean("particles.environment.underground.enabled", true) &&
                (player.isOp() || player.hasPermission("autovehicles2.particles.environment")) &&
                loc.clone().add(0, 2, 0).getBlock().getType().isSolid()) {
            spawnStaticParticle(vehicle, "particles.environment.underground", loc, blockData);
        }
    }

    private void handleBiomeParticles(Vehicle vehicle, Block block, Location loc, BlockData blockData) {
        String biomeName = block.getBiome().name();
        ConfigurationSection biomesSection = plugin.getConfig().getConfigurationSection("particles.environment.biomes");

        if (biomesSection != null) {
            for (String category : biomesSection.getKeys(false)) {
                if (category.equals("enabled"))
                    continue;
                if (biomeName.contains(category)) {
                    if (vehicle instanceof Boat) {
                        double speed = getCalculatedSpeed(vehicle);
                        double maxSpeed = getMaxSpeed(vehicle);
                        spawnDynamicParticle(vehicle, "particles.environment.biomes." + category, loc, speed, maxSpeed,
                                blockData);
                    } else {
                        spawnStaticParticle(vehicle, "particles.environment.biomes." + category, loc, blockData);
                    }
                    break;
                }
            }
        }
    }

    private double updateAndGetVehicleSpeed(Vehicle vehicle) {
        if (vehicle instanceof Minecart)
            return MAX_MINECART_SPEED;

        UUID id = vehicle.getUniqueId();
        Location currentLoc = vehicle.getLocation();
        long currentTime = System.currentTimeMillis();

        VehicleState state = vehicleStates.computeIfAbsent(id, k -> new VehicleState(currentLoc, currentTime));

        if (state.lastLocation.getWorld().equals(currentLoc.getWorld())) {
            double distanceSq = currentLoc.distanceSquared(state.lastLocation);
            if (distanceSq > 0.0001) {
                double timeDiff = (currentTime - state.lastTime) / 1000.0;
                if (timeDiff > 0) {
                    state.lastSpeed = (Math.sqrt(distanceSq) / timeDiff) / TICKS_PER_SECOND;
                    state.lastLocation = currentLoc.clone();
                    state.lastTime = currentTime;
                    return state.lastSpeed;
                }
            }
        } else {
            state.lastLocation = currentLoc.clone();
            state.lastTime = currentTime;
        }

        if (state.lastSpeed > 0)
            return state.lastSpeed;

        Vector velocity = vehicle.getVelocity();
        return Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
    }

    private double getCalculatedSpeed(Vehicle vehicle) {
        if (vehicle instanceof Minecart)
            return MAX_MINECART_SPEED;
        VehicleState state = vehicleStates.get(vehicle.getUniqueId());
        return state != null ? state.lastSpeed : 0.0;
    }

    private double getMaxSpeed(Vehicle vehicle) {
        if (vehicle instanceof Minecart)
            return MAX_MINECART_SPEED;

        Material surface = vehicle.getLocation().clone().subtract(0, 1, 0).getBlock().getType();
        switch (surface) {
            case BLUE_ICE:
                return MAX_BOAT_BLUE_ICE_SPEED;
            case PACKED_ICE:
                return MAX_BOAT_PACKED_ICE_SPEED;
            case ICE:
                return MAX_BOAT_ICE_SPEED;
            default:
                return MAX_BOAT_WATER_SPEED;
        }
    }

    private ParticleParams getParams(String path) {
        return configCache.computeIfAbsent(path, p -> {
            ConfigurationSection section = plugin.getConfig().getConfigurationSection(p);
            if (section == null)
                return null;

            try {
                String effectName = section.getString("effect");
                if (effectName == null)
                    return null;

                return new ParticleParams(
                        Particle.valueOf(effectName.toUpperCase()),
                        section.getInt("amount", 1),
                        section.getDouble("speed", 0.1),
                        section.getDouble("offset_x", 0.2),
                        section.getDouble("offset_y", 0.1),
                        section.getDouble("offset_z", 0.2),
                        section.contains("particle_offset") ? section.getDouble("particle_offset") : null);
            } catch (Exception e) {
                return null;
            }
        });
    }

    private void spawnDynamicParticle(Vehicle vehicle, String path, Location location, double speed, double maxSpeed,
            BlockData blockData) {
        ParticleParams params = getParams(path);
        if (params == null)
            return;

        double speedRatio = Math.min(speed / maxSpeed, 1.0);
        int adjustedAmount = Math.max(1, (int) (1 + (speedRatio * (params.amount - 1))));
        double adjustedSpeed = params.speed * (0.1 + (speedRatio * 0.9));

        Location spawnLoc = calculateSpawnLoc(vehicle, speed, maxSpeed);

        boolean force = plugin.getConfig().getBoolean("particles.force_on_minimal", true);
        if (force) {
            adjustedAmount = Math.max(1, adjustedAmount / 4);
        }

        if (params.effect.getDataType() == BlockData.class) {
            if (blockData.getMaterial().isAir()) {
                blockData = Material.STONE.createBlockData();
            }
            location.getWorld().spawnParticle(
                    params.effect, spawnLoc, adjustedAmount,
                    params.offsetX, params.offsetY, params.offsetZ, adjustedSpeed, blockData, force);
        } else {
            location.getWorld().spawnParticle(
                    params.effect, spawnLoc, adjustedAmount,
                    params.offsetX, params.offsetY, params.offsetZ, adjustedSpeed, null, force);
        }
    }

    private void spawnStaticParticle(Vehicle vehicle, String path, Location location, BlockData blockData) {
        ParticleParams params = getParams(path);
        if (params == null)
            return;

        Location spawnLoc = vehicle.getLocation().clone().add(0, VERTICAL_OFFSET, 0);
        if (params.particleOffset != null) {
            Vector direction = getDirection(vehicle);
            spawnLoc.add(direction.multiply(-params.particleOffset));
        }

        boolean force = plugin.getConfig().getBoolean("particles.force_on_minimal", true);
        int amount = params.amount;
        if (force) {
            amount = Math.max(1, amount / 4);
        }

        if (params.effect.getDataType() == BlockData.class) {
            if (blockData.getMaterial().isAir()) {
                blockData = Material.STONE.createBlockData();
            }
            location.getWorld().spawnParticle(
                    params.effect, spawnLoc, amount,
                    params.offsetX, params.offsetY, params.offsetZ, params.speed, blockData, force);
        } else {
            location.getWorld().spawnParticle(
                    params.effect, spawnLoc, amount,
                    params.offsetX, params.offsetY, params.offsetZ, params.speed, null, force);
        }
    }

    private Location calculateSpawnLoc(Vehicle vehicle, double speed, double maxSpeed) {
        Vector velocity = vehicle.getVelocity();
        Vector direction = (velocity.lengthSquared() > 0.0001) ? velocity.clone().normalize() : getDirection(vehicle);

        double speedRatio = Math.min(speed / maxSpeed, MAX_SPEED_RATIO);
        double maxOffset = (vehicle instanceof Minecart) ? MINECART_MAX_LOCATION_OFFSET : MAX_LOCATION_OFFSET;
        double offset = MIN_LOCATION_OFFSET + (speedRatio * (maxOffset - MIN_LOCATION_OFFSET));

        Location spawnLoc = vehicle.getLocation().clone().add(0,
                (vehicle.getLocation().getBlock().isLiquid() ? 0.5 : 0.0) + VERTICAL_OFFSET, 0);
        spawnLoc.add(direction.multiply(vehicle instanceof Minecart ? -offset : offset));

        return spawnLoc;
    }

    private Vector getDirection(Vehicle vehicle) {
        double radiansYaw = Math.toRadians(vehicle.getLocation().getYaw());
        return new Vector(-Math.sin(radiansYaw), 0, Math.cos(radiansYaw));
    }

    public void removeVehicleState(UUID id) {
        vehicleStates.remove(id);
    }

    public void clearCache() {
        configCache.clear();
    }
}
