package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.data.GlobalPresetData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class AdminPresetEditGUI {

  private static final String TITLE = "Edit Presets";

  public static void open(Player player) {
    BetterChatColours plugin = JavaPlugin.getPlugin(BetterChatColours.class);
    List<GlobalPresetData> allPresets = new ArrayList<>(plugin.getGlobalPresetManager().getAllPresets().values());

    if (allPresets.isEmpty()) {
      player.sendMessage(Component.text("[INFO] No presets found! Create some presets first.", NamedTextColor.YELLOW));
      FallbackMainMenuGUI.open(player);
      return;
    }

    // Calculate inventory size (multiples of 9, with space for navigation)
    int presetsPerPage = 45; // 5 rows of 9 items
    int totalPages = (allPresets.size() + presetsPerPage - 1) / presetsPerPage;

    openPage(player, 1, totalPages, allPresets);
  }

  public static void openPage(Player player, int page, int totalPages, List<GlobalPresetData> allPresets) {
    Inventory inv = Bukkit.createInventory(null, 54, Component.text(TITLE + " (Page " + page + "/" + totalPages + ")"));

    // Fill with glass panes
    ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemMeta glassMeta = glass.getItemMeta();
    glassMeta.displayName(Component.text(" "));
    glass.setItemMeta(glassMeta);

    for (int i = 0; i < 54; i++) {
      inv.setItem(i, glass);
    }

    // Calculate preset range for this page
    int presetsPerPage = 45;
    int startIndex = (page - 1) * presetsPerPage;
    int endIndex = Math.min(startIndex + presetsPerPage, allPresets.size());

    // Add preset items (5 rows)
    int slot = 0;
    for (int i = startIndex; i < endIndex; i++) {
      // Skip bottom row (reserved for navigation)
      if (slot >= 45)
        break;

      GlobalPresetData preset = allPresets.get(i);
      ItemStack presetItem = createPresetEditItem(preset);
      inv.setItem(slot, presetItem);
      slot++;
    }

    // Navigation items (bottom row)
    if (page > 1) {
      // Previous page button
      ItemStack prevButton = new ItemStack(Material.ARROW);
      ItemMeta prevMeta = prevButton.getItemMeta();
      prevMeta.displayName(Component.text("Previous Page", NamedTextColor.GREEN));
      List<Component> prevLore = new ArrayList<>();
      prevLore.add(Component.text("Go to page " + (page - 1), NamedTextColor.GRAY));
      prevMeta.lore(prevLore);
      prevButton.setItemMeta(prevMeta);
      inv.setItem(45, prevButton);
    }

    if (page < totalPages) {
      // Next page button
      ItemStack nextButton = new ItemStack(Material.ARROW);
      ItemMeta nextMeta = nextButton.getItemMeta();
      nextMeta.displayName(Component.text("Next Page", NamedTextColor.GREEN));
      List<Component> nextLore = new ArrayList<>();
      nextLore.add(Component.text("Go to page " + (page + 1), NamedTextColor.GRAY));
      nextMeta.lore(nextLore);
      nextButton.setItemMeta(nextMeta);
      inv.setItem(53, nextButton);
    }

    // Back to main menu button
    ItemStack backButton = new ItemStack(Material.BARRIER);
    ItemMeta backMeta = backButton.getItemMeta();
    backMeta.displayName(Component.text("Back to Main Menu", NamedTextColor.RED));
    backButton.setItemMeta(backMeta);
    inv.setItem(49, backButton);

    player.openInventory(inv);
  }

  private static ItemStack createPresetEditItem(GlobalPresetData preset) {
    ItemStack item = new ItemStack(Material.PAPER);
    ItemMeta meta = item.getItemMeta();

    // Create gradient title
    String gradientTitle = createGradientText(preset.getName(), preset.getColors());
    Component title = MiniMessage.miniMessage().deserialize(gradientTitle);
    meta.displayName(title);

    List<Component> lore = new ArrayList<>();
    lore.add(Component.text("Colors: " + preset.getColors().size(), NamedTextColor.GRAY));

    // Show color preview
    StringBuilder colorPreview = new StringBuilder();
    for (String color : preset.getColors()) {
      colorPreview.append("<color:").append(color).append(">█</color>");
    }
    if (!preset.getColors().isEmpty()) {
      lore.add(MiniMessage.miniMessage().deserialize(colorPreview.toString()));
    }

    lore.add(Component.empty());
    lore.add(Component.text("Permission: " + (preset.getPermission().isEmpty() ? "None" : preset.getPermission()),
        NamedTextColor.YELLOW));
    lore.add(Component.empty());
    lore.add(Component.text("LEFT-CLICK: Edit preset", NamedTextColor.GREEN));
    lore.add(Component.text("RIGHT-CLICK: Delete preset", NamedTextColor.RED));
    lore.add(Component.empty());
    // Hidden identifier for click handling
    lore.add(Component.text("__PRESET_NAME__" + preset.getName(), NamedTextColor.DARK_GRAY));

    meta.lore(lore);
    item.setItemMeta(meta);

    return item;
  }

  private static String createGradientText(String text, List<String> colors) {
    if (colors.isEmpty()) {
      return "<gray>" + text + "</gray>";
    }
    if (colors.size() == 1) {
      return "<color:" + colors.get(0) + ">" + text + "</color>";
    }

    StringBuilder gradient = new StringBuilder("<gradient:");
    for (int i = 0; i < colors.size(); i++) {
      if (i > 0)
        gradient.append(":");
      gradient.append(colors.get(i));
    }
    gradient.append(">").append(text).append("</gradient>");
    return gradient.toString();
  }

  public static void openEditInterface(Player player, String presetName) {
    BetterChatColours plugin = JavaPlugin.getPlugin(BetterChatColours.class);
    GlobalPresetData preset = plugin.getGlobalPresetManager().getPreset(presetName);

    if (preset == null) {
      player.sendMessage(Component.text("[ERROR] Preset not found: " + presetName, NamedTextColor.RED));
      open(player);
      return;
    }

    // Open the color selection GUI but in edit mode
    AdminPresetCreateGUI.openColorSelectionGUIForEdit(player, presetName, new ArrayList<>(preset.getColors()));
  }
}
