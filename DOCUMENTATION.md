# BetterChatColours - Complete Documentation

## Overview

BetterChatColours is a comprehensive Paper 1.19.4 plugin that provides gradient chat color functionality with GUI configuration, preset management, admin commands, and PlaceholderAPI integration.

## Features

### Core Features

- **Gradient Chat Colors**: Apply smooth color gradients to chat messages
- **GUI Interface**: Interactive color selection and preset management
- **Preset System**: Save and reuse custom color combinations
- **PlaceholderAPI Integration**: Seamless chat integration with other plugins
- **Admin Commands**: Comprehensive administration tools
- **Performance Monitoring**: Built-in performance tracking and optimization

### Technical Features

- **Namespace**: `io.imadam.betterchatcolours`
- **Java Version**: 17
- **Build System**: Gradle 9.0.0
- **Dependencies**: Paper 1.19.4, PlaceholderAPI 2.11.6
- **Architecture**: Layered design with error handling and async operations

## Installation

1. **Requirements**:

   - Paper server 1.19.4+
   - Java 17+
   - PlaceholderAPI plugin

2. **Installation Steps**:

   ```bash
   # Clone the repository
   git clone <repository-url>
   cd BetterChatColours

   # Build the plugin
   ./gradlew build

   # Copy to server
   cp build/libs/BetterChatColours-1.0.0-SNAPSHOT.jar /path/to/server/plugins/
   ```

3. **First Run**:
   - Start your server
   - Plugin will create default configuration files
   - Configure colors and permissions as needed

## Configuration

### Main Config (`config.yml`)

```yaml
settings:
  max-colors-per-gradient: 5
  max-presets-per-user: 10
  gui-size: 54
  validate-hex-codes: true
  strip-existing-colors: true
  preserve-formatting: true

colours:
  red:
    colour: "#FF0000"
    permission: "chatcolors.color.red"
    displayname: "Red"
  green:
    colour: "#00FF00"
    permission: "chatcolors.color.green"
    displayname: "Green"
  # ... more colors
```

### Messages Config (`messages.yml`)

```yaml
# All user-facing messages
gui:
  main-title: "Chat Colors"
  color-selection-title: "Select Colors"
  preset-activated: "Activated preset: {preset}"
  preset-updated: "Updated preset: {preset}"

commands:
  help:
    header: "=== BetterChatColours Help ==="
    usage: "Usage: /chatcolors <command>"
  no-permission: "You don't have permission to use this command"

errors:
  insufficient-colors: "You need at least 2 colors for a gradient"
  preset-limit-reached: "You've reached your preset limit"
```

## Commands

### Player Commands

- `/chatcolors` - Open main GUI
- `/chatcolors help` - Show help information
- `/chatcolors list` - List available presets
- `/chatcolors apply <preset>` - Apply a specific preset
- `/chatcolors clear` - Clear current gradient
- `/chatcolors presets` - Manage presets

### Admin Commands

- `/chatcolors reload` - Reload configuration
- `/chatcolors set <player> <preset>` - Set player's gradient
- `/chatcolors adminclear <player>` - Clear player's gradient
- `/chatcolors addcolor <name> <hex> [permission]` - Add new color
- `/chatcolors info <player>` - Show player information
- `/chatcolors stats [reset]` - View/reset performance statistics

## Permissions

### Player Permissions

- `betterchatcolours.use` - Basic plugin usage
- `betterchatcolours.gui` - Access to GUI interface
- `betterchatcolours.presets.create` - Create new presets
- `betterchatcolours.presets.manage` - Manage own presets
- `betterchatcolours.colors.*` - Access to all colors

### Admin Permissions

- `betterchatcolours.admin` - Full admin access
- `betterchatcolours.admin.reload` - Reload configuration
- `betterchatcolours.admin.set` - Set other players' gradients
- `betterchatcolours.admin.clear` - Clear other players' gradients
- `betterchatcolours.admin.stats` - View performance statistics

### Color-Specific Permissions

- `betterchatcolours.color.<color>` - Access to specific color
- `betterchatcolours.color.premium` - Access to premium colors
- `betterchatcolours.color.vip` - Access to VIP colors

## GUI System

### Main Menu GUI

- **Purpose**: Central hub for preset management
- **Features**:
  - View all saved presets
  - Create new gradients
  - Quick preset activation
  - User statistics display

### Color Selection GUI

- **Purpose**: Interactive color picker
- **Features**:
  - Visual color selection
  - Real-time preview
  - Gradient ordering
  - Save/update presets

### Reorder GUI

- **Purpose**: Drag-and-drop color arrangement
- **Features**:
  - Visual color reordering
  - Real-time preview updates
  - Intuitive drag-and-drop interface

## PlaceholderAPI Integration

### Available Placeholders

- `%betterchatcolours_gradient%` - Apply current gradient to text
- `%betterchatcolours_has_gradient%` - Check if player has active gradient
- `%betterchatcolours_preset_count%` - Number of saved presets
- `%betterchatcolours_active_preset%` - Name of active preset

### Usage Examples

```yaml
# In chat format
format: "%player_name%: %betterchatcolours_gradient%%message%"

# In scoreboard
- "Gradients: %betterchatcolours_preset_count%"
- "Active: %betterchatcolours_active_preset%"
```

## Development

### Project Structure

```
src/main/java/io/imadam/betterchatcolours/
├── BetterChatColours.java          # Main plugin class
├── commands/                       # Command handling
│   ├── ChatColorsCommand.java
│   └── CommandTabCompleter.java
├── config/                         # Configuration management
│   ├── ConfigManager.java
│   └── MessagesConfig.java
├── data/                          # Data management
│   ├── DataManager.java
│   ├── UserDataManager.java
│   ├── ColorData.java
│   ├── ColorPreset.java
│   └── PresetData.java
├── gui/                           # GUI system
│   ├── MainMenuGUI.java
│   ├── ColorSelectionGUI.java
│   ├── ReorderGUI.java
│   ├── GUIListener.java
│   └── GUIUtils.java
├── placeholders/                  # PlaceholderAPI integration
│   └── ChatColorsExpansion.java
└── utils/                         # Utility classes
    ├── PerformanceMonitor.java
    ├── PermissionUtils.java
    └── ColorUtils.java
```

### Building from Source

```bash
# Clone repository
git clone <repository-url>
cd BetterChatColours

# Build with Gradle
./gradlew build

# Run tests
./gradlew test

# Generate documentation
./gradlew javadoc
```

### Testing

The plugin includes comprehensive unit tests:

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests BetterChatColoursTest

# Generate test report
./gradlew test jacocoTestReport
```

## Performance Monitoring

### Built-in Monitoring

The plugin includes performance monitoring capabilities:

- **Operation Tracking**: Times all major operations
- **Memory Monitoring**: Tracks memory usage
- **Statistics Logging**: Detailed performance logs
- **Slow Operation Detection**: Warns about operations >100ms

### Monitoring Commands

```bash
# View current statistics
/chatcolors stats

# Reset statistics
/chatcolors stats reset
```

### Performance Optimization

- **Async Data Loading**: User data loads asynchronously
- **Caching**: Frequently accessed data is cached
- **Efficient GUI Updates**: Only updates changed components
- **Memory Management**: Automatic cleanup of unused data

## Troubleshooting

### Common Issues

1. **Plugin Won't Start**

   - Check PlaceholderAPI is installed
   - Verify Java 17+ is being used
   - Check server logs for specific errors

2. **Colors Not Working**

   - Verify color permissions
   - Check hex code format (#RRGGBB)
   - Ensure PlaceholderAPI expansion is registered

3. **GUI Not Opening**

   - Check GUI permissions
   - Verify inventory space
   - Check for conflicting plugins

4. **Performance Issues**
   - Use `/chatcolors stats` to check performance
   - Reduce max colors/presets if needed
   - Check for excessive data storage

### Debug Mode

Enable debug logging in `config.yml`:

```yaml
debug:
  enabled: true
  log-level: "INFO"
  track-performance: true
```

### Support

- **GitHub Issues**: Report bugs and feature requests
- **Wiki**: Comprehensive documentation and guides
- **Discord**: Community support and discussion

## API for Developers

### Maven Dependency

```xml
<dependency>
    <groupId>io.imadam</groupId>
    <artifactId>betterchatcolours</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

### Basic API Usage

```java
// Get plugin instance
BetterChatColours plugin = (BetterChatColours) Bukkit.getPluginManager().getPlugin("BetterChatColours");

// Apply gradient to player
plugin.getDataManager().applyPreset(player, "preset-name");

// Create new preset
List<String> colors = Arrays.asList("#FF0000", "#00FF00", "#0000FF");
plugin.getDataManager().createPreset(player, "my-preset", colors);

// Check if player has gradient
boolean hasGradient = plugin.getDataManager().hasActiveGradient(player);
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## Changelog

### Version 1.0.0-SNAPSHOT

- Initial release
- Complete GUI system implementation
- PlaceholderAPI integration
- Admin command system
- Performance monitoring
- Comprehensive error handling
- Full test suite

---

**Note**: This plugin is in active development. Features and documentation may change between versions.
