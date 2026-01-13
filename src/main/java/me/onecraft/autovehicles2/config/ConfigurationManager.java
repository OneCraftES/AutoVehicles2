package me.onecraft.autovehicles2.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class ConfigurationManager {
    private final Plugin plugin;
    private FileConfiguration config;
    private File configFile;
    private static final String CURRENT_VERSION = "2.2.1";

    public ConfigurationManager(Plugin plugin) {
        this.plugin = plugin;
        this.loadConfig();
    }

    public void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }

        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        checkConfigVersion();
    }

    private void checkConfigVersion() {
        String version = config.getString("config_version", "1.0.0");
        if (!version.equals(CURRENT_VERSION)) {
            plugin.getLogger().info("Updating config.yml from version " + version + " to " + CURRENT_VERSION);
            updateConfig(version);
        }
    }

    private void updateConfig(String oldVersion) {
        // Backup old config
        File backupFile = new File(plugin.getDataFolder(), "config_backup_" + oldVersion + ".yml");
        try {
            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not create config backup", e);
            return;
        }

        // Load default config from resources
        InputStream defaultConfig = plugin.getResource("config.yml");
        if (defaultConfig == null) {
            plugin.getLogger().severe("Could not load default config from jar");
            return;
        }

        // Create new config with default values
        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(defaultConfig));

        // Transfer old values that should be kept
        transferOldValues(oldVersion, config, newConfig);

        // Save the updated config
        try {
            newConfig.save(configFile);
            plugin.getLogger().info("Successfully updated config.yml to version " + CURRENT_VERSION);
            plugin.getLogger().info("A backup of your old config has been saved as: " + backupFile.getName());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save updated config", e);
        }

        // Reload the config
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void transferOldValues(String oldVersion, FileConfiguration oldConfig, FileConfiguration newConfig) {
        // Transfer disabled worlds
        if (oldConfig.contains("disabled_worlds")) {
            newConfig.set("disabled_worlds", oldConfig.getStringList("disabled_worlds"));
        }

        // Transfer settings
        if (oldConfig.contains("settings")) {
            for (String key : oldConfig.getConfigurationSection("settings").getKeys(false)) {
                newConfig.set("settings." + key, oldConfig.get("settings." + key));
            }
        }

        // Transfer particle settings
        if (oldConfig.contains("particles")) {
            if (oldConfig.contains("particles.enabled")) {
                newConfig.set("particles.enabled", oldConfig.getBoolean("particles.enabled"));
            }
            if (oldConfig.contains("particles.force_on_minimal")) {
                newConfig.set("particles.force_on_minimal", oldConfig.getBoolean("particles.force_on_minimal"));
            }
            if (oldConfig.contains("particles.forced_display_ratio")) {
                newConfig.set("particles.forced_display_ratio", oldConfig.getDouble("particles.forced_display_ratio"));
            }

            // Transfer default styles
            if (oldConfig.contains("particles.default_style")) {
                for (String type : oldConfig.getConfigurationSection("particles.default_style").getKeys(false)) {
                    String path = "particles.default_style." + type;
                    newConfig.set(path, oldConfig.getConfigurationSection(path));
                }
            }

            // Transfer environment settings
            if (oldConfig.contains("particles.environment")) {
                // Underground
                if (oldConfig.contains("particles.environment.underground")) {
                    newConfig.set("particles.environment.underground",
                            oldConfig.getConfigurationSection("particles.environment.underground"));
                }
                // Ice path
                if (oldConfig.contains("particles.environment.ice_path")) {
                    newConfig.set("particles.environment.ice_path",
                            oldConfig.getConfigurationSection("particles.environment.ice_path"));
                }

                // Biomes
                if (oldConfig.contains("particles.environment.biomes")) {
                    for (String key : oldConfig.getConfigurationSection("particles.environment.biomes")
                            .getKeys(false)) {
                        // Check specifically for enabled flag or biome keys
                        String path = "particles.environment.biomes." + key;
                        newConfig.set(path, oldConfig.get(path));
                    }
                }
                // Backwards compatibility for old "particles.biomes" path
                else if (oldConfig.contains("particles.biomes")) {
                    for (String biome : oldConfig.getConfigurationSection("particles.biomes").getKeys(false)) {
                        String oldPath = "particles.biomes." + biome;
                        String newPath = "particles.environment.biomes." + biome;
                        if (oldConfig.contains(oldPath)) {
                            newConfig.set(newPath, oldConfig.getConfigurationSection(oldPath));
                        }
                    }
                }
            }
        }

        // Version-specific migrations
        switch (oldVersion) {
            case "1.0.0":
                // Handle migration from 1.0.0 to 1.1.0
                migrateFrom100To110(oldConfig, newConfig);
                // Fall through to next version
            case "1.1.0":
                // Handle migration from 1.1.0 to 1.2.0
                migrateFrom110To120(oldConfig, newConfig);
                // Fall through to next version
            case "1.2.0":
                // Handle migration from 1.2.0 to 1.3.0
                // Handle migration from 1.2.0 to 1.3.0
                migrateFrom120To130(oldConfig, newConfig);
                // Fall through
            case "1.3.0":
            case "2.0.0":
            case "2.1.0":
                // No specific migration needed, just transfer values
                break;
        }
    }

    private void migrateFrom100To110(FileConfiguration oldConfig, FileConfiguration newConfig) {
        // Add any migration logic for 1.0.0 to 1.1.0
    }

    private void migrateFrom110To120(FileConfiguration oldConfig, FileConfiguration newConfig) {
        // Add any migration logic for 1.1.0 to 1.2.0
    }

    private void migrateFrom120To130(FileConfiguration oldConfig, FileConfiguration newConfig) {
        // Migrate to new particle system if old settings exist
        if (oldConfig.contains("particles")) {
            // Keep existing particle settings and add new biomes with default values
            // The new biomes will be automatically added from the default config
            plugin.getLogger().info("Migrating particle settings to version 1.3.0");
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }

    public void reloadConfig() {
        loadConfig();
    }
}
