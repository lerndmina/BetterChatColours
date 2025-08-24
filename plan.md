# BetterChatColours Plugin - Global Preset System (InvUI Edition)

## Overview

A Paper 1.19.4 plugin that provides chat color gradients through PlaceholderAPI integration. **Admins create global presets** that users can equip based on their permissions. Users cannot create their own presets - they can only select from available global presets.

**Key Change: Moving to InvUI Framework** - After experiencing security issues with custom GUI implementation (item theft vulnerability), we're switching to the professional InvUI library for secure, feature-rich inventory management.

## Core Features

### 1. Global Preset System (Unchanged)

- **Admin-only preset creation**: Only admins can create, modify, and delete global presets
- **Permission-based access**: Users can only see and equip presets they have permission for
- **No user preset creation**: Users cannot create custom presets, only equip existing ones
- Colors defined in `config.yml` are automatically converted to default global presets

### 2. Secure GUI System (InvUI-Based)

#### Key Benefits of InvUI:

- **Security**: No item theft vulnerabilities - items are protected by design
- **Professional UI**: Builder pattern with structure-based layouts (like shaped recipes)
- **Localization Ready**: Built-in support for multi-language UIs
- **Clean Code**: Separation of GUI logic from display (Window system)
- **Rich Items**: AbstractItem, ControlItem, CycleItem, etc.

#### Unified GUI System (Single Command: `/chatcolors`)

- **Main Menu GUI**: Central hub that adapts based on user permissions
  - **For Regular Users**: Direct access to preset selection and equipment
  - **For Admins**: Additional options for preset management and administration
- **Preset Selection GUI**: Paged GUI displaying all available global presets
- **Equipment Interface**: Click to equip/unequip presets with instant feedback
- **Preview System**: Live gradient preview in item names and lore
- **Admin Features** (permission-gated within main GUI):
  - **Global Preset Management**: Create, edit, delete global presets through GUI
  - **Color Editor**: Interactive color selection with live preview
  - **Permission Configuration**: Set required permissions through AnvilGUI
  - **All Text Input via AnvilGUI**: Preset names, colors, permissions, etc.

#### Reload Command (`/chatcolors reload`)

- **Admin-only reload**: Reload configuration and presets from files
- **No GUI**: Simple command with text feedback

### 3. Permission-Based Access (Unchanged)

- Users can only see presets they have permission for
- No preset limits - users can equip any preset they have access to
- Admin permissions allow full preset management

## Technical Implementation (✅ COMPLETE)

### InvUI Integration (✅ IMPLEMENTED)

#### Dependencies

```xml
<repository>
    <id>xenondevs</id>
    <url>https://repo.xenondevs.xyz/releases</url>
</repository>

<dependency>
    <groupId>xyz.xenondevs.invui</groupId>
    <artifactId>invui</artifactId>
    <version>1.46</version>
    <type>pom</type>
</dependency>
```

#### Compatibility (✅ FIXED)

- **Java 17 Target**: Plugin compiled for Java 17 compatibility (major version 61)
- **Paper 1.19.4**: Fully compatible with Paper 1.19.4 servers (r15 implementation included)
- **Complete InvUI**: All version-specific implementations (r1-r24) included and properly relocated
- **Server Requirements**: Java 17+ (standard for Paper 1.19.4)

#### GUI Architecture (✅ IMPLEMENTED)

- **Builder Pattern**: Using `PagedGui.Builder` and `Gui.normal()` builders
- **Custom Items**: All interactive elements extend AbstractItem
- **PagedGui System**: Implemented for preset selection and editing
- **AnvilGUI Integration**: Text input for preset names, colors, permissions
- **Security**: Complete protection against item theft and duplication
- **Pure InvUI**: No fallback system - all GUI operations handled by InvUI framework

### Project Structure (✅ COMPLETE)

```
src/main/java/io/imadam/betterchatcolours/
├── BetterChatColours.java (✅ InvUI registered, only ChatInputManager event listener)
├── commands/
│   └── ChatColorsCommand.java (✅ Single command with InvUI GUI dispatch)
├── data/
│   ├── GlobalPresetManager.java (✅ Enhanced with automatic permissions)
│   ├── UserDataManager.java (✅ Complete)
│   └── GlobalPresetData.java (✅ Complete)
├── gui/ (✅ Complete InvUI-Based System)
│   ├── MainMenuGUI.java (✅ Permission-based main hub)
│   ├── PresetSelectionGUI.java (✅ PagedGui implementation)
│   ├── InvUIAdminPresetCreateGUI.java (✅ Complete preset creation system)
│   ├── InvUIAdminPresetEditGUI.java (✅ Paginated preset editing)
│   ├── ChatInputManager.java (✅ AnvilGUI integration)
│   ├── GUIUtils.java (✅ Utility methods)
│   └── items/ (✅ All Custom InvUI Items)
│       ├── PresetItem.java (✅ extends AbstractItem)
│       ├── CreatePresetItem.java (✅ extends AbstractItem)
│       ├── EditPresetItem.java (✅ extends AbstractItem)
│       ├── SelectPresetsItem.java (✅ extends AbstractItem)
│       └── preset/
│           ├── AddColorItem.java (✅ extends AbstractItem)
│           ├── ColorItem.java (✅ extends AbstractItem)
│           ├── PermissionItem.java (✅ extends AbstractItem)
│           └── PresetNameItem.java (✅ extends AbstractItem)
└── placeholders/
    └── ChatColorsExpansion.java (✅ PlaceholderAPI integration)
│   │   ├── PermissionItem.java (extends AbstractItem)
│   │   ├── SavePresetItem.java (extends AbstractItem)
│   │   ├── CreatePresetItem.java (extends AbstractItem)
│   │   ├── EditPresetItem.java (extends AbstractItem)
│   │   └── DeletePresetItem.java (extends AbstractItem)
│   ├── anvil/
│   │   ├── PresetNameGUI.java (AnvilGUI for naming)
│   │   ├── ColorInputGUI.java (AnvilGUI for hex colors)
│   │   └── PermissionInputGUI.java (AnvilGUI for permissions)
│   └── GUIUtils.java (InvUI helper methods)
├── placeholders/
│   └── ChatColorsExpansion.java (Unchanged)
└── listeners/
    └── (Remove GUIListener - InvUI handles this internally)
```

### GUI Implementation Examples

#### Main Menu GUI (Permission-Based Hub)

```java
public class MainMenuGUI {
    public void open(Player player) {
        boolean isAdmin = player.hasPermission("chatcolors.admin");

        Gui.Builder<?, ?> builder = Gui.normal()
            .setStructure(
                "# # # # # # # # #",
                "# s # # # # # # #",
                "# # # # # # # # #");

        if (isAdmin) {
            builder.setStructure(
                "# # # # # # # # #",
                "# s # c # e # d #",
                "# # # # # # # # #")
                .addIngredient('c', new CreatePresetItem())
                .addIngredient('e', new EditPresetItem())
                .addIngredient('d', new DeletePresetItem());
        }

        Gui gui = builder
            .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)))
            .addIngredient('s', new SelectPresetsItem())
            .build();

        Window window = Window.single()
            .setViewer(player)
            .setTitle(isAdmin ? "Chat Colors - Admin Menu" : "Chat Colors")
            .setGui(gui)
            .build();

        window.open();
    }
}
```

#### Preset Selection GUI (InvUI Paged)

```java
public class PresetSelectionGUI {
    public void open(Player player) {
        // Get available presets
        List<GlobalPresetData> presets = getAvailablePresets(player);

        // Create paged GUI with preset items
        Gui gui = Gui.paged()
            .setStructure(
                "# # # # # # # # #",
                "# . . . . . . . #",
                "# . . . . . . . #",
                "# < # # # # # > #")
            .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)))
            .addIngredient('<', new BackwardItem())
            .addIngredient('>', new ForwardItem())
            .setContent(presets.stream().map(PresetItem::new).toList())
            .build();

        Window window = Window.single()
            .setViewer(player)
            .setTitle("Available Presets")
            .setGui(gui)
            .build();

        window.open();
    }
}
```

#### Color Editing GUI (InvUI Normal)

```java
public class GlobalPresetSettingsGUI {
    public void open(Player player, String presetName, List<String> colors) {
        Gui gui = Gui.normal()
            .setStructure(
                "c c c c c c a p",
                "# # # # # # # #",
                "# # # s # # # #")
            .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)))
            .addIngredient('c', () -> new ColorItem(colors.get(index)))
            .addIngredient('a', new AddColorItem())
            .addIngredient('p', new PermissionItem())
            .addIngredient('s', new SavePresetItem())
            .build();

        // Create gradient title that updates dynamically
        String gradientTitle = createGradientTitle(presetName, colors);

        Window window = Window.single()
            .setViewer(player)
            .setTitle(gradientTitle)
            .setGui(gui)
            .build();

        window.open();
    }
}
```

#### Custom Items

```java
public class ColorItem extends AbstractItem {
    private String hexColor;
    private int index;

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(getClosestDyeColor(hexColor))
            .setDisplayName("Color " + (index + 1) + ": " + hexColor)
            .addLoreLines(
                "Left-click to edit",
                "Right-click to remove",
                MiniMessage.miniMessage().deserialize("<color:" + hexColor + ">████████</color>")
            );
    }

    @Override
    public void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
        if (clickType.isLeftClick()) {
            openColorEditor(player);
        } else if (clickType.isRightClick()) {
            removeColor();
        }
        notifyWindows(); // Update display
    }
}
```

#### Placeholders

- `%chatcolor_before%` - Opening MiniMessage gradient tag
- `%chatcolor_after%` - Closing MiniMessage gradient tag
- `%chatcolor_process:MESSAGE%` - Process entire message with gradient

## Development Plan (Fresh Start)

### Phase 1: InvUI Foundation

- ✅ Add InvUI dependency and repository
- ✅ Update main plugin class with `InvUI.getInstance().setPlugin(this)`
- 🔄 Remove old GUI system (GUIListener, custom click handling)
- 🔄 Create base InvUI GUI classes

### Phase 2: Preset Selection GUI (InvUI Paged)

- 🔄 Implement PresetSelectionGUI with PagedGui
- 🔄 Create PresetItem extending AbstractItem
- 🔄 Add navigation controls (BackwardItem, ForwardItem)
- 🔄 Integrate with existing GlobalPresetManager

### Phase 3: Admin Preset Creation (InvUI Normal)

- 🔄 Implement GlobalPresetSettingsGUI with normal Gui
- 🔄 Create ColorItem, AddColorItem, PermissionItem, SavePresetItem
- 🔄 Add live gradient preview in GUI title
- 🔄 Integrate AnvilGUI for text input (preset names, colors, permissions)

### Phase 4: Testing & Security Validation

- 🔄 Test item theft protection
- 🔄 Verify click handling works correctly
- 🔄 Test with multiple players simultaneously
- 🔄 Performance testing with large preset lists

### Phase 5: Polish & Documentation

- 🔄 Add error handling and user feedback
- 🔄 Create admin documentation
- 🔄 Add configuration options for GUI layouts
- 🔄 Final testing and optimization

## Current Status

### Issues Resolved

- ✅ **Color Removal UnsupportedOperationException**: Fixed mutable list creation
- ✅ **GUI Title Gradient Preview**: Added live gradient preview in titles
- ✅ **Global Preset Saving**: Fixed to use GlobalPresetManager properly
- ✅ **ChatControlRed Integration**: Fixed placeholder processing for `%chatcolor_process:{message}%`
- ✅ **Gradient Display in GUI**: Preset names now show actual gradient effects instead of color codes
- ✅ **Fallback GUI System**: Working alternative to InvUI for current compatibility
- ✅ **AnvilGUI Compatibility Issues**: Replaced with reliable chat-based input system
- ✅ **Emoji Rendering Issues**: Replaced all emojis with universally compatible text labels
- ✅ **Admin Preset Creation**: Complete workflow implemented with chat input validation
- ✅ **Input Validation**: Comprehensive validation for preset names, hex colors, and permissions
- ✅ **Admin Preset Management**: Complete CRUD operations for preset management
- ✅ **Default Preset Permissions**: All default presets now have proper permission nodes

### Current Status

- ✅ **Core Functionality**: Plugin works with gradient display and preset selection
- ✅ **PlaceholderAPI Integration**: Compatible with ChatControlRed's `{message}` placeholder system
- ✅ **Java 21 Upgrade**: Project upgraded to Java 21, matching server runtime environment
- ✅ **Chat Input System**: Replaced AnvilGUI with reliable chat-based input system
- ✅ **Admin Preset Creation**: Complete workflow for creating presets via chat input
- ✅ **Admin Preset Editing**: Complete workflow for editing and deleting existing presets
- ✅ **Emoji Compatibility**: All emojis replaced with text-based labels for universal compatibility
- ✅ **Fallback GUI System**: Working alternative to InvUI for current compatibility
- ✅ **Comprehensive Default Presets**: 28 total presets (16 standard Minecraft colors + 12 premium gradients)
- ✅ **Permission System**: All presets have proper `chatcolor.preset.name` permissions

### Next Steps

1. ✅ **Java 21 Migration**: Successfully upgraded project to Java 21
2. ✅ **Chat Input System**: Implemented reliable chat-based input replacing AnvilGUI
3. ✅ **Admin Preset Creation**: Complete workflow with name, color, and permission input
4. ✅ **Gradient Editing**: Implemented editing of existing presets (colors, names, permissions)
5. ✅ **Preset Deletion**: Added ability to delete existing presets with confirmation
6. ✅ **Enhanced Default Presets**: Added all standard Minecraft colors and premium gradients
7. 🔄 **Testing & Polish**: Final testing and documentation
8. 🔄 **Performance Optimization**: Optimize for large numbers of presets

## Key Changes from Previous Approach

1. **Security First**: InvUI provides built-in protection against item theft
2. **Professional Architecture**: Separation of GUI logic and display
3. **Maintainable Code**: Builder patterns and structure-based layouts
4. **Future-Proof**: Localization support and extensible item system
5. **Less Custom Code**: Leverage proven library instead of reinventing GUI system

The core plugin functionality (commands, data management, PlaceholderAPI integration) remains unchanged. Only the GUI layer is being replaced for security and maintainability.
