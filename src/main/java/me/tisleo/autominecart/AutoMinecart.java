package me.tisleo.autominecart;

import me.tisleo.autominecart.commands.CommandToggleBoat;
import me.tisleo.autominecart.commands.CommandToggleCart;
import me.tisleo.autominecart.commands.CommandToggleParticles;
import me.tisleo.autominecart.config.ConfigurationManager;
import me.tisleo.autominecart.listeners.BoatLeaveHandler;
import me.tisleo.autominecart.listeners.BoatMoveHandler;
import me.tisleo.autominecart.listeners.IceClickHandler;
import me.tisleo.autominecart.listeners.MinecartLeaveHandler;
import me.tisleo.autominecart.listeners.PlayerJoinHandler;
import me.tisleo.autominecart.listeners.RailClickHandler;
import me.tisleo.autominecart.listeners.VehicleMoveHandler;
import me.tisleo.autominecart.listeners.WaterClickHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

public final class AutoMinecart extends JavaPlugin {

    /**
     * A list of players currently inside an AutoMinecart
     */
    private final ArrayList<Player> minecartUsers = new ArrayList<>();
    private final ArrayList<Player> boatUsers = new ArrayList<>();

    private static AutoMinecart instance;
    private ConfigurationManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize configuration manager
        configManager = new ConfigurationManager(this);

        try {
            PlayerConfig.initPlayerConfig();
            PlayerConfig.getPlayersFileConfig().options().copyDefaults(true);
            PlayerConfig.savePlayerConfig();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "There was an error creating the AutoMinecart player file. " +
                    "Please try to reload the plugin/server, or contact the developer under the 'Help' section at https://github.com/TisLeo/AutoMinecart");
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

    public void addMinecartUser(Player p) {
        minecartUsers.add(p);
    }

    public void addBoatUser(Player p) {
        boatUsers.add(p);
    }

    public ArrayList<Player> getMinecartUsers() {
        return this.minecartUsers;
    }

    public ArrayList<Player> getBoatUsers() {
        return this.boatUsers;
    }

    public void removeMinecartUser(Player p) {
        minecartUsers.remove(p);
    }

    public void removeBoatUser(Player p) {
        boatUsers.remove(p);
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
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "AutoMinecart has been disabled!");
    }
}
