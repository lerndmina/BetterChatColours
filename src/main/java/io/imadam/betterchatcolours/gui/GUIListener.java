package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles GUI events for the global preset system
 */
public class GUIListener implements Listener {

  private final BetterChatColours plugin;
  private final Map<UUID, PresetSelectionGUI> presetSelectionGUIs = new HashMap<>();
  private final Map<UUID, GlobalPresetSettingsGUI> globalPresetSettingsGUIs = new HashMap<>();

  public GUIListener(BetterChatColours plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player)) {
      return;
    }

    Player player = (Player) event.getWhoClicked();
    UUID playerId = player.getUniqueId();
    Inventory clickedInventory = event.getClickedInventory();

    if (clickedInventory == null) {
      return;
    }

    // Handle PresetSelectionGUI clicks
    PresetSelectionGUI presetSelectionGUI = presetSelectionGUIs.get(playerId);
    if (presetSelectionGUI != null && presetSelectionGUI.getInventory().equals(clickedInventory)) {
      event.setCancelled(true);
      presetSelectionGUI.handleClick(event.getSlot(), event.getCurrentItem());
      return;
    }

    // Handle GlobalPresetSettingsGUI clicks
    GlobalPresetSettingsGUI globalSettingsGUI = globalPresetSettingsGUIs.get(playerId);
    if (globalSettingsGUI != null && globalSettingsGUI.getInventory().equals(clickedInventory)) {
      event.setCancelled(true);
      globalSettingsGUI.handleClick(event.getSlot());
      return;
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player)) {
      return;
    }

    Player player = (Player) event.getPlayer();
    UUID playerId = player.getUniqueId();

    // Clean up GUI references when inventories are closed
    PresetSelectionGUI presetSelectionGUI = presetSelectionGUIs.get(playerId);
    if (presetSelectionGUI != null && presetSelectionGUI.getInventory().equals(event.getInventory())) {
      presetSelectionGUIs.remove(playerId);
    }

    GlobalPresetSettingsGUI globalSettingsGUI = globalPresetSettingsGUIs.get(playerId);
    if (globalSettingsGUI != null && globalSettingsGUI.getInventory().equals(event.getInventory())) {
      globalPresetSettingsGUIs.remove(playerId);
    }
  }

  /**
   * Register a PresetSelectionGUI for event handling
   */
  public void registerPresetSelectionGUI(Player player, PresetSelectionGUI gui) {
    presetSelectionGUIs.put(player.getUniqueId(), gui);
  }

  /**
   * Register a GlobalPresetSettingsGUI for event handling
   */
  public void registerGlobalPresetSettingsGUI(Player player, GlobalPresetSettingsGUI gui) {
    globalPresetSettingsGUIs.put(player.getUniqueId(), gui);
  }

  /**
   * Clean up all GUI references for a player
   */
  public void cleanupPlayer(UUID playerId) {
    presetSelectionGUIs.remove(playerId);
    globalPresetSettingsGUIs.remove(playerId);
  }
}
