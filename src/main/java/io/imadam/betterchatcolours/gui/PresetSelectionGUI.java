package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.data.GlobalPresetData;
import io.imadam.betterchatcolours.gui.items.PresetItem;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.List;
import java.util.stream.Collectors;

public class PresetSelectionGUI {

  public static void open(Player player) {
    BetterChatColours plugin = JavaPlugin.getPlugin(BetterChatColours.class);

    // Get available presets for this player
    List<GlobalPresetData> availablePresets = plugin.getGlobalPresetManager()
        .getAllPresets()
        .values()
        .stream()
        .filter(preset -> preset.getPermission() == null ||
            preset.getPermission().isEmpty() ||
            player.hasPermission(preset.getPermission()))
        .collect(Collectors.toList());

    // Convert to PresetItems (as Items)
    List<Item> presetItems = availablePresets.stream()
        .map(PresetItem::new)
        .collect(Collectors.toList());

    Structure structure = new Structure(
        "# # # # # # # # #",
        "# x x x x x x x #",
        "# x x x x x x x #",
        "# # # < # > # # #")
        .addIngredient('#', GUIUtils.createGlassPane())
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .addIngredient('<', GUIUtils.createGlassPane()) // Use glass pane for now
        .addIngredient('>', GUIUtils.createGlassPane()); // Use glass pane for now

    PagedGui<Item> gui = PagedGui.items()
        .setStructure(structure)
        .setContent(presetItems)
        .build();

    Window window = Window.single()
        .setViewer(player)
        .setTitle("§8Available Presets")
        .setGui(gui)
        .build();

    window.open();
  }
}
