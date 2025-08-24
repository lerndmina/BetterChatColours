# BetterChatColours Plugin - Global Preset System

## Overview

A Paper### 7. Admin Commands

**Admin Management:**

- `/chatcolors admin create` - Open AnvilGUI to create new global preset
- `/chatcolors admin delete <preset>` - Delete global preset
- `/chatcolors admin list` - List all global presets

**User Management:**

- `/chatcolors` - Open user menu to view and equip available presets
- `/chatcolors equip <preset>` - Equip specific preset (if permission exists)
- `/chatcolors clear` - Clear equipped preset (return to default)
- `/chatcolors list` - List available presets for user

**Admin Override Commands:**

- `/chatcolors force <player> <preset>` - Force set player to use specific preset
- `/chatcolors force <player> <color1> [color2] [color3]...` - Force set player gradient with hex codes
- `/chatcolors unforce <player>` - Remove forced gradient (return to user's equipped preset)
- `/chatcolors info <player>` - Show player's current gradient and preset infoin that provides chat color gradients through PlaceholderAPI integration. **Admins create global presets** that users can equip based on their permissions. Users cannot create their own presets - they can only select from available global presets.

## Core Features

### 1. Global Preset System

- **Admin-only preset creation**: Only admins can create, modify, and delete global presets
- **Permission-based access**: Users can only see and equip presets they have permission for
- **No user preset creation**: Users cannot create custom presets, only equip existing ones
- Colors defined in `config.yml` are automatically converted to default global presets

### 2. GUI System

#### User Menu (Base Command: `/chatcolors`)

- **Preset Selection GUI**: Display all global presets the user has permission to use
- **Equipment Interface**: Click to equip/unequip presets
- **Preview System**: Show gradient preview for each preset
- **No Creation Options**: Users cannot create new presets

#### Admin Menu (`/chatcolors admin`)

- **Global Preset Management**: Create, edit, delete global presets
- **Permission Configuration**: Set required permissions for each preset
- **User Override**: Force-equip presets on specific users
- **Preset Library**: View all global presets and their settings

### 3. Permission-Based Access

- Users can only see presets they have permission for
- No preset limits - users can equip any preset they have access to
- Admin permissions allow full preset management

### 3. Data Storage & Persistence

- **Global presets** stored in `global_presets.yml`
- **User equipped presets** tracked per user (which global preset they have equipped)
- **Admin forced gradients** override user selections

### 4. GUI System

Two primary interfaces:

- **Admin Menu**: Accessible via `/chatcolors admin` - create, delete, and manage global presets
- **User Menu**: Accessible via base `/chatcolors` - view and equip global presets they have permission for
- **Preset permissions** control which presets users can see and equip
- **AnvilGUI integration** for preset naming with live color preview

### 5. PlaceholderAPI Integration

#### Placeholders

- `%chatcolor_before%` - Opening MiniMessage gradient tag
- `%chatcolor_after%` - Closing MiniMessage gradient tag
- `%chatcolor_process:MESSAGE%` - Process entire message with gradient

#### Message Processing

- Strip existing color codes from messages while preserving formatting (bold, italic, etc.)
- Apply gradient using MiniMessage format
- If no gradient set, return message unchanged (let chat plugin handle default formatting)
- Use MiniMessage's built-in gradient construction (no manual gradient calculation needed)

### 6. Permission System

```
chatcolors.use - Basic access to plugin features
chatcolors.color.use - Default fallback for colors without specific permissions
chatcolors.color.<colorname> - Access to specific colors from config
chatcolors.presets.<number> - Number of presets allowed (1, 3, 5, 10, etc.)
chatcolors.admin - Administrative commands and overrides
```

### 7. Admin Commands

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

### 8. Configuration Structure

### config.yml - Default Presets

```yaml
# Default presets that get auto-converted to global presets on startup
default_presets:
  rainbow:
    colors:
      - "#FF0000" # Red
      - "#FF7F00" # Orange
      - "#FFFF00" # Yellow
      - "#00FF00" # Green
      - "#0000FF" # Blue
      - "#4B0082" # Indigo
      - "#9400D3" # Violet
    permission: "chatcolors.preset.rainbow"

  fire:
    colors:
      - "#FF4500" # OrangeRed
      - "#FF6347" # Tomato
      - "#FFD700" # Gold
    permission: "chatcolors.preset.fire"

  ocean:
    colors:
      - "#006994" # Deep Blue
      - "#87CEEB" # Sky Blue
    permission: "chatcolors.preset.ocean"

# Plugin settings
settings:
  max-colors-per-gradient: 5
  gui-size: 54
  validate-hex-codes: true
  strip-existing-colors: true
  preserve-formatting: true
```

### global_presets.yml - Runtime Presets

```yaml
# Generated and managed by GlobalPresetManager
presets:
  rainbow:
    id: "rainbow"
    name: "Rainbow"
    colors:
      - "#FF0000"
      - "#FF7F00"
      - "#FFFF00"
      - "#00FF00"
      - "#0000FF"
      - "#4B0082"
      - "#9400D3"
    permission: "chatcolors.preset.rainbow"

  fire:
    id: "fire"
    name: "Fire"
    colors:
      - "#FF4500"
      - "#FF6347"
      - "#FFD700"
    permission: "chatcolors.preset.fire"
```

### user_data.yml - Equipped Presets

```yaml
# Tracks which preset each user has equipped
users:
  "550e8400-e29b-41d4-a716-446655440000": # UUID
    equipped_preset: "rainbow"
  "6ba7b810-9dad-11d1-80b4-00c04fd430c8":
    equipped_preset: "fire"

# Admin forced gradients override equipped presets
forced_gradients:
  "550e8400-e29b-41d4-a716-446655440000":
    colors:
      - "#FF0000"
      - "#00FF00"
```

### 9. Messages Configuration

### messages.yml

```yaml
gui:
  preset-selection-title: "<gold><bold>Available Presets</bold></gold>"
  admin-settings-title: "<red><bold>Admin: Global Presets</bold></red>"
  no-presets: "<gray>No presets available</gray>"

commands:
  no-permission: "<red>You don't have permission to use this command.</red>"
  player-not-found: "<red>Player {player} not found.</red>"
  preset-not-found: "<red>Preset '{preset}' not found.</red>"
  preset-equipped: "<green>Preset '{preset}' equipped!</green>"
  preset-cleared: "<green>Preset cleared. Using default chat colors.</green>"
  preset-created: "<green>Preset '{preset}' created successfully!</green>"
  preset-deleted: "<green>Preset '{preset}' deleted!</green>"

admin:
  gradient-forced: "<green>Forced gradient set for {player}.</green>"
  gradient-unforced: "<green>Forced gradient removed for {player}.</green>"

errors:
  invalid-hex: "<red>Invalid hex code: {hex}. Use format #RRGGBB.</red>"
  preset-exists: "<red>Preset '{preset}' already exists!</red>"
  no-permission-preset: "<red>You don't have permission to use preset '{preset}'.</red>"
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
