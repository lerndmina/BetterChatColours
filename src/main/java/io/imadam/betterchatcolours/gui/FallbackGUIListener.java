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

import java.util.ArrayList;
import java.util.List;

public class FallbackGUIListener implements Listener {

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player))
      return;

    String title = event.getView().getTitle();

    // Prevent taking items from our GUIs
    if (title.startsWith("Chat Colors") || title.equals("Available Presets") ||
        title.startsWith("Create New Preset") || title.startsWith("Edit Preset") || title.startsWith("Edit Presets")) {
      event.setCancelled(true);

      if (event.getCurrentItem() == null)
        return;

      if (title.startsWith("Chat Colors")) {
        handleMainMenuClick(player, event);
      } else if (title.equals("Available Presets")) {
        handlePresetSelectionClick(player, event);
      } else if (title.startsWith("Create New Preset") || title.startsWith("Edit Preset")) {
        handleAdminCreateClick(player, event);
      } else if (title.startsWith("Edit Presets")) {
        handleAdminEditClick(player, event);
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
        if (!player.hasPermission("betterchatcolours.admin")) {
          player.sendMessage(Component.text("You don't have permission to create presets!", NamedTextColor.RED));
          return;
        }
        player.closeInventory();
        AdminPresetCreateGUI.open(player);
      }
      case "Edit Presets" -> {
        if (!player.hasPermission("betterchatcolours.admin")) {
          player.sendMessage(Component.text("You don't have permission to edit presets!", NamedTextColor.RED));
          return;
        }
        player.closeInventory();
        AdminPresetEditGUI.open(player);
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

  private void handleAdminCreateClick(Player player, InventoryClickEvent event) {
    if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta())
      return;

    ItemMeta meta = event.getCurrentItem().getItemMeta();
    Component displayName = meta.displayName();
    if (displayName == null)
      return;

    String itemName = ((net.kyori.adventure.text.TextComponent) displayName).content();
    String title = event.getView().getTitle();

    // Extract preset name from title
    String presetName = title.substring("Create New Preset: ".length());

    switch (itemName) {
      case "Add Color" -> {
        player.closeInventory();
        openColorInput(player, presetName, getCurrentColors(event.getInventory()));
      }
      case "Save Preset" -> {
        List<String> colors = getCurrentColors(event.getInventory());
        if (colors.isEmpty()) {
          player.sendMessage(Component.text("Cannot save preset without colors!", NamedTextColor.RED));
          return;
        }

        BetterChatColours plugin = JavaPlugin.getPlugin(BetterChatColours.class);
        String inventoryTitle = event.getView().getTitle();
        boolean editMode = isEditMode(inventoryTitle);

        plugin.getGlobalPresetManager().addPreset(presetName, colors);
        String permission = "chatcolor.preset." + presetName.toLowerCase();

        String action = editMode ? "updated" : "created";
        player.sendMessage(
            Component.text("Preset '" + presetName + "' " + action + " successfully! Permission: " + permission,
                NamedTextColor.GREEN));

        if (editMode) {
          AdminPresetEditGUI.open(player);
        } else {
          player.closeInventory();
        }
      }
      case "Cancel" -> {
        player.closeInventory();
        FallbackMainMenuGUI.open(player);
      }
      default -> {
        // Handle color item clicks
        if (itemName.startsWith("Color ")) {
          // Extract color index from lore
          String colorIndex = extractColorIndex(meta);
          if (colorIndex != null) {
            List<String> colors = getCurrentColors(event.getInventory());
            if (event.getClick().isRightClick()) {
              // Remove color
              int index = Integer.parseInt(colorIndex);
              if (index >= 0 && index < colors.size()) {
                colors.remove(index);
                AdminPresetCreateGUI.openColorSelectionGUI(player, presetName, colors);
              }
            } else if (event.getClick().isLeftClick()) {
              // Edit color
              int index = Integer.parseInt(colorIndex);
              if (index >= 0 && index < colors.size()) {
                openColorEditInput(player, presetName, colors, index);
              }
            }
          }
        }
      }
    }
  }

  private void openColorInput(Player player, String presetName, List<String> currentColors) {
    ChatInputManager.requestHexColor(player, presetName, currentColors,
        hexColor -> {
          currentColors.add(hexColor);
          AdminPresetCreateGUI.openColorSelectionGUI(player, presetName, currentColors);
        },
        () -> AdminPresetCreateGUI.openColorSelectionGUI(player, presetName, currentColors));
  }

  private void openColorEditInput(Player player, String presetName, List<String> colors, int index) {
    ChatInputManager.requestHexColorEdit(player, presetName, colors, index,
        hexColor -> {
          colors.set(index, hexColor);
          AdminPresetCreateGUI.openColorSelectionGUI(player, presetName, colors);
        },
        () -> AdminPresetCreateGUI.openColorSelectionGUI(player, presetName, colors));
  }

  private List<String> getCurrentColors(org.bukkit.inventory.Inventory inventory) {
    List<String> colors = new ArrayList<>();
    for (int i = 0; i < inventory.getSize() - 9; i++) { // Exclude bottom row
      var item = inventory.getItem(i);
      if (item != null && item.hasItemMeta()) {
        var meta = item.getItemMeta();
        if (meta.displayName() != null) {
          String displayName = ((net.kyori.adventure.text.TextComponent) meta.displayName()).content();
          if (displayName.startsWith("Color ")) {
            // Extract hex color from display name
            String[] parts = displayName.split(": ");
            if (parts.length > 1) {
              colors.add(parts[1]);
            }
          }
        }
      }
    }
    return colors;
  }

  private void handleAdminEditClick(Player player, InventoryClickEvent event) {
    if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta())
      return;

    ItemMeta meta = event.getCurrentItem().getItemMeta();
    Component displayName = meta.displayName();
    if (displayName == null)
      return;

    // Handle navigation buttons
    String displayText = ((net.kyori.adventure.text.TextComponent) displayName).content();

    switch (displayText) {
      case "Previous Page" -> {
        // Extract page info from title
        String title = event.getView().getTitle();
        if (title.contains("(Page ")) {
          int currentPage = extractPageNumber(title);
          if (currentPage > 1) {
            reopenEditPageWithOffset(player, -1);
          }
        }
      }
      case "Next Page" -> {
        String title = event.getView().getTitle();
        if (title.contains("(Page ")) {
          reopenEditPageWithOffset(player, 1);
        }
      }
      case "Back to Main Menu" -> {
        player.closeInventory();
        FallbackMainMenuGUI.open(player);
      }
      default -> {
        // This is a preset item - check for left/right click
        String presetName = extractPresetName(meta);
        if (presetName != null) {
          if (event.isLeftClick()) {
            // Edit the preset
            player.closeInventory();
            AdminPresetEditGUI.openEditInterface(player, presetName);
          } else if (event.isRightClick()) {
            // Delete the preset
            deletePreset(player, presetName);
          }
        }
      }
    }
  }

  private int extractPageNumber(String title) {
    try {
      int pageStart = title.indexOf("(Page ") + 6;
      int pageEnd = title.indexOf("/", pageStart);
      return Integer.parseInt(title.substring(pageStart, pageEnd));
    } catch (Exception e) {
      return 1;
    }
  }

  private void reopenEditPageWithOffset(Player player, int offset) {
    player.closeInventory();
    // For now, just reopen the main edit GUI
    // TODO: Implement proper page tracking
    AdminPresetEditGUI.open(player);
  }

  private String extractPresetName(ItemMeta meta) {
    if (meta.lore() == null)
      return null;

    for (Component loreComponent : meta.lore()) {
      String loreText = ((net.kyori.adventure.text.TextComponent) loreComponent).content();
      if (loreText.startsWith("__PRESET_NAME__")) {
        return loreText.substring("__PRESET_NAME__".length());
      }
    }
    return null;
  }

  private boolean isEditMode(String title) {
    return title.startsWith("Edit Preset:");
  }

  private void deletePreset(Player player, String presetName) {
    BetterChatColours plugin = JavaPlugin.getPlugin(BetterChatColours.class);

    // Confirm deletion via chat
    ChatInputManager.requestPresetName(player,
        input -> {
          if (input.equalsIgnoreCase(presetName)) {
            // Delete confirmed
            plugin.getGlobalPresetManager().removePreset(presetName);
            player.sendMessage(
                Component.text("[SUCCESS] Preset '" + presetName + "' deleted successfully!", NamedTextColor.GREEN));
            AdminPresetEditGUI.open(player);
          } else {
            player.sendMessage(Component.text("[ERROR] Confirmation failed. Type the exact preset name to delete.",
                NamedTextColor.RED));
            AdminPresetEditGUI.open(player);
          }
        },
        () -> {
          player.sendMessage(Component.text("[CANCELLED] Preset deletion cancelled.", NamedTextColor.YELLOW));
          AdminPresetEditGUI.open(player);
        });

    player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
    player.sendMessage(Component.text("[DELETE] Type the preset name to confirm deletion:", NamedTextColor.RED));
    player.sendMessage(Component.text("   • Preset to delete: " + presetName, NamedTextColor.GRAY));
    player.sendMessage(Component.text("   • Type '" + presetName + "' to confirm", NamedTextColor.GRAY));
    player.sendMessage(Component.text("   • Type 'cancel' to abort", NamedTextColor.GRAY));
    player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
  }

  private String extractColorIndex(ItemMeta meta) {
    if (meta.lore() == null)
      return null;

    for (Component loreComponent : meta.lore()) {
      String loreText = ((net.kyori.adventure.text.TextComponent) loreComponent).content();
      if (loreText.startsWith("__COLOR_INDEX__")) {
        return loreText.substring("__COLOR_INDEX__".length());
      }
    }
    return null;
  }
}
