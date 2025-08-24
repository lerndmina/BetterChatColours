package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AdminPresetCreateGUI {

  private static final String TITLE = "Create New Preset";

  public static void open(Player player) {
    // Request preset name via chat
    ChatInputManager.requestPresetName(player,
        presetName -> openColorSelectionGUI(player, presetName, new ArrayList<>()),
        () -> FallbackMainMenuGUI.open(player));
  }

  public static void openColorSelectionGUI(Player player, String presetName, List<String> colors) {
    // Create inventory with dynamic size based on number of colors
    int inventorySize = Math.max(54, ((colors.size() + 8) / 9) * 9);
    Inventory inv = Bukkit.createInventory(null, inventorySize, Component.text(TITLE + ": " + presetName));

    // Fill with glass panes
    ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemMeta glassMeta = glass.getItemMeta();
    glassMeta.displayName(Component.text(" "));
    glass.setItemMeta(glassMeta);

    for (int i = 0; i < inventorySize; i++) {
      inv.setItem(i, glass);
    }

    // Add color items
    for (int i = 0; i < colors.size(); i++) {
      String color = colors.get(i);
      ItemStack colorItem = createColorItem(color, i + 1);
      inv.setItem(i, colorItem);
    }

    // Add control items in the bottom row
    int bottomRow = inventorySize - 9;

    // Add Color button
    ItemStack addColor = new ItemStack(Material.LIME_DYE);
    ItemMeta addMeta = addColor.getItemMeta();
    addMeta.displayName(Component.text("Add Color", NamedTextColor.GREEN));
    List<Component> addLore = new ArrayList<>();
    addLore.add(Component.text("Click to add a new color", NamedTextColor.GRAY));
    addLore.add(Component.text("to this gradient", NamedTextColor.GRAY));
    addMeta.lore(addLore);
    addColor.setItemMeta(addMeta);
    inv.setItem(bottomRow + 1, addColor);

    // Permission button
    ItemStack permission = new ItemStack(Material.NAME_TAG);
    ItemMeta permMeta = permission.getItemMeta();
    permMeta.displayName(Component.text("Set Permission", NamedTextColor.YELLOW));
    List<Component> permLore = new ArrayList<>();
    permLore.add(Component.text("Click to set required", NamedTextColor.GRAY));
    permLore.add(Component.text("permission for this preset", NamedTextColor.GRAY));
    permMeta.lore(permLore);
    permission.setItemMeta(permMeta);
    inv.setItem(bottomRow + 3, permission);

    // Preview button (shows gradient preview)
    if (!colors.isEmpty()) {
      ItemStack preview = new ItemStack(Material.PAPER);
      ItemMeta previewMeta = preview.getItemMeta();
      previewMeta.displayName(applyGradientToText("Preview: " + presetName, colors));
      List<Component> previewLore = new ArrayList<>();
      previewLore.add(Component.text("This is how the gradient", NamedTextColor.GRAY));
      previewLore.add(Component.text("will look in game", NamedTextColor.GRAY));
      previewMeta.lore(previewLore);
      preview.setItemMeta(previewMeta);
      inv.setItem(bottomRow + 4, preview);
    }

    // Save button (only if we have colors)
    if (!colors.isEmpty()) {
      ItemStack save = new ItemStack(Material.EMERALD);
      ItemMeta saveMeta = save.getItemMeta();
      saveMeta.displayName(Component.text("Save Preset", NamedTextColor.GREEN));
      List<Component> saveLore = new ArrayList<>();
      saveLore.add(Component.text("Click to save this preset", NamedTextColor.GRAY));
      saveLore.add(Component.text("Colors: " + colors.size(), NamedTextColor.DARK_GRAY));
      saveMeta.lore(saveLore);
      save.setItemMeta(saveMeta);
      inv.setItem(bottomRow + 6, save);
    }

    // Cancel button
    ItemStack cancel = new ItemStack(Material.BARRIER);
    ItemMeta cancelMeta = cancel.getItemMeta();
    cancelMeta.displayName(Component.text("Cancel", NamedTextColor.RED));
    List<Component> cancelLore = new ArrayList<>();
    cancelLore.add(Component.text("Click to cancel and", NamedTextColor.GRAY));
    cancelLore.add(Component.text("return to main menu", NamedTextColor.GRAY));
    cancelMeta.lore(cancelLore);
    cancel.setItemMeta(cancelMeta);
    inv.setItem(bottomRow + 8, cancel);

    player.openInventory(inv);
  }

  private static ItemStack createColorItem(String hexColor, int position) {
    Material dyeColor = getClosestDyeColor(hexColor);
    ItemStack item = new ItemStack(dyeColor);
    ItemMeta meta = item.getItemMeta();

    meta.displayName(Component.text("Color " + position + ": " + hexColor, NamedTextColor.WHITE));

    List<Component> lore = new ArrayList<>();
    lore.add(Component.text(""));
    // Create a color preview using MiniMessage
    try {
      String colorPreview = "<color:" + hexColor + ">████████████████</color>";
      var component = MiniMessage.miniMessage().deserialize(colorPreview);
      lore.add(component);
    } catch (Exception e) {
      lore.add(Component.text("████████████████", NamedTextColor.GRAY));
    }
    lore.add(Component.text(""));
    lore.add(Component.text("Left-click to edit", NamedTextColor.GREEN));
    lore.add(Component.text("Right-click to remove", NamedTextColor.RED));
    lore.add(Component.text("__COLOR_INDEX__" + (position - 1), NamedTextColor.DARK_GRAY));

    meta.lore(lore);
    item.setItemMeta(meta);
    return item;
  }

  private static Component applyGradientToText(String text, List<String> colors) {
    if (colors == null || colors.isEmpty()) {
      return Component.text(text, NamedTextColor.YELLOW);
    }

    try {
      StringBuilder gradient = new StringBuilder("<gradient:");
      for (int i = 0; i < colors.size(); i++) {
        if (i > 0)
          gradient.append(":");
        gradient.append(colors.get(i));
      }
      gradient.append(">").append(text).append("</gradient>");

      return MiniMessage.miniMessage().deserialize(gradient.toString());
    } catch (Exception e) {
      return Component.text(text, NamedTextColor.YELLOW);
    }
  }

  private static Material getClosestDyeColor(String hexColor) {
    if (hexColor == null || hexColor.isEmpty()) {
      return Material.WHITE_DYE;
    }

    // Remove # if present
    if (hexColor.startsWith("#")) {
      hexColor = hexColor.substring(1);
    }

    try {
      int rgb = Integer.parseInt(hexColor, 16);
      int r = (rgb >> 16) & 0xFF;
      int g = (rgb >> 8) & 0xFF;
      int b = rgb & 0xFF;

      // Simple color matching logic
      if (r > 200 && g < 100 && b < 100)
        return Material.RED_DYE;
      if (r > 200 && g > 150 && b < 100)
        return Material.ORANGE_DYE;
      if (r > 200 && g > 200 && b < 100)
        return Material.YELLOW_DYE;
      if (r < 100 && g > 150 && b < 100)
        return Material.LIME_DYE;
      if (r < 100 && g > 100 && b < 100)
        return Material.GREEN_DYE;
      if (r < 100 && g > 150 && b > 150)
        return Material.CYAN_DYE;
      if (r < 100 && g < 150 && b > 200)
        return Material.BLUE_DYE;
      if (r > 150 && g < 100 && b > 150)
        return Material.MAGENTA_DYE;
      if (r > 150 && g < 150 && b > 150)
        return Material.PINK_DYE;
      if (r > 150 && g > 150 && b > 150)
        return Material.WHITE_DYE;
      if (r < 50 && g < 50 && b < 50)
        return Material.BLACK_DYE;
      if (r < 100 && g < 100 && b < 100)
        return Material.GRAY_DYE;

      return Material.LIGHT_GRAY_DYE;

    } catch (NumberFormatException e) {
      return Material.WHITE_DYE;
    }
  }
}
