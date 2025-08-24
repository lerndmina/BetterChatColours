package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.data.GlobalPresetData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InvUIAdminPresetEditGUI {

    public static void open(Player player) {
        BetterChatColours plugin = JavaPlugin.getPlugin(BetterChatColours.class);
        Map<String, GlobalPresetData> allPresets = plugin.getGlobalPresetManager().getAllPresets();

        // Create preset items
        List<Item> presetItems = new ArrayList<>();
        for (GlobalPresetData preset : allPresets.values()) {
            presetItems.add(new EditablePresetItem(preset));
        }

        Structure structure = new Structure(
                "# # # # # # # # #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# < # # # # # > #",
                "# # # b # # # # #")
                .addIngredient('#', GUIUtils.createGlassPane())
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('<', new PreviousPageItem())
                .addIngredient('>', new NextPageItem())
                .addIngredient('b', new BackToMainItem());

        PagedGui<Item> gui = PagedGui.items()
                .setStructure(structure)
                .setContent(presetItems)
                .build();

        Window window = Window.single()
                .setViewer(player)
                .setTitle("§8Edit Presets")
                .setGui(gui)
                .build();

        window.open();
    }

    private static class EditablePresetItem extends AbstractItem {
        private final GlobalPresetData preset;

        public EditablePresetItem(GlobalPresetData preset) {
            this.preset = preset;
        }

        @Override
        public ItemProvider getItemProvider() {
            // Create gradient preview for display name
            Component displayName = GUIUtils.createGradientText(preset.getName(), preset.getColors());
            
            return new ItemBuilder(Material.PAPER)
                    .setDisplayName(displayName.toString())
                    .addLoreLines(
                            "§7Colors: §f" + preset.getColors().size(),
                            "§7Permission: §f" + preset.getPermission(),
                            "",
                            "§eLeft click: §7Edit preset",
                            "§eRight click: §7Delete preset"
                    );
        }

        @Override
        public void handleClick(org.bukkit.event.inventory.ClickType clickType, Player player, org.bukkit.event.inventory.InventoryClickEvent event) {
            if (clickType.isLeftClick()) {
                // Edit the preset
                player.closeInventory();
                InvUIAdminPresetCreateGUI.openForEditing(player, preset.getName(), preset.getColors());
            } else if (clickType.isRightClick()) {
                // Delete the preset
                deletePreset(player, preset.getName());
            }
        }

        private void deletePreset(Player player, String presetName) {
            BetterChatColours plugin = JavaPlugin.getPlugin(BetterChatColours.class);
            plugin.getGlobalPresetManager().removePreset(presetName);
            
            player.sendMessage(Component.text("Preset '" + presetName + "' deleted successfully!", NamedTextColor.GREEN));
            
            // Refresh the GUI
            player.closeInventory();
            InvUIAdminPresetEditGUI.open(player);
        }
    }

    private static class BackToMainItem extends AbstractItem {
        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.ARROW)
                    .setDisplayName("§c§lBack to Main Menu")
                    .addLoreLines("§7Click to return to main menu");
        }

        @Override
        public void handleClick(org.bukkit.event.inventory.ClickType clickType, Player player, org.bukkit.event.inventory.InventoryClickEvent event) {
            player.closeInventory();
            MainMenuGUI.open(player);
        }
    }

    private static class PreviousPageItem extends AbstractItem {
        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.ARROW)
                    .setDisplayName("§e§lPrevious Page")
                    .addLoreLines("§7Click to go to previous page");
        }

        @Override
        public void handleClick(org.bukkit.event.inventory.ClickType clickType, Player player, org.bukkit.event.inventory.InventoryClickEvent event) {
            // InvUI handles pagination automatically for PagedGui
            // This is just a placeholder for visual consistency
        }
    }

    private static class NextPageItem extends AbstractItem {
        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.ARROW)
                    .setDisplayName("§e§lNext Page")
                    .addLoreLines("§7Click to go to next page");
        }

        @Override
        public void handleClick(org.bukkit.event.inventory.ClickType clickType, Player player, org.bukkit.event.inventory.InventoryClickEvent event) {
            // InvUI handles pagination automatically for PagedGui
            // This is just a placeholder for visual consistency
        }
    }
}
