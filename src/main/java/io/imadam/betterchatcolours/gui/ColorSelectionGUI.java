package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.utils.ColorUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Color selection GUI - allows players to choose colors for gradients
 */
public class ColorSelectionGUI {

  private final BetterChatColours plugin;
  private final Player player;
  private Inventory inventory;
  private final List<NamedTextColor> selectedColors;
  private String presetName;
  private boolean isEditing;

  // Slot positions
  private static final int[] COLOR_SLOTS = {
      10, 11, 12, 13, 14, 15, 16,
      19, 20, 21, 22, 23, 24, 25,
      28, 29, 30, 31, 32, 33, 34
  };

  private static final int PREVIEW_SLOT = 4;
  private static final int SAVE_SLOT = 40;
  private static final int CANCEL_SLOT = 44;
  private static final int CLEAR_SLOT = 39;
  private static final int REORDER_SLOT = 41;

  // Available colors
  private static final NamedTextColor[] AVAILABLE_COLORS = {
      NamedTextColor.RED, NamedTextColor.DARK_RED,
      NamedTextColor.GOLD, NamedTextColor.YELLOW,
      NamedTextColor.GREEN, NamedTextColor.DARK_GREEN,
      NamedTextColor.AQUA, NamedTextColor.DARK_AQUA,
      NamedTextColor.BLUE, NamedTextColor.DARK_BLUE,
      NamedTextColor.LIGHT_PURPLE, NamedTextColor.DARK_PURPLE,
      NamedTextColor.WHITE, NamedTextColor.GRAY,
      NamedTextColor.DARK_GRAY, NamedTextColor.BLACK
  };

  public ColorSelectionGUI(BetterChatColours plugin, Player player) {
    this.plugin = plugin;
    this.player = player;
    this.selectedColors = new ArrayList<>();
    this.presetName = "";
    this.isEditing = false;
    createInventory();
    populateInventory();
  }

  public ColorSelectionGUI(BetterChatColours plugin, Player player, String existingPresetName,
      List<NamedTextColor> existingColors) {
    this.plugin = plugin;
    this.player = player;
    this.selectedColors = new ArrayList<>(existingColors);
    this.presetName = existingPresetName;
    this.isEditing = true;
    createInventory();
    populateInventory();
  }

  private void createInventory() {
    String title = isEditing ? plugin.getMessagesConfig().getMessage("gui.edit-gradient-title")
        : plugin.getMessagesConfig().getMessage("gui.create-gradient-title");
    this.inventory = GUIUtils.createInventory(player, 54, title);
  }

  private void populateInventory() {
    inventory.clear();

    // Add available colors
    for (int i = 0; i < Math.min(AVAILABLE_COLORS.length, COLOR_SLOTS.length); i++) {
      NamedTextColor color = AVAILABLE_COLORS[i];
      boolean isSelected = selectedColors.contains(color);

      ItemStack colorItem = createColorItem(color, isSelected);
      inventory.setItem(COLOR_SLOTS[i], colorItem);
    }

    // Preview item
    updatePreview();

    // Control buttons
    inventory.setItem(SAVE_SLOT, createSaveButton());
    inventory.setItem(CANCEL_SLOT, GUIUtils.createCancelButton());
    inventory.setItem(CLEAR_SLOT, createClearButton());

    if (selectedColors.size() > 1) {
      inventory.setItem(REORDER_SLOT, createReorderButton());
    }

    // Fill empty slots
    GUIUtils.fillEmptySlots(inventory);
  }

  private ItemStack createColorItem(NamedTextColor color, boolean isSelected) {
    Material woolMaterial = ColorUtils.getWoolMaterial(color);
    String colorName = ColorUtils.getColorName(color);

    String displayName = isSelected ? "<green>✓ " + colorName + "</green>"
        : "<" + color.asHexString() + ">" + colorName + "</" + color.asHexString() + ">";

    String[] lore = isSelected ? new String[] {
        "<gray>Currently selected</gray>",
        "<yellow>Click to remove</yellow>"
    }
        : new String[] {
            "<gray>Click to add to gradient</gray>"
        };

    return GUIUtils.createItem(woolMaterial, displayName, lore);
  }

  private void updatePreview() {
    if (selectedColors.isEmpty()) {
      ItemStack noPreview = GUIUtils.createItem(
          Material.BARRIER,
          "<red>No Colors Selected</red>",
          new String[] {
              "<gray>Select colors to see preview</gray>"
          });
      inventory.setItem(PREVIEW_SLOT, noPreview);
    } else {
      String previewText = ColorUtils.createGradientPreviewFromNamedColors(selectedColors, "Sample Text");

      ItemStack preview = GUIUtils.createItem(
          Material.PAPER,
          "<white>Gradient Preview</white>",
          new String[] {
              previewText,
              "",
              "<gray>Colors: " + selectedColors.size() + "</gray>"
          });
      inventory.setItem(PREVIEW_SLOT, preview);
    }
  }

  private ItemStack createSaveButton() {
    boolean canSave = selectedColors.size() >= 1; // Allow single colors

    if (!canSave) {
      return GUIUtils.createItem(
          Material.GRAY_DYE,
          "<gray>Save Gradient</gray>",
          new String[] {
              "<red>Need at least 1 color</red>"
          });
    }

    String type = selectedColors.size() == 1 ? "Single Color" : "Gradient";
    return GUIUtils.createItem(
        Material.LIME_DYE,
        "<green>Save " + type + "</green>",
        new String[] {
            "<gray>Colors: " + selectedColors.size() + "</gray>",
            "<gray>Type: " + type + "</gray>",
            "<yellow>Click to save</yellow>"
        });
  }

  private ItemStack createClearButton() {
    return GUIUtils.createItem(
        Material.RED_DYE,
        "<red>Clear All Colors</red>",
        new String[] {
            "<gray>Remove all selected colors</gray>"
        });
  }

  private ItemStack createReorderButton() {
    return GUIUtils.createItem(
        Material.ARROW,
        "<yellow>Reorder Colors</yellow>",
        new String[] {
            "<gray>Change the order of colors</gray>",
            "<yellow>Click to open reorder menu</yellow>"
        });
  }

  /**
   * Handle click events in the color selection GUI
   */
  public void handleClick(int slot, boolean rightClick) {
    // Check color slots
    for (int i = 0; i < Math.min(COLOR_SLOTS.length, AVAILABLE_COLORS.length); i++) {
      if (COLOR_SLOTS[i] == slot) {
        toggleColor(AVAILABLE_COLORS[i]);
        return;
      }
    }

    // Control buttons
    switch (slot) {
      case SAVE_SLOT:
        if (selectedColors.size() >= 1) { // Allow single colors
          openSaveDialog();
        }
        break;

      case CANCEL_SLOT:
        new MainMenuGUI(plugin, player).open();
        break;

      case CLEAR_SLOT:
        selectedColors.clear();
        populateInventory();
        break;

      case REORDER_SLOT:
        if (selectedColors.size() > 1) {
          // Open reorder GUI
          new ReorderGUI(plugin, player, selectedColors, this).open();
        }
        break;
    }
  }

  private void toggleColor(NamedTextColor color) {
    if (selectedColors.contains(color)) {
      selectedColors.remove(color);
    } else {
      selectedColors.add(color);
    }
    populateInventory();
  }

  private void openSaveDialog() {
    // Allow single colors now - validate only for empty selection
    if (selectedColors.isEmpty()) {
      plugin.getMessagesConfig().sendMessage(player, "errors.no-colors-selected");
      return;
    }

    if (selectedColors.size() > plugin.getConfigManager().getMaxColorsPerGradient()) {
      player.sendMessage(
          "<red>Too many colors! Maximum: " + plugin.getConfigManager().getMaxColorsPerGradient() + "</red>");
      return;
    }

    // Convert NamedTextColor to hex strings for the new system
    List<String> colorHexes = new ArrayList<>();
    for (NamedTextColor color : selectedColors) {
      colorHexes.add(color.asHexString());
    }

    // Close inventory and open preset naming GUI
    player.closeInventory();

    if (isEditing && presetName != null) {
      // For editing, we need to get the existing preset data
      // For now, create a new GlobalPresetData with existing info
      // This will be enhanced when we have full GlobalPresetData integration
      plugin.getLogger().info("Editing preset: " + presetName + " (simplified for now)");
      boolean success = plugin.getDataManager().updatePresetFromNamedColors(player, presetName, selectedColors);

      if (success) {
        plugin.getMessagesConfig().sendMessage(player, "gui.preset-updated", "preset", presetName);
      } else {
        plugin.getMessagesConfig().sendMessage(player, "errors.preset-update-failed");
      }

      new MainMenuGUI(plugin, player).open();
    } else {
      // Create new preset with AnvilGUI naming
      new PresetNamingGUI(plugin, player, colorHexes).open();
    }

    // Return to main menu after a short delay
    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
      new MainMenuGUI(plugin, player).open();
    }, 20L); // 1 second delay
  }

  /**
   * Open the GUI for the player
   */
  public void open() {
    plugin.getGuiListener().registerColorGUI(player, this);
    player.openInventory(inventory);
  }

  /**
   * Update selected colors (used by ReorderGUI)
   */
  public void updateSelectedColors(List<NamedTextColor> newColors) {
    selectedColors.clear();
    selectedColors.addAll(newColors);
    populateInventory(); // Use the existing method name
  }

  /**
   * Get the Bukkit inventory
   */
  public Inventory getInventory() {
    return inventory;
  }

  /**
   * Check if an inventory belongs to this GUI
   */
  public boolean isInventory(Inventory inv) {
    return inventory.equals(inv);
  }
}
