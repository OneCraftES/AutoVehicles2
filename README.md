# AutoVehicles

A fork of [AutoMinecart by Tisleo](https://github.com/TisLeo/AutoMinecart) that enhances vehicle mechanics with custom particle effects and expanded boat functionality.

In our implementation, we use [ExtraContexts](https://github.com/LuckPerms/ExtraContexts) plugin with [LuckPerms](https://github.com/LuckPerms/LuckPerms) to allow the permission `autominecart.use` only in concrete regions of [WorldGuard](https://github.com/EngineHub/WorldGuard).

Originally created to add boat functionality and region-specific permissions for the OneCraft server, this fork has evolved to include a rich particle system.

## Features

### Vehicle Mechanics
- Allows players to right-click a rail (excluding Activator Rails), different types of ice or the blocks under blocks of water with an empty hand and get teleported into an automatically-spawned minecart or boat.
- Minecarts and boats will automatically be deleted once a player gets out
- Add/remove disabled worlds in the `config.yml` file
- Use the permission `autominecart.use` to allow non-OP players to use the plugin
- Use the `/togglecart` command to toggle the functionality on or off for yourself
- **Note:** due to how Minecraft handles powered rails, if travelling over one, *it must be powered on*. Additionally, having too many corner rails close together may cause the vehicle to fly off-track due to speed.
- Support for ice paths and water routes

### Particle Effects System (New in 1.3.0!)
- Dynamic particle effects for vehicles
- Biome-specific visual effects
- Environmental condition detection
- Fully customizable particle settings:
  - Different effects for minecarts and boats
  - Adjustable particle speed and spread
  - Biome-specific particle configurations
  - Underground and ice path detection

### Configuration Management
- Automatic config version tracking
- Safe config updates with backups
- Preserves user customizations during updates
- Supports all Minecraft biomes:
  - Desert & Savanna biomes
  - All Nether biome variants
  - All End biome variants
  - Ocean variants
  - Snow biome variants
  - Jungle variants
  - And more!

### **TO-DO / Planned Features**
The following features are planned or under consideration for future updates:

#### Transportation System Enhancements
- [ ] Station System
  - Named stations with teleport points
  - GUI menu for selecting destinations
  - Integration with BlueMap/Dynmap to show routes and stations
  - Statistics tracking for most used routes and stations

#### Visual and Environmental Features
- [x] Enhanced Visual Feedback
  - Particle effects for different path types
  - Biome-specific visual effects
  - Underground/cave detection with appropriate effects
  - Path visualization when holding specific items

#### Performance and Optimization
- [ ] Server Performance Improvements
  - Smart chunk loading optimization for long routes
  - Vehicle cleanup system for abandoned vehicles
  - Configurable render distance for vehicles
  - Performance monitoring and statistics

#### Administrative Tools
- [ ] Management Features
  - Station and route creation tools for admins
  - Usage statistics dashboard
  - Debug tools for path verification
  - Configuration options for particle effects and visuals

## Commands
- `/togglecart` - Toggle minecart features
- `/toggleboat` - Toggle boat features
- `/toggleparticles` - Toggle particle effects for your vehicles

## Permissions
- `autominecart.use` - Access to basic plugin features
- `autovehicles.particles` - Allows seeing particle effects
- `autovehicles.particles.toggle` - Allows toggling particle effects

## Configuration

### Vehicle Settings
```yaml
disabled_worlds:
  - "example_world"
```

### Particle Effects
```yaml
particles:
  enabled: true
  default_style:
    minecart:
      effect: CLOUD
      amount: 1
      speed: 0.1
      offset_x: 0.2
      offset_y: 0.0
      offset_z: 0.2
```

### Configuration Versioning
The plugin now includes automatic configuration management:
- Config version is tracked in `config_version`
- Old configs are automatically backed up before updates
- User customizations are preserved during updates
- New features are automatically added with default values

## Installation
1. Download the latest release
2. Place the jar file in your plugins folder
3. Start/restart your server
4. Configure the plugin in `config.yml`

## Requirements
- Paper/Spigot 1.21.3+
- Java 8 or higher
- Optional dependencies:
  - LuckPerms (for region-specific permissions)
  - ExtraContexts (for region-specific permissions)
  - WorldGuard (for region protection)

## Version Compatibility
- 1.21.3+: Current version with all features
- 1.18-1.21.1: Use [this version](https://github.com/OneCraftES/AutoVehicles/commit/970385b7da12a8c68885cfc181949c6d1b84edd3) for older Minecraft compatibility

## Contributing
Feel free to submit issues and pull requests! While this fork is primarily maintained for the OneCraft server, contributions that align with the project's goals are welcome.

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Credits
- Original plugin: [AutoMinecart by Tisleo](https://github.com/TisLeo/AutoMinecart)
- Enhanced by the OneCraft team
