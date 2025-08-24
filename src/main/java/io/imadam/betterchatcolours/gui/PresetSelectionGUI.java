package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.data.GlobalPresetData;
import io.imadam.betterchatcolours.gui.items.BackToMainItem;
import io.imadam.betterchatcolours.gui.items.CloseMenuItem;
import io.imadam.betterchatcolours.gui.items.PresetItem;
import io.imadam.betterchatcolours.gui.items.UnequipPresetItem;
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
import xyz.xenondevs.inventoryaccess.component.ComponentWrapper;

import java.util.List;
import java.util.stream.Collectors;

public class PresetSelectionGUI {

  public static void open(Player player) {
    BetterChatColours plugin = JavaPlugin.getPlugin(BetterChatColours.class);
    boolean isAdmin = player.hasPermission("chatcolor.admin");

    // Check if player's current preset is still valid
    plugin.getUserDataManager().checkAndUnequipInvalidPreset(player);

    // Get available presets for this player
    List<GlobalPresetData> availablePresets = plugin.getGlobalPresetManager()
        .getAllPresets()
        .values()
        .stream()
        .filter(preset -> preset.getPermission() == null ||
            preset.getPermission().isEmpty() ||
            player.hasPermission(preset.getPermission()))
        .sorted((a, b) -> {
          // First sort by color count (descending - higher numbers first)
          int colorCountCompare = Integer.compare(b.getColors().size(), a.getColors().size());
          if (colorCountCompare != 0) {
            return colorCountCompare;
          }
          // Then sort alphabetically (ascending)
          return a.getName().compareToIgnoreCase(b.getName());
        })
        .collect(Collectors.toList());

    // Convert to PresetItems (as Items)
    List<Item> presetItems = availablePresets.stream()
        .map(PresetItem::new)
        .collect(Collectors.toList());

    Structure structure = new Structure(
        "# # # # # # # # #",
        "# x x x x x x x #",
        "# x x x x x x x #",
        "# x x x x x x x #",
        "# x x x x x x x #",
        "# u # < # > # b #")
        .addIngredient('#', GUIUtils.createGlassPane())
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .addIngredient('u', new UnequipPresetItem()) // Unequip button
        .addIngredient('<', new PreviousPageItem()) // Previous page
        .addIngredient('>', new NextPageItem()) // Next page
        .addIngredient('b', isAdmin ? new BackToMainItem() : new CloseMenuItem());

    PagedGui<Item> gui = PagedGui.items()
        .setStructure(structure)
        .setContent(presetItems)
        .build();

    // Create title with current page info
    String title = "§8Available Presets §7(Page " + (gui.getCurrentPage() + 1) + "/" + gui.getPageAmount() + ")";

    Window window = Window.single()
        .setViewer(player)
        .setTitle(title)
        .setGui(gui)
        .build();

    window.open();
  }

  // Pagination control items using InvUI's built-in PageItem
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
