package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.data.GlobalPresetData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * GUI for configuring global preset settings
 */
public class GlobalPresetSettingsGUI {

  private final BetterChatColours plugin;
  private final Player player;
  private final String presetName;
  private final List<String> selectedColors;
  private Inventory inventory;

  // Default permission for global presets
  private String permission = "chatcolors.preset.global";
  private boolean isPublished = false;

  public GlobalPresetSettingsGUI(BetterChatColours plugin, Player player, String presetName,
      List<String> selectedColors) {
    this.plugin = plugin;
    this.player = player;
    this.presetName = presetName;
    this.selectedColors = selectedColors;

    // Generate default permission based on preset name
    this.permission = "chatcolors.preset." + presetName.toLowerCase().replace(" ", "_").replace("-", "_");
  }

  public void open() {
    createInventory();
    populateInventory();
    plugin.getGuiListener().registerGlobalPresetSettingsGUI(player, this);
    player.openInventory(inventory);
  }

  private void createInventory() {
    String title = "Global Preset: " + presetName;
    this.inventory = GUIUtils.createInventory(player, 27, title);
  }

  private void populateInventory() {
    inventory.clear();

    // Permission setting button (slot 10)
    ItemStack permissionItem = new ItemStack(Material.WRITABLE_BOOK);
    ItemMeta permMeta = permissionItem.getItemMeta();
    if (permMeta != null) {
      permMeta.displayName(Component.text("Set Permission", NamedTextColor.YELLOW));
      permMeta.lore(Arrays.asList(
          Component.text("Current: " + permission, NamedTextColor.GRAY),
          Component.text("Click to change", NamedTextColor.GREEN)));
      permissionItem.setItemMeta(permMeta);
    }
    inventory.setItem(10, permissionItem);

    // Publish/Unpublish button (slot 12)
    ItemStack publishItem = new ItemStack(isPublished ? Material.LIME_DYE : Material.GRAY_DYE);
    ItemMeta pubMeta = publishItem.getItemMeta();
    if (pubMeta != null) {
      pubMeta.displayName(Component.text(isPublished ? "Published" : "Unpublished",
          isPublished ? NamedTextColor.GREEN : NamedTextColor.RED));
      pubMeta.lore(Arrays.asList(
          Component.text("Status: " + (isPublished ? "Available to users" : "Hidden from users"), NamedTextColor.GRAY),
          Component.text("Click to toggle", NamedTextColor.YELLOW)));
      publishItem.setItemMeta(pubMeta);
    }
    inventory.setItem(12, publishItem);

    // Preview item (slot 14)
    ItemStack previewItem = createPreviewItem();
    inventory.setItem(14, previewItem);

    // Save button (slot 22)
    ItemStack saveItem = new ItemStack(Material.GREEN_CONCRETE);
    ItemMeta saveMeta = saveItem.getItemMeta();
    if (saveMeta != null) {
      saveMeta.displayName(Component.text("Save Global Preset", NamedTextColor.GREEN));
      saveMeta.lore(Arrays.asList(
          Component.text("Create this global preset", NamedTextColor.GRAY),
          Component.text("with current settings", NamedTextColor.GRAY)));
      saveItem.setItemMeta(saveMeta);
    }
    inventory.setItem(22, saveItem);

    // Cancel button (slot 18)
    ItemStack cancelItem = new ItemStack(Material.RED_CONCRETE);
    ItemMeta cancelMeta = cancelItem.getItemMeta();
    if (cancelMeta != null) {
      cancelMeta.displayName(Component.text("Cancel", NamedTextColor.RED));
      cancelMeta.lore(Arrays.asList(
          Component.text("Return without saving", NamedTextColor.GRAY)));
      cancelItem.setItemMeta(cancelMeta);
    }
    inventory.setItem(18, cancelItem);

    // Fill empty slots
    GUIUtils.fillEmptySlots(inventory);
  }

  private ItemStack createPreviewItem() {
    ItemStack item = new ItemStack(Material.PAINTING);
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      meta.displayName(Component.text("Preset Preview", NamedTextColor.GOLD));

      // Create preview lore with colors
      List<Component> lore = Arrays.asList(
          Component.text("Name: " + presetName, NamedTextColor.WHITE),
          Component.text("Colors: " + selectedColors.size(), NamedTextColor.GRAY),
          Component.text("Permission: " + permission, NamedTextColor.AQUA),
          Component.text("Published: " + (isPublished ? "Yes" : "No"),
              isPublished ? NamedTextColor.GREEN : NamedTextColor.RED));

      meta.lore(lore);
      item.setItemMeta(meta);
    }
    return item;
  }

  public void handleClick(int slot) {
    switch (slot) {
      case 10: // Permission setting
        openPermissionEditor();
        break;
      case 12: // Toggle publish status
        isPublished = !isPublished;
        populateInventory();
        break;
      case 14: // Preview (no action)
        break;
      case 18: // Cancel
        player.closeInventory();
        new PresetSelectionGUI(plugin, player).openGUI();
        break;
      case 22: // Save
        saveGlobalPreset();
        break;
    }
  }

  private void openPermissionEditor() {
    new AnvilGUI.Builder()
        .plugin(plugin)
        .title("Set Permission")
        .itemLeft(new ItemStack(Material.PAPER))
        .text(permission)
        .onClick((slot, stateSnapshot) -> {
          if (slot != AnvilGUI.Slot.OUTPUT) {
            return Arrays.asList();
          }

          String newPermission = stateSnapshot.getText().trim();

          // Validate permission
          if (newPermission.isEmpty()) {
            return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText("Permission cannot be empty!"));
          }

          if (!newPermission.matches("^[a-zA-Z0-9._-]+$")) {
            return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText("Invalid permission format!"));
          }

          permission = newPermission;
          populateInventory();

          return Arrays.asList(AnvilGUI.ResponseAction.close());
        })
        .onClose(player -> open()) // Return to this GUI
        .open(player);
  }

  private void saveGlobalPreset() {
    try {
      GlobalPresetData globalPreset = new GlobalPresetData(
          presetName,
          selectedColors,
          player.getUniqueId(), // owner
          true, // isGlobal
          permission,
          isPublished);

      // For now, just create as user preset since we need to implement global preset
      // storage
      plugin.getDataManager().createPreset(player, presetName, selectedColors);

      player.sendMessage(Component.text("Global preset '" + presetName + "' created!", NamedTextColor.GREEN));
      player.sendMessage(Component.text("Permission: " + permission, NamedTextColor.GRAY));
      player.sendMessage(Component.text("Published: " + (isPublished ? "Yes" : "No"), NamedTextColor.GRAY));

      player.closeInventory();
      new PresetSelectionGUI(plugin, player).openGUI();

    } catch (Exception e) {
      plugin.getLogger().warning("Error saving global preset: " + e.getMessage());
      player.sendMessage(Component.text("Error saving global preset!", NamedTextColor.RED));
    }
  }

  public Inventory getInventory() {
    return inventory;
  }

  public boolean isInventory(Inventory inv) {
    return inventory.equals(inv);
  }
}
