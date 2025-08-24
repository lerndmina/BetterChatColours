package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.utils.ColorUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reorder GUI - allows players to drag and drop colors to reorder gradients
 */
public class ReorderGUI {

  private final BetterChatColours plugin;
  private final Player player;
  private Inventory inventory;
  private final List<NamedTextColor> colors;
  private final ColorSelectionGUI parent;

  // Slot positions
  private static final int[] COLOR_SLOTS = {
      10, 11, 12, 13, 14, 15, 16,
      19, 20, 21, 22, 23, 24, 25
  };
  private static final int BACK_SLOT = 36;
  private static final int CONFIRM_SLOT = 44;

  public ReorderGUI(BetterChatColours plugin, Player player, List<NamedTextColor> colors, ColorSelectionGUI parent) {
    this.plugin = plugin;
    this.player = player;
    this.colors = new ArrayList<>(colors);
    this.parent = parent;
    createInventory();
    populateInventory();
  }

  private void createInventory() {
    String gradient = ColorUtils.createGradientPreviewFromNamedColors(colors, "Preview");
    String title = plugin.getMessagesConfig().getMessage("gui.reorder-title")
        .replace("{gradient_preview}", gradient);
    this.inventory = GUIUtils.createInventory(player, 54, title);
  }

  private void populateInventory() {
    // Clear inventory
    inventory.clear();

    // Add colors to reorder slots
    for (int i = 0; i < Math.min(colors.size(), COLOR_SLOTS.length); i++) {
      NamedTextColor color = colors.get(i);
      Material woolMaterial = ColorUtils.getClosestWool(color.asHexString());
      ItemStack colorItem = GUIUtils.createItem(
          woolMaterial,
          "<color:" + color.asHexString() + ">Color " + (i + 1) + "</color>",
          "<gray>Left click: Move left</gray>",
          "<gray>Right click: Move right</gray>",
          "<gray>Color: " + color.asHexString() + "</gray>");
      inventory.setItem(COLOR_SLOTS[i], colorItem);
    }

    // Add control buttons
    inventory.setItem(BACK_SLOT, createBackButton());
    inventory.setItem(CONFIRM_SLOT, createConfirmButton());

    // Fill empty slots with barriers
    GUIUtils.fillEmptySlots(inventory);
  }

  private ItemStack createBackButton() {
    return GUIUtils.createItem(
        Material.ARROW,
        "<red>Back</red>",
        "<gray>Return to color selection</gray>");
  }

  private ItemStack createConfirmButton() {
    return GUIUtils.createItem(
        Material.LIME_DYE,
        "<green>Confirm Order</green>",
        "<gray>Apply this color order</gray>",
        "<yellow>Current order:</yellow>",
        ColorUtils.createGradientPreviewFromNamedColors(colors, "Preview"));
  }

  /**
   * Handle click events in the reorder GUI
   */
  public void handleClick(int slot, boolean rightClick, boolean shift) {
    if (slot == BACK_SLOT) {
      // Return to color selection
      parent.open();
      return;
    }

    if (slot == CONFIRM_SLOT) {
      // Apply reordered colors and return to selection
      parent.updateSelectedColors(colors);
      parent.open();
      return;
    }

    // Check if clicking on a color slot
    int colorIndex = getColorIndex(slot);
    if (colorIndex != -1) {
      handleColorReorder(colorIndex, rightClick, shift);
    }
  }

  private int getColorIndex(int slot) {
    for (int i = 0; i < COLOR_SLOTS.length; i++) {
      if (COLOR_SLOTS[i] == slot) {
        return i < colors.size() ? i : -1;
      }
    }
    return -1;
  }

  private void handleColorReorder(int colorIndex, boolean rightClick, boolean shift) {
    if (colorIndex < 0 || colorIndex >= colors.size()) {
      return;
    }

    if (rightClick) {
      // Move right/down
      if (colorIndex < colors.size() - 1) {
        Collections.swap(colors, colorIndex, colorIndex + 1);
        updateTitle();
        populateInventory();
      }
    } else {
      // Move left/up
      if (colorIndex > 0) {
        Collections.swap(colors, colorIndex, colorIndex - 1);
        updateTitle();
        populateInventory();
      }
    }
  }

  private void updateTitle() {
    String gradient = ColorUtils.createGradientPreviewFromNamedColors(colors, "Preview");
    String title = plugin.getMessagesConfig().getMessage("gui.reorder-title")
        .replace("{gradient_preview}", gradient);

    // Close and reopen with new title (Bukkit limitation)
    player.closeInventory();
    this.inventory = GUIUtils.createInventory(player, 54, title);
    populateInventory();
    open();
  }

  /**
   * Open the GUI for the player
   */
  public void open() {
    plugin.getGuiListener().registerReorderGUI(player, this);
    player.openInventory(inventory);
  }

  /**
   * Get the current color order
   */
  public List<NamedTextColor> getColors() {
    return new ArrayList<>(colors);
  }

  /**
   * Get the Bukkit inventory
   */
  public Inventory getInventory() {
    return inventory;
  }
}
