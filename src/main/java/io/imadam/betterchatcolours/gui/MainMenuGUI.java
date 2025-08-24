package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.data.DataManager;
import io.imadam.betterchatcolours.data.PresetData;
import io.imadam.betterchatcolours.utils.PermissionUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Main menu GUI - shows user presets and options
 */
public class MainMenuGUI {

  private final BetterChatColours plugin;
  private final Player player;
  private Inventory inventory;

  // Slot positions
  private static final int CREATE_NEW_SLOT = 4;
  private static final int INFO_SLOT = 49;
  private static final int[] PRESET_SLOTS = {
      10, 11, 12, 13, 14, 15, 16,
      19, 20, 21, 22, 23, 24, 25,
      28, 29, 30, 31, 32, 33, 34,
      37, 38, 39, 40, 41, 42, 43
  };

  public MainMenuGUI(BetterChatColours plugin, Player player) {
    this.plugin = plugin;
    this.player = player;
    createInventory();
    populateInventory();
  }

  private void createInventory() {
    String title = plugin.getMessagesConfig().getMessage("gui.main-title");
    plugin.getLogger().info("Creating main menu with title: '" + title + "'");
    this.inventory = GUIUtils.createInventory(player, 54, title);
  }

  private void populateInventory() {
    try {
      // Clear inventory
      inventory.clear();

      // Get user data with validation
      DataManager.UserDashboard dashboard = plugin.getDataManager().getUserDashboard(player);
      if (dashboard == null) {
        plugin.getLogger().warning("Failed to load dashboard for player: " + player.getName());
        inventory.setItem(22, GUIUtils.createItem(Material.BARRIER, "<red>Error Loading Data</red>",
            "<gray>Try reopening the menu</gray>"));
        return;
      }

      DataManager.UserStats stats = dashboard.getStats();
      List<PresetData> presets = dashboard.getPresets();
      PresetData activeGradient = dashboard.getActiveGradient();

      // Create new gradient button (only if player has permission)
      int maxPresets = plugin.getConfigManager().getMaxPresetsPerUser();
      if (PermissionUtils.canCreatePresets(player, maxPresets)) {
        inventory.setItem(CREATE_NEW_SLOT, GUIUtils.createNewGradientButton());
      }

      // Add user presets with validation
      if (presets != null && !presets.isEmpty()) {
        for (int i = 0; i < Math.min(presets.size(), PRESET_SLOTS.length); i++) {
          PresetData preset = presets.get(i);
          if (preset == null || preset.getName() == null || preset.getColors() == null) {
            plugin.getLogger().warning("Invalid preset found for player " + player.getName() + " at index " + i);
            continue;
          }

          try {
            boolean isActive = activeGradient != null && preset.getName().equals(activeGradient.getName());

            ItemStack presetItem = GUIUtils.createPresetItem(
                preset.getName(),
                preset.getColors(),
                isActive);

            inventory.setItem(PRESET_SLOTS[i], presetItem);
          } catch (Exception e) {
            plugin.getLogger().warning("Failed to create preset item for " + preset.getName() + ": " + e.getMessage());
          }
        }
      }

      // Add user info/statistics
      if (stats != null) {
        ItemStack infoItem = createUserInfoItem(stats);
        inventory.setItem(INFO_SLOT, infoItem);
      }

      // Fill empty slots with border
      GUIUtils.fillEmptySlots(inventory);
    } catch (Exception e) {
      plugin.getLogger().severe("Error populating main menu for " + player.getName() + ": " + e.getMessage());
      e.printStackTrace();

      // Add error indicator
      inventory.setItem(22, GUIUtils.createItem(Material.BARRIER, "<red>Error Loading Menu</red>",
          "<gray>Contact an administrator</gray>"));
    }
  }

  private ItemStack createUserInfoItem(DataManager.UserStats stats) {
    String activeStatus = stats.hasActiveGradient() ? "<green>Active</green>" : "<gray>None</gray>";

    String adminForced = stats.hasAdminForced() ? "<red>Admin Override Active</red>" : "";

    String[] lore = {
        "<gray>Presets: " + stats.getPresetCount() + "/" + stats.getMaxPresets() + "</gray>",
        "<gray>Active Gradient: " + activeStatus + "</gray>",
        adminForced.isEmpty() ? "" : adminForced,
        "",
        "<yellow>Your chat color statistics</yellow>"
    };

    // Filter out empty strings
    lore = java.util.Arrays.stream(lore)
        .filter(s -> !s.isEmpty())
        .toArray(String[]::new);

    return GUIUtils.createItem(
        Material.PLAYER_HEAD,
        "<gold>Your Chat Colors</gold>",
        lore);
  }

  /**
   * Handle click events in the main menu
   */
  public void handleClick(int slot, boolean rightClick) {
    if (slot == CREATE_NEW_SLOT) {
      // Double-check permission before opening color selection
      int maxPresets = plugin.getConfigManager().getMaxPresetsPerUser();
      if (!PermissionUtils.canCreatePresets(player, maxPresets)) {
        plugin.getMessagesConfig().sendMessage(player, "commands.no-permission");
        return;
      }

      // Open color selection GUI
      new ColorSelectionGUI(plugin, player).open();
      return;
    }

    // Check if it's a preset slot
    for (int i = 0; i < PRESET_SLOTS.length; i++) {
      if (PRESET_SLOTS[i] == slot) {
        handlePresetClick(i, rightClick);
        return;
      }
    }

    if (slot == INFO_SLOT) {
      // Refresh the GUI or show detailed stats
      refresh();
    }
  }

  private void handlePresetClick(int presetIndex, boolean rightClick) {
    DataManager.UserDashboard dashboard = plugin.getDataManager().getUserDashboard(player);
    List<PresetData> presets = dashboard.getPresets();

    if (presetIndex >= presets.size()) {
      return; // Invalid preset index
    }

    PresetData preset = presets.get(presetIndex);

    if (rightClick) {
      // Delete preset (with confirmation)
      // TODO: Implement ConfirmDeleteGUI
      player.sendMessage("<red>Preset deletion not yet implemented</red>");
    } else {
      // Activate preset
      boolean success = plugin.getDataManager().applyPreset(player, preset.getName());

      if (success) {
        plugin.getMessagesConfig().sendMessage(player, "gui.preset-activated", "preset", preset.getName());
        refresh(); // Update the GUI to show new active status
      } else {
        plugin.getMessagesConfig().sendMessage(player, "errors.preset-permission-lost");
      }
    }
  }

  /**
   * Refresh the GUI contents
   */
  public void refresh() {
    populateInventory();
  }

  /**
   * Open the GUI for the player
   */
  public void open() {
    plugin.getGuiListener().registerMainMenu(player, this);
    player.openInventory(inventory);
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
