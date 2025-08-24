package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.gui.items.preset.AddColorItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;

public class InvUIAdminPresetCreateGUI {

    public static void openForCreation(Player player) {
        // Request preset name via chat first, then open the color selection GUI
        ChatInputManager.requestPresetName(player,
                presetName -> openColorSelectionGUI(player, presetName, new ArrayList<>(), false),
                () -> MainMenuGUI.open(player));
    }

    public static void openForEditing(Player player, String presetName, List<String> colors) {
        openColorSelectionGUI(player, presetName, new ArrayList<>(colors), true);
    }

    public static void reopenColorSelectionGUI(Player player, String presetName, List<String> colors, boolean isEditMode) {
        openColorSelectionGUI(player, presetName, colors, isEditMode);
    }

    private static void openColorSelectionGUI(Player player, String presetName, List<String> colors, boolean isEditMode) {
        // Create structure for color display
        Structure structure = new Structure(
                "# # # # # # # # #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# a # p # s # c #")
                .addIngredient('#', GUIUtils.createGlassPane())
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('a', new AddColorItem(presetName, colors, isEditMode))
                .addIngredient('p', new PreviewItem(presetName, colors))
                .addIngredient('s', new SavePresetItem(presetName, colors, isEditMode))
                .addIngredient('c', new CancelItem(isEditMode));

        // Create color items for the dynamic content
        List<Item> colorItems = new ArrayList<>();
        for (int i = 0; i < colors.size(); i++) {
            colorItems.add(new ColorSlotItem(presetName, colors, i, isEditMode));
        }

        // Use PagedGui for dynamic content
        PagedGui<Item> gui = PagedGui.items()
                .setStructure(structure)
                .setContent(colorItems)
                .build();

        String title = (isEditMode ? "Edit Preset: " : "Create Preset: ") + presetName;
        
        Window window = Window.single()
                .setViewer(player)
                .setTitle(title)
                .setGui(gui)
                .build();

        window.open();
    }

    // Inner classes for GUI items
    private static class PreviewItem extends AbstractItem {
        private final String presetName;
        private final List<String> colors;

        public PreviewItem(String presetName, List<String> colors) {
            this.presetName = presetName;
            this.colors = colors;
        }

        @Override
        public ItemProvider getItemProvider() {
            ItemBuilder builder = new ItemBuilder(Material.PAPER)
                    .setDisplayName("§e§lPreview");
            
            if (!colors.isEmpty()) {
                Component preview = GUIUtils.createGradientText("Preview: " + presetName, colors);
                builder.addLoreLines("§7Gradient preview:", preview.toString());
            } else {
                builder.addLoreLines("§7Add colors to see preview");
            }
            
            return builder;
        }

        @Override
        public void handleClick(org.bukkit.event.inventory.ClickType clickType, Player player, org.bukkit.event.inventory.InventoryClickEvent event) {
            // Do nothing - this is just a preview
        }
    }

    private static class SavePresetItem extends AbstractItem {
        private final String presetName;
        private final List<String> colors;
        private final boolean isEditMode;

        public SavePresetItem(String presetName, List<String> colors, boolean isEditMode) {
            this.presetName = presetName;
            this.colors = colors;
            this.isEditMode = isEditMode;
        }

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.NAME_TAG)
                    .setDisplayName("§a§lSave Preset")
                    .addLoreLines(
                            "§7Click to save preset with",
                            "§7auto-generated permission:",
                            "§bchatcolor.preset." + presetName.toLowerCase()
                    );
        }

        @Override
        public void handleClick(org.bukkit.event.inventory.ClickType clickType, Player player, org.bukkit.event.inventory.InventoryClickEvent event) {
            if (colors.isEmpty()) {
                player.sendMessage(Component.text("Cannot save preset without colors!", NamedTextColor.RED));
                return;
            }

            BetterChatColours plugin = JavaPlugin.getPlugin(BetterChatColours.class);
            plugin.getGlobalPresetManager().addPreset(presetName, colors);
            
            String permission = "chatcolor.preset." + presetName.toLowerCase();
            String action = isEditMode ? "updated" : "created";
            player.sendMessage(Component.text("Preset '" + presetName + "' " + action + " successfully! Permission: " + permission, NamedTextColor.GREEN));
            
            player.closeInventory();
            if (isEditMode) {
                InvUIAdminPresetEditGUI.open(player);
            }
        }
    }

    private static class CancelItem extends AbstractItem {
        private final boolean isEditMode;

        public CancelItem(boolean isEditMode) {
            this.isEditMode = isEditMode;
        }

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.BARRIER)
                    .setDisplayName("§c§lCancel")
                    .addLoreLines("§7Click to cancel and return");
        }

        @Override
        public void handleClick(org.bukkit.event.inventory.ClickType clickType, Player player, org.bukkit.event.inventory.InventoryClickEvent event) {
            player.closeInventory();
            if (isEditMode) {
                InvUIAdminPresetEditGUI.open(player);
            } else {
                MainMenuGUI.open(player);
            }
        }
    }

    private static class ColorSlotItem extends AbstractItem {
        private final String presetName;
        private final List<String> colors;
        private final int index;
        private final boolean isEditMode;

        public ColorSlotItem(String presetName, List<String> colors, int index, boolean isEditMode) {
            this.presetName = presetName;
            this.colors = colors;
            this.index = index;
            this.isEditMode = isEditMode;
        }

        @Override
        public ItemProvider getItemProvider() {
            String hexColor = colors.get(index);
            Material dyeColor = GUIUtils.getClosestDyeColor(hexColor);
            
            return new ItemBuilder(dyeColor)
                    .setDisplayName("§f§lColor " + (index + 1))
                    .addLoreLines(
                            "§7Hex: §f" + hexColor,
                            "",
                            "§eLeft click: §7Edit color",
                            "§eRight click: §7Remove color"
                    );
        }

        @Override
        public void handleClick(org.bukkit.event.inventory.ClickType clickType, Player player, org.bukkit.event.inventory.InventoryClickEvent event) {
            if (clickType.isLeftClick()) {
                // Edit color
                ChatInputManager.requestHexColorEdit(player, presetName, colors, index,
                        hexColor -> {
                            colors.set(index, hexColor);
                            reopenColorSelectionGUI(player, presetName, colors, isEditMode);
                        },
                        () -> reopenColorSelectionGUI(player, presetName, colors, isEditMode));
            } else if (clickType.isRightClick()) {
                // Remove color
                colors.remove(index);
                reopenColorSelectionGUI(player, presetName, colors, isEditMode);
            }
        }
    }
}
