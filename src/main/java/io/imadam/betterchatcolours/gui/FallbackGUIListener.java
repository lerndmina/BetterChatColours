package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.data.GlobalPresetData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class FallbackGUIListener implements Listener {

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player))
      return;

    String title = event.getView().getTitle();

    // Prevent taking items from our GUIs
    if (title.startsWith("Chat Colors") || title.equals("Available Presets")) {
      event.setCancelled(true);

      if (event.getCurrentItem() == null)
        return;

      if (title.startsWith("Chat Colors")) {
        handleMainMenuClick(player, event);
      } else if (title.equals("Available Presets")) {
        handlePresetSelectionClick(player, event);
      }
    }
  }

  private void handleMainMenuClick(Player player, InventoryClickEvent event) {
    if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta())
      return;

    Component displayName = event.getCurrentItem().getItemMeta().displayName();
    if (displayName == null)
      return;

    String itemName = ((net.kyori.adventure.text.TextComponent) displayName).content();

    switch (itemName) {
      case "Select Presets" -> {
        player.closeInventory();
        FallbackMainMenuGUI.openPresetSelection(player);
      }
      case "Create Preset" -> {
        player.closeInventory();
        player.sendMessage(
            Component.text("Preset creation coming soon! (AnvilGUI integration needed)", NamedTextColor.YELLOW));
      }
      case "Edit Presets" -> {
        player.closeInventory();
        player.sendMessage(Component.text("Preset editing coming soon!", NamedTextColor.YELLOW));
      }
    }
  }

  private void handlePresetSelectionClick(Player player, InventoryClickEvent event) {
    if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta())
      return;

    ItemMeta meta = event.getCurrentItem().getItemMeta();
    if (meta.lore() == null || meta.lore().isEmpty())
      return;

    // Find the hidden preset identifier in the lore
    String presetName = null;
    for (Component loreComponent : meta.lore()) {
      String loreText = ((net.kyori.adventure.text.TextComponent) loreComponent).content();
      if (loreText.startsWith("__PRESET_ID__")) {
        presetName = loreText.substring("__PRESET_ID__".length());
        break;
      }
    }

    if (presetName == null)
      return;

    // Find the preset and equip it
    BetterChatColours plugin = JavaPlugin.getPlugin(BetterChatColours.class);
    GlobalPresetData preset = plugin.getGlobalPresetManager().getPreset(presetName);

    if (preset != null) {
      // Check permission
      if (preset.getPermission() != null && !preset.getPermission().isEmpty()
          && !player.hasPermission(preset.getPermission())) {
        player.sendMessage(Component.text("You don't have permission to use this preset!", NamedTextColor.RED));
        return;
      }

      // Equip the preset
      plugin.getUserDataManager().setEquippedPreset(player.getUniqueId(), presetName);
      player.sendMessage(Component.text("Equipped preset: " + presetName, NamedTextColor.GREEN));
      player.closeInventory();
    } else {
      player.sendMessage(Component.text("Preset not found!", NamedTextColor.RED));
    }
  }
}
