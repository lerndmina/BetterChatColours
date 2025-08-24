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

public class FallbackMainMenuGUI {

  public static void open(Player player) {
    boolean isAdmin = player.hasPermission("chatcolors.admin");

    // Create a 27-slot inventory (3 rows)
    Inventory inv = Bukkit.createInventory(null, 27, "Chat Colors" + (isAdmin ? " - Admin" : ""));

    // Fill with glass panes
    ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemMeta glassMeta = glass.getItemMeta();
    glassMeta.displayName(Component.text(" "));
    glass.setItemMeta(glassMeta);

    for (int i = 0; i < 27; i++) {
      inv.setItem(i, glass);
    }

    // Select presets item
    ItemStack selectPresets = new ItemStack(Material.ENDER_CHEST);
    ItemMeta selectMeta = selectPresets.getItemMeta();
    selectMeta.displayName(Component.text("Select Presets", NamedTextColor.LIGHT_PURPLE));
    List<Component> lore = new ArrayList<>();
    lore.add(Component.text("Click to browse available", NamedTextColor.GRAY));
    lore.add(Component.text("color presets", NamedTextColor.GRAY));
    selectMeta.lore(lore);
    selectPresets.setItemMeta(selectMeta);
    inv.setItem(13, selectPresets); // Center slot

    if (isAdmin) {
      // Create preset item
      ItemStack createPreset = new ItemStack(Material.CRAFTING_TABLE);
      ItemMeta createMeta = createPreset.getItemMeta();
      createMeta.displayName(Component.text("Create Preset", NamedTextColor.GREEN));
      List<Component> createLore = new ArrayList<>();
      createLore.add(Component.text("Click to create a new", NamedTextColor.GRAY));
      createLore.add(Component.text("global preset", NamedTextColor.GRAY));
      createMeta.lore(createLore);
      createPreset.setItemMeta(createMeta);
      inv.setItem(11, createPreset);

      // Edit presets item
      ItemStack editPreset = new ItemStack(Material.ANVIL);
      ItemMeta editMeta = editPreset.getItemMeta();
      editMeta.displayName(Component.text("Edit Presets", NamedTextColor.YELLOW));
      List<Component> editLore = new ArrayList<>();
      editLore.add(Component.text("Click to edit existing", NamedTextColor.GRAY));
      editLore.add(Component.text("presets", NamedTextColor.GRAY));
      editMeta.lore(editLore);
      editPreset.setItemMeta(editMeta);
      inv.setItem(15, editPreset);
    }

    player.openInventory(inv);
  }

  public static void openPresetSelection(Player player) {
    BetterChatColours plugin = JavaPlugin.getPlugin(BetterChatColours.class);

    // Get available presets
    List<GlobalPresetData> availablePresets = plugin.getGlobalPresetManager()
        .getAllPresets()
        .values()
        .stream()
        .filter(preset -> preset.getPermission() == null ||
            preset.getPermission().isEmpty() ||
            player.hasPermission(preset.getPermission()))
        .toList();

    // Create inventory based on number of presets (minimum 27 slots)
    int slots = Math.max(27, ((availablePresets.size() + 8) / 9) * 9);
    Inventory inv = Bukkit.createInventory(null, slots, "Available Presets");

    // Add preset items
    for (int i = 0; i < availablePresets.size(); i++) {
      GlobalPresetData preset = availablePresets.get(i);

      ItemStack presetItem = new ItemStack(Material.PAPER);
      ItemMeta meta = presetItem.getItemMeta();

      // Apply gradient to the preset name
      Component gradientName = applyGradientToText(preset.getName(), preset);
      meta.displayName(gradientName);

      List<Component> lore = new ArrayList<>();
      lore.add(Component.text("Colors: " + preset.getColors().size(), NamedTextColor.GRAY));
      lore.add(Component.text(""));
      lore.add(Component.text("Click to equip this preset", NamedTextColor.GREEN));
      // Add hidden identifier for preset name
      lore.add(Component.text("__PRESET_ID__" + preset.getName(), NamedTextColor.DARK_GRAY));
      meta.lore(lore);

      presetItem.setItemMeta(meta);
      inv.setItem(i, presetItem);
    }

    player.openInventory(inv);
  }

  private static Component applyGradientToText(String text, GlobalPresetData preset) {
    if (preset.getColors() == null || preset.getColors().isEmpty()) {
      return Component.text(text, NamedTextColor.YELLOW);
    }

    try {
      String gradientMessage = preset.getGradientTag() + text + preset.getClosingTag();
      var component = MiniMessage.miniMessage().deserialize(gradientMessage);
      return component;
    } catch (Exception e) {
      // Fallback to yellow text if gradient parsing fails
      return Component.text(text, NamedTextColor.YELLOW);
    }
  }
}
