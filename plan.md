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

#### User Menu (Base Command: `/chatcolors`)

- **Preset Selection GUI**: Paged GUI displaying all available global presets
- **Equipment Interface**: Click to equip/unequip presets with instant feedback
- **Preview System**: Live gradient preview in item names and lore
- **No Creation Options**: Users cannot create new presets

#### Admin Menu (`/chatcolors admin create`)

- **Global Preset Management**: Secure GUI for creating/editing global presets
- **Color Editor**: Interactive color selection with live preview in GUI title
- **Permission Configuration**: Set required permissions for each preset
- **AnvilGUI Integration**: Professional preset naming interface

### 3. Permission-Based Access (Unchanged)

- Users can only see presets they have permission for
- No preset limits - users can equip any preset they have access to
- Admin permissions allow full preset management

## Technical Implementation (Updated)

### InvUI Integration

#### Dependencies

```xml
<repository>
    <id>xenondevs</id>
    <url>https://repo.xenondevs.xyz/releases</url>
</repository>

<dependency>
    <groupId>xyz.xenondevs.invui</groupId>
    <artifactId>invui</artifactId>
    <version>1.34</version>
    <type>pom</type>
</dependency>
```

#### GUI Architecture

- **Builder Pattern**: Use `Gui.normal()` and `Gui.paged()` builders
- **Structure System**: Visual layout definition similar to shaped recipes
- **Custom Items**: Extend `AbstractItem` for interactive elements
- **Windows**: Separate display logic from GUI logic
- **Security**: Built-in protection against item theft and duplication

### Project Structure (Updated)

```
src/main/java/io/imadam/betterchatcolours/
├── BetterChatColours.java (Main - includes InvUI.getInstance().setPlugin(this))
├── commands/
│   ├── ChatColorsCommand.java
│   └── CommandTabCompleter.java
├── data/
│   ├── GlobalPresetManager.java (Unchanged)
│   ├── UserDataManager.java (Unchanged)
│   └── GlobalPresetData.java (Unchanged)
├── gui/ (Complete Rewrite with InvUI)
│   ├── PresetSelectionGUI.java (PagedGui with custom items)
│   ├── GlobalPresetSettingsGUI.java (Normal GUI with color editing)
│   ├── items/
│   │   ├── PresetItem.java (extends AbstractItem)
│   │   ├── ColorItem.java (extends AbstractItem)
│   │   ├── AddColorItem.java (extends AbstractItem)
│   │   ├── PermissionItem.java (extends AbstractItem)
│   │   └── SavePresetItem.java (extends AbstractItem)
│   └── GUIUtils.java (InvUI helper methods)
├── placeholders/
│   └── ChatColorsExpansion.java (Unchanged)
└── listeners/
    └── (Remove GUIListener - InvUI handles this internally)
```

### GUI Implementation Examples

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

### Current Issue

- ❌ **GUI Security Vulnerability**: Custom GUI allows item theft
- 🔄 **Solution in Progress**: Migrating to InvUI framework

### Next Steps

1. Complete InvUI dependency setup
2. Rewrite GUI classes using InvUI patterns
3. Test security and functionality
4. Deploy updated plugin

## Key Changes from Previous Approach

1. **Security First**: InvUI provides built-in protection against item theft
2. **Professional Architecture**: Separation of GUI logic and display
3. **Maintainable Code**: Builder patterns and structure-based layouts
4. **Future-Proof**: Localization support and extensible item system
5. **Less Custom Code**: Leverage proven library instead of reinventing GUI system

The core plugin functionality (commands, data management, PlaceholderAPI integration) remains unchanged. Only the GUI layer is being replaced for security and maintainability.
