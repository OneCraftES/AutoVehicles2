package me.onecraft.autovehicles2;

import me.onecraft.autovehicles2.commands.CommandToggleBoat;
import me.onecraft.autovehicles2.commands.CommandToggleCart;
import me.onecraft.autovehicles2.commands.CommandToggleParticles;
import me.onecraft.autovehicles2.config.ConfigurationManager;
import me.onecraft.autovehicles2.listeners.BoatLeaveHandler;
import me.onecraft.autovehicles2.listeners.BoatMoveHandler;
import me.onecraft.autovehicles2.listeners.IceClickHandler;
import me.onecraft.autovehicles2.listeners.MinecartLeaveHandler;
import me.onecraft.autovehicles2.listeners.PlayerJoinHandler;
import me.onecraft.autovehicles2.listeners.RailClickHandler;
import me.onecraft.autovehicles2.listeners.VehicleMoveHandler;
import me.onecraft.autovehicles2.listeners.WaterClickHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;
import java.util.logging.Level;

public final class AutoVehicles2 extends JavaPlugin {

    private ConfigurationManager configManager;
    private NamespacedKey vehicleKey;
    private me.onecraft.autovehicles2.particles.VehicleParticleHandler particleHandler;

    @Override
    public void onEnable() {
        vehicleKey = new NamespacedKey(this, "is_autovehicle");
        particleHandler = new me.onecraft.autovehicles2.particles.VehicleParticleHandler(this);

        // Initialize configuration manager
        configManager = new ConfigurationManager(this);

        try {
            PlayerConfig.initPlayerConfig();
            PlayerConfig.getPlayersFileConfig().options().copyDefaults(true);
            PlayerConfig.savePlayerConfig();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "There was an error creating the AutoVehicles2 player file. " +
                    "Please try to reload the plugin/server, or contact the developer under the 'Help' section at https://github.com/OneCraft/AutoVehicles2");
            getPluginLoader().disablePlugin(this);
        }

        registerEvents();
        registerCommands();
    }

    /**
     * Registers event listeners
     */
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new RailClickHandler(this), this);
        getServer().getPluginManager().registerEvents(new VehicleMoveHandler(this), this);
        getServer().getPluginManager().registerEvents(new BoatMoveHandler(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinHandler(), this);
        getServer().getPluginManager().registerEvents(new MinecartLeaveHandler(this), this);
        getServer().getPluginManager().registerEvents(new WaterClickHandler(this), this);
        getServer().getPluginManager().registerEvents(new IceClickHandler(this), this);
        getServer().getPluginManager().registerEvents(new BoatLeaveHandler(this), this);
    }

    /**
     * Sets executors for commands
     */
    private void registerCommands() {
        getCommand("togglecart").setExecutor(new CommandToggleCart());
        getCommand("toggleboat").setExecutor(new CommandToggleBoat());
        getCommand("toggleparticles").setExecutor(new CommandToggleParticles());
    }

    public NamespacedKey getVehicleKey() {
        return vehicleKey;
    }

    public me.onecraft.autovehicles2.particles.VehicleParticleHandler getParticleHandler() {
        return particleHandler;
    }

    @Override
    public FileConfiguration getConfig() {
        return configManager.getConfig();
    }

    @Override
    public void saveConfig() {
        configManager.saveConfig();
    }

    @Override
    public void reloadConfig() {
        configManager.reloadConfig();
        if (particleHandler != null) {
            particleHandler.clearCache();
        }
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "AutoVehicles2 has been disabled!");
    }
}
