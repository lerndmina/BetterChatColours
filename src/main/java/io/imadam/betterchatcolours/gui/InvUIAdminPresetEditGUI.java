package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.data.GlobalPresetData;
import io.imadam.betterchatcolours.gui.GUIUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InvUIAdminPresetEditGUI {

    public static void open(Player player) {
        BetterChatColours plugin = JavaPlugin.getPlugin(BetterChatColours.class);
        Map<String, GlobalPresetData> allPresets = plugin.getGlobalPresetManager().getAllPresets();

        // Create preset items and sort alphabetically
        List<Item> presetItems = allPresets.values().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())) // Alphabetical sort
                .map(EditablePresetItem::new)
                .collect(Collectors.toList());

        Structure structure = new Structure(
                "# # # # # # # # #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# < # # b # # > #")
                .addIngredient('#', GUIUtils.createGlassPane())
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('<', new PreviousPageItem())
                .addIngredient('>', new NextPageItem())
                .addIngredient('b', new BackToMainItem());

        PagedGui<Item> gui = PagedGui.items()
                .setStructure(structure)
                .setContent(presetItems)
                .build();

        // Create title with current page info
        String title = "§8Edit Presets §7(Page " + (gui.getCurrentPage() + 1) + "/" + gui.getPageAmount() + ")";

        Window window = Window.single()
                .setViewer(player)
                .setTitle(title)
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
            // Create gradient preview for display name using the same method as PresetItem
            String gradientName = applyGradientToText(preset.getName());
            
            // Get the material based on the first color in the gradient
            Material iconMaterial = getIconMaterial();
            
            return new ItemBuilder(iconMaterial)
                    .setDisplayName(gradientName)
                    .addLoreLines(
                            "§7Colors: §f" + preset.getColors().size(),
                            "§7Permission: §f" + preset.getPermission(),
                            "",
                            "§eLeft click: §7Edit preset",
                            "§eRight click: §7Delete preset"
                    );
        }

        private Material getIconMaterial() {
            if (preset.getColors() == null || preset.getColors().isEmpty()) {
                return Material.PAPER; // Fallback for presets with no colors
            }
            
            // Use the first color in the gradient to determine the icon
            String firstColor = preset.getColors().get(0);
            return GUIUtils.getClosestConcreteColor(firstColor);
        }

        private String applyGradientToText(String text) {
            if (preset.getColors() == null || preset.getColors().isEmpty()) {
                return "§e§l" + text;
            }

            try {
                String gradientMessage = preset.getGradientTag() + text + preset.getClosingTag();
                var component = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(gradientMessage);
                return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(component);
            } catch (Exception e) {
                // Fallback to yellow text if gradient parsing fails
                return "§e§l" + text;
            }
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

    private static class PreviousPageItem extends PageItem {
        public PreviousPageItem() {
            super(false); // false = previous page
        }

        @Override
        public ItemProvider getItemProvider(PagedGui<?> gui) {
            return new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                    .setDisplayName("§e§lPrevious Page")
                    .addLoreLines(
                            gui.hasPreviousPage() 
                                ? "§7Go to page " + gui.getCurrentPage() + "/" + gui.getPageAmount()
                                : "§7You can't go further back"
                    );
        }
    }

    private static class NextPageItem extends PageItem {
        public NextPageItem() {
            super(true); // true = next page
        }

        @Override
        public ItemProvider getItemProvider(PagedGui<?> gui) {
            return new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                    .setDisplayName("§e§lNext Page")
                    .addLoreLines(
                            gui.hasNextPage() 
                                ? "§7Go to page " + (gui.getCurrentPage() + 2) + "/" + gui.getPageAmount()
                                : "§7There are no more pages"
                    );
        }
    }
}
