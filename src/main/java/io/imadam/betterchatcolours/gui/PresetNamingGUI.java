package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.data.DataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * AnvilGUI for naming presets with colored preview
 */
public class PresetNamingGUI {

  private final BetterChatColours plugin;
  private final Player player;
  private final List<String> selectedColors;
  private final boolean isEditing;
  private final String originalName;
  private final boolean isGlobalPreset;

  public PresetNamingGUI(BetterChatColours plugin, Player player, List<String> selectedColors) {
    this(plugin, player, selectedColors, false, null, false);
  }

  public PresetNamingGUI(BetterChatColours plugin, Player player, List<String> selectedColors, boolean isGlobalPreset) {
    this(plugin, player, selectedColors, false, null, isGlobalPreset);
  }

  public PresetNamingGUI(BetterChatColours plugin, Player player, List<String> selectedColors,
      boolean isEditing, String originalName, boolean isGlobalPreset) {
    this.plugin = plugin;
    this.player = player;
    this.selectedColors = selectedColors;
    this.isEditing = isEditing;
    this.originalName = originalName;
    this.isGlobalPreset = isGlobalPreset;
  }

  public void open() {
    // Allow single colors now
    if (selectedColors.isEmpty()) {
      player.sendMessage(Component.text("No colors selected!", NamedTextColor.RED));
      returnToPreviousGUI();
      return;
    }

    String defaultName = isEditing ? originalName : "";

    new AnvilGUI.Builder()
        .plugin(plugin)
        .title("Name your " + (isGlobalPreset ? "global " : "") + "preset")
        .itemLeft(createPreviewItem())
        .text(defaultName)
        .onClick((slot, stateSnapshot) -> {
          if (slot != AnvilGUI.Slot.OUTPUT) {
            return Arrays.asList();
          }

          String presetName = stateSnapshot.getText().trim();

          // Validate preset name
          String validationError = validatePresetName(presetName);
          if (validationError != null) {
            return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText(validationError));
          }

          // Handle saving/updating
          if (isGlobalPreset) {
            openGlobalPresetSettings(presetName);
          } else {
            handleUserPreset(presetName);
          }

          return Arrays.asList(AnvilGUI.ResponseAction.close());
        })
        .onClose(player -> returnToPreviousGUI())
        .open(player);
  }

  private ItemStack createPreviewItem() {
    ItemStack item = new ItemStack(Material.NAME_TAG);
    var meta = item.getItemMeta();
    if (meta != null) {
      String previewText = selectedColors.size() == 1 ? "Single Color" : "Gradient Preview";

      // Create a simple colored preview
      Component displayName;
      if (selectedColors.size() == 1) {
        TextColor color = TextColor.fromHexString(selectedColors.get(0));
        displayName = Component.text(previewText, color);
      } else {
        // For gradients, just use the first color
        TextColor color = TextColor.fromHexString(selectedColors.get(0));
        displayName = Component.text(previewText, color);
      }

      meta.displayName(displayName);
      item.setItemMeta(meta);
    }
    return item;
  }

  private String validatePresetName(String presetName) {
    if (presetName.isEmpty()) {
      return "Name cannot be empty!";
    }

    if (presetName.length() > 32) {
      return "Name too long! (Max 32 chars)";
    }

    // Check for invalid characters
    if (!presetName.matches("^[a-zA-Z0-9 _-]+$")) {
      return "Invalid chars! Use letters, numbers, spaces, _ or -";
    }

    // For now, skip duplicate checking until we implement the methods

    return null; // Valid
  }

  private void openGlobalPresetSettings(String presetName) {
    // Open global preset settings GUI
    new GlobalPresetSettingsGUI(plugin, player, presetName, selectedColors).open();
  }

  private void handleUserPreset(String presetName) {
    try {
      if (isEditing) {
        // Update existing preset - for now just create new
        DataManager.CreatePresetResult result = plugin.getDataManager().createPreset(player, presetName,
            selectedColors);

        if (result.isSuccess()) {
          player.sendMessage(Component.text("Preset '" + presetName + "' updated!", NamedTextColor.GREEN));
        } else {
          player.sendMessage(Component.text("Error: " + result.getMessage(), NamedTextColor.RED));
        }
      } else {
        // Create new preset
        DataManager.CreatePresetResult result = plugin.getDataManager().createPreset(player, presetName,
            selectedColors);

        if (result.isSuccess()) {
          player.sendMessage(Component.text("Preset '" + presetName + "' created!", NamedTextColor.GREEN));
        } else {
          player.sendMessage(Component.text("Error: " + result.getMessage(), NamedTextColor.RED));
        }
      }
    } catch (Exception e) {
      plugin.getLogger().warning("Error handling user preset: " + e.getMessage());
      player.sendMessage(Component.text("Error saving preset. Please try again.", NamedTextColor.RED));
    }
  }

  private void returnToPreviousGUI() {
    // Return to main menu for now
    new MainMenuGUI(plugin, player).open();
  }
}
