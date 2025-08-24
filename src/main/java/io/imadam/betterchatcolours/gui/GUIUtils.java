package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for GUI operations and item creation
 */
public class GUIUtils {

  private static final MiniMessage miniMessage = MiniMessage.miniMessage();

  /**
   * Create an inventory with MiniMessage title support
   */
  public static Inventory createInventory(Player player, int size, String title) {
    Component titleComponent = miniMessage.deserialize(title);
    return Bukkit.createInventory(player, size, titleComponent);
  }

  /**
   * Create an ItemStack with name and lore
   */
  public static ItemStack createItem(Material material, String name, String... lore) {
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();

    if (meta != null) {
      if (name != null) {
        meta.displayName(miniMessage.deserialize(name));
      }

      if (lore.length > 0) {
        List<Component> loreComponents = Arrays.stream(lore)
            .map(miniMessage::deserialize)
            .toList();
        meta.lore(loreComponents);
      }

      item.setItemMeta(meta);
    }

    return item;
  }

  /**
   * Create a colored wool item with gradient preview
   */
  public static ItemStack createColorWool(Material woolMaterial, String colorHex, String displayName,
      boolean selected) {
    String name = selected ? "<green>✓</green> <color:" + colorHex + ">" + displayName + "</color>"
        : "<color:" + colorHex + ">" + displayName + "</color>";

    String[] lore = {
        "<gray>Color: " + colorHex + "</gray>",
        selected ? "<green>Currently selected</green>" : "<yellow>Click to select</yellow>"
    };

    return createItem(woolMaterial, name, lore);
  }

  /**
   * Create a preset item for the main menu
   */
  public static ItemStack createPresetItem(String presetName, List<String> colors, boolean isActive) {
    Material material = isActive ? Material.GLOWSTONE : Material.PAPER;

    String name = isActive ? "<green>✓</green> <gold>" + presetName + "</gold> <green>(Active)</green>"
        : "<gold>" + presetName + "</gold>";

    String gradientPreview = io.imadam.betterchatcolours.utils.ColorUtils.createGradientPreview(colors,
        "Sample Text");

    String[] lore = {
        "<gray>Colors: " + colors.size() + "</gray>",
        "<gray>Preview: " + gradientPreview + "</gray>",
        "",
        isActive ? "<green>This preset is currently active</green>" : "<yellow>Click to activate</yellow>",
        "<red>Right-click to delete</red>"
    };

    return createItem(material, name, lore);
  }

  /**
   * Create navigation button
   */
  public static ItemStack createNavigationButton(String name, Material material, String... lore) {
    return createItem(material, "<aqua>" + name + "</aqua>", lore);
  }

  /**
   * Create border/filler item
   */
  public static ItemStack createFillerItem() {
    return createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
  }

  /**
   * Create the "Create New Gradient" button
   */
  public static ItemStack createNewGradientButton() {
    return createItem(
        Material.EMERALD,
        "<green><bold>Create New Gradient</bold></green>",
        "<gray>Start creating a new color gradient</gray>",
        "<yellow>Click to begin</yellow>");
  }

  /**
   * Create save preset button
   */
  public static ItemStack createSaveButton(boolean enabled) {
    if (enabled) {
      return createItem(
          Material.DIAMOND,
          "<blue><bold>Save Preset</bold></blue>",
          "<gray>Save your gradient as a new preset</gray>",
          "<yellow>Click to save</yellow>");
    } else {
      return createItem(
          Material.BARRIER,
          "<red>Cannot Save</red>",
          "<gray>You need at least one color selected</gray>");
    }
  }

  /**
   * Create back button
   */
  public static ItemStack createBackButton() {
    return createItem(
        Material.ARROW,
        "<yellow>← Back</yellow>",
        "<gray>Return to previous menu</gray>");
  }

  /**
   * Create next button for color selection
   */
  public static ItemStack createNextButton(boolean enabled) {
    if (enabled) {
      return createItem(
          Material.LIME_DYE,
          "<green>Next Color →</green>",
          "<gray>Add another color to your gradient</gray>",
          "<yellow>Click to continue</yellow>");
    } else {
      return createItem(
          Material.GRAY_DYE,
          "<gray>Maximum Colors Reached</gray>",
          "<gray>You've reached the maximum number of colors</gray>");
    }
  }

  /**
   * Create finish button for color selection
   */
  public static ItemStack createFinishButton(boolean enabled) {
    if (enabled) {
      return createItem(
          Material.EMERALD_BLOCK,
          "<green><bold>Finish Gradient</bold></green>",
          "<gray>Complete your gradient with current colors</gray>",
          "<yellow>Click to finish</yellow>");
    } else {
      return createItem(
          Material.REDSTONE_BLOCK,
          "<red>Need More Colors</red>",
          "<gray>You need at least one color</gray>");
    }
  }

  /**
   * Update inventory title with gradient preview
   */
  public static void updateInventoryTitle(Player player, String baseTitle, List<String> selectedColors) {
    // Note: Bukkit doesn't support runtime title changes, so this would require
    // closing and reopening the inventory. For now, we'll handle this in the GUI
    // classes.
  }

  /**
   * Fill empty slots with filler items
   */
  public static void fillEmptySlots(Inventory inventory) {
    ItemStack filler = createFillerItem();
    for (int i = 0; i < inventory.getSize(); i++) {
      if (inventory.getItem(i) == null) {
        inventory.setItem(i, filler);
      }
    }
  }

  /**
   * Check if an item is a filler/border item
   */
  public static boolean isFillerItem(ItemStack item) {
    return item != null && item.getType() == Material.GRAY_STAINED_GLASS_PANE;
  }

  /**
   * Create a cancel button
   */
  public static ItemStack createCancelButton() {
    return createItem(
        Material.REDSTONE,
        "<red><bold>Cancel</bold></red>",
        "<gray>Return to main menu</gray>",
        "<yellow>Click to cancel</yellow>");
  }

  /**
   * Get the plugin instance for static access
   */
  public static BetterChatColours getPlugin() {
    return BetterChatColours.getInstance();
  }
}
