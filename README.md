# AutoVehicles2
### Enhanced Vehicle Mechanics for modern Minecraft

AutoVehicles2 is a high-performance, feature-rich fork of the original **[AutoMinecart](https://github.com/TisLeo/AutoMinecart)** plugin by **[TisLeo](https://github.com/TisLeo)**. This version, managed by **OneCraft**, brings significant improvements to stability, performance, and visual effects.

A powerful, high-performance Minecraft (Paper/Spigot) plugin that elevates vehicle mechanics with automatic spawning (terraria-like), persistent tagging, and dynamic particle effects.

## ✨ Key Features

- **Automatic Spawning**: Right-click rails (for minecarts), water, or ice (for boats) with an empty hand to automatically deploy and enter a vehicle.
- **Reliable persistent Cleanup**: No more clutter! Vehicles are automatically removed when you leave them, using Bukkit's `PersistentDataContainer` to ensure cleanup works even across player sessions and server restarts.
- **Dynamic Particle System**: High-performance particles that scale with your vehicle's speed.
- **Environmental Awareness**: Biome-specific particles, underground effects, and special ice-path visuals.
- **Optimized Performance**: Internal configuration caching and per-vehicle state tracking ensure minimal impact on server performance.
- **Highly Configurable**: Control every detail of the particle styles, speed thresholds, and world restrictions.

## 🚀 Getting Started

### Installation
1. Download the latest `AutoVehicles2-2.0.0.jar`.
2. Place it in your server's `plugins/` folder.
3. Restart your server.

### Basic Usage
- **Minecarts**: Right-click any rail with an empty hand.
- **Boats**: Right-click water or ice with an empty hand.
- **Exit**: Ships and carts are removed immediately upon exit to keep your world clean.

## ⚙️ Configuration

The plugin generates a detailed `config.yml` that allows you to customize colors, styles, and behaviors.

### Commands
- `/togglecart`: Enable/Disable automatic minecart spawning for yourself.
- `/toggleboat`: Enable/Disable automatic boat spawning for yourself.
- `/toggleparticles`: Toggle your personal particle effects.

### Permissions
- `autovehicles2.use`: Use the basic spawning features.
- `autovehicles2.particles`: Access to base particle effects.
- `autovehicles2.particles.environment`: Access to advanced biome and environment particles.
- `autovehicles2.particles.toggle`: Allows players to toggle their own particles.

## � Credits & History

AutoVehicles2 began as a fork of **AutoMinecart** by **TisLeo**. We are grateful for the solid foundation he provided. 

**OneCraft** has since evolved the project into version 2.0.0, focusing on:
- **Scalability**: Reliable PDC-based vehicle cleanup.
- **Aesthetics**: Environment and biome-aware particle systems.
- **Performance**: High-efficiency particle caching and speed tracking.

---

Original project by [TisLeo](https://github.com/TisLeo).
