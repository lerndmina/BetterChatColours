# BetterChatColours Plugin - Development Plan

## Overview

A Paper 1.19.4 plugin that provides chat color gradients through PlaceholderAPI integration. Users select colors from a configurable palette to create gradients that can be saved as presets and used with any MiniMessage-compatible chat plugin.

## Core Features

### 1. Color Palette System

- Colors defined in `config.yml` with hex codes, permissions, and display names
- Automatic hex code validation on startup (invalid codes warned and discarded)
- Default permission fallback system for colors without specific permissions

### 2. GUI System

#### Main Menu

- Display saved presets (up to configured limit)
- "Create New Gradient" option
- Real-time gradient preview in GUI title as colors are selected

#### Color Selection Flow

1. User selects colors sequentially from available palette
2. GUI title updates to show gradient progress using MiniMessage format
3. After selecting colors, display wool blocks matching closest colors
4. User can click and drag to reorder colors in the gradient
5. Save gradient with custom name as preset

#### Preset Management

- View, edit, delete, and activate saved presets
- Permission-based limits (capped at config maximum for predictability)
- No unlimited presets permission - all users respect the config cap

### 3. Data Storage & Persistence

- User presets stored persistently between server restarts
- Current active gradient per user
- Admin-forced gradients override user selections

### 4. PlaceholderAPI Integration

#### Placeholders

- `%chatcolor_before%` - Opening MiniMessage gradient tag
- `%chatcolor_after%` - Closing MiniMessage gradient tag
- `%chatcolor_process:MESSAGE%` - Process entire message with gradient

#### Message Processing

- Strip existing color codes from messages while preserving formatting (bold, italic, etc.)
- Apply gradient using MiniMessage format
- If no gradient set, return message unchanged (let chat plugin handle default formatting)
- Use MiniMessage's built-in gradient construction (no manual gradient calculation needed)

### 5. Permission System

```
chatcolors.use - Basic access to plugin features
chatcolors.color.use - Default fallback for colors without specific permissions
chatcolors.color.<colorname> - Access to specific colors from config
chatcolors.presets.<number> - Number of presets allowed (1, 3, 5, 10, etc.)
chatcolors.admin - Administrative commands and overrides
```

### 6. Admin Commands

```
/chatcolors reload - Reload configuration
/chatcolors set <player> <preset> - Force set player to use specific preset
/chatcolors set <player> <color1> [color2] [color3]... - Force set player gradient with hex codes
/chatcolors clear <player> - Remove player's gradient (return to default)
/chatcolors presets <player> - View player's saved presets
/chatcolors delete <player> <preset> - Delete specific preset from player
/chatcolors addcolor <colorname> <hexcode> [permission] [displayname] - Add color to config runtime
/chatcolors info <player> - Show player's current gradient and preset info
```

## Configuration Structure

### config.yml

```yaml
# Color palette available to users
colours:
  red:
    permission: "chatcolors.color.red" # optional
    colour: "#FF0000"
    displayname: "Bright Red" # optional, defaults to "Red"
  blue:
    colour: "#0000FF"
    # uses chatcolors.color.use permission by default
  green:
    colour: "#00FF00"
    displayname: "Lime Green"
  purple:
    colour: "#800080"
    permission: "chatcolors.color.vip"
  gold:
    colour: "#FFD700"
    permission: "chatcolors.color.premium"

# Plugin settings
settings:
  max-colors-per-gradient: 5
  max-presets-per-user: 10
  gui-size: 54
  validate-hex-codes: true
  strip-existing-colors: true
  preserve-formatting: true

# Permission-based preset limits
permissions:
  preset-limits:
    default: 3 # chatcolors.presets.3
    vip: 5 # chatcolors.presets.5
    premium: 10 # chatcolors.presets.10
```

### messages.yml

```yaml
gui:
  main-title: "&6Chat Colors - Select Preset"
  create-title: "&6Creating Gradient: {gradient_preview}"
  reorder-title: "&6Reorder Colors: {gradient_preview}"

commands:
  no-permission: "&cYou don't have permission to use this command."
  player-not-found: "&cPlayer {player} not found."
  preset-not-found: "&cPreset '{preset}' not found."
  gradient-set: "&aGradient set for {player}."
  gradient-cleared: "&aGradient cleared for {player}."

errors:
  invalid-hex: "&cInvalid hex code: {hex}. Skipping color '{color}'."
  max-presets: "&cYou've reached your maximum preset limit ({limit})."
  invalid-color: "&cColor '{color}' not found in palette."
```

## Technical Implementation

### Project Structure

```
src/main/java/com/yourname/betterchatcolours/
├── BetterChatColours.java (Main plugin class)
├── commands/
│   ├── ChatColorsCommand.java
│   └── CommandTabCompleter.java
├── config/
│   ├── ConfigManager.java
│   └── MessagesConfig.java
├── data/
│   ├── UserDataManager.java
│   ├── PresetData.java
│   └── ColorData.java
├── gui/
│   ├── MainMenuGUI.java
│   ├── ColorSelectionGUI.java
│   ├── ReorderGUI.java
│   └── GUIUtils.java
├── placeholders/
│   └── ChatColorsExpansion.java
├── utils/
│   ├── ColorUtils.java
│   ├── MiniMessageUtils.java
│   └── PermissionUtils.java
└── listeners/
    └── GUIListener.java
```

### Dependencies

- Paper 1.19.4
- PlaceholderAPI
- Adventure API (included in Paper)
- MiniMessage (included in Paper)

### Key Technical Notes

1. Use MiniMessage's gradient syntax: `<gradient:color1:color2:color3>text</gradient>`
2. Wool color matching using closest RGB distance calculation
3. Drag-and-drop reordering using inventory click events
4. Persistent storage using YAML files in plugin data folder
5. Hex validation using regex pattern and Color.decode() fallback
6. Permission checking with bukkit permission system + custom logic

## Development Phases

### Phase 1: Core Infrastructure

- Plugin main class and basic setup
- Configuration system with validation
- Permission system implementation
- Basic command structure

### Phase 2: Data Management

- User data storage system
- Preset management
- Color palette loading and validation

### Phase 3: GUI System

- Main menu GUI
- Color selection interface
- Drag-and-drop reordering system
- Real-time preview in titles

### Phase 4: PlaceholderAPI Integration

- Placeholder expansion class
- Message processing logic
- MiniMessage gradient generation

### Phase 5: Admin Commands

- Command system implementation
- Tab completion
- Admin override functionality

### Phase 6: Testing & Polish

- Comprehensive testing
- Error handling improvements
- Performance optimization
- Documentation

## Future Enhancement Ideas

- Shareable presets between users
- Gradient animation effects
- Custom gradient patterns (rainbow, fade, etc.)
- Integration with economy plugins for color purchases
- RGB color picker GUI for custom colors
- Gradient preview in chat before saving
