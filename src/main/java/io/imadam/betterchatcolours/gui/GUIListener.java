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
 * Handles all GUI events for the plugin
 */
public class GUIListener implements Listener {

  private final BetterChatColours plugin;
  private final Map<UUID, MainMenuGUI> mainMenus = new HashMap<>();
  private final Map<UUID, ColorSelectionGUI> colorGUIs = new HashMap<>();
  private final Map<UUID, ReorderGUI> reorderGUIs = new HashMap<>();
  private final Map<UUID, GlobalPresetSettingsGUI> globalSettingsGUIs = new HashMap<>();

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

    // Check if it's one of our GUIs
    MainMenuGUI mainMenu = mainMenus.get(playerId);
    ColorSelectionGUI colorGUI = colorGUIs.get(playerId);
    ReorderGUI reorderGUI = reorderGUIs.get(playerId);
    GlobalPresetSettingsGUI globalSettingsGUI = globalSettingsGUIs.get(playerId);

    if (mainMenu != null && mainMenu.isInventory(clickedInventory)) {
      event.setCancelled(true);

      if (event.getSlot() >= 0 && event.getSlot() < clickedInventory.getSize()) {
        mainMenu.handleClick(event.getSlot(), event.isRightClick());
      }
      return;
    }

    if (colorGUI != null && colorGUI.isInventory(clickedInventory)) {
      event.setCancelled(true);

      if (event.getSlot() >= 0 && event.getSlot() < clickedInventory.getSize()) {
        colorGUI.handleClick(event.getSlot(), event.isRightClick());
      }
      return;
    }

    if (reorderGUI != null && reorderGUI.getInventory().equals(clickedInventory)) {
      event.setCancelled(true);

      if (event.getSlot() >= 0 && event.getSlot() < clickedInventory.getSize()) {
        reorderGUI.handleClick(event.getSlot(), event.isRightClick(), event.isShiftClick());
      }
      return;
    }

    if (globalSettingsGUI != null && globalSettingsGUI.isInventory(clickedInventory)) {
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
    MainMenuGUI mainMenu = mainMenus.get(playerId);
    ColorSelectionGUI colorGUI = colorGUIs.get(playerId);
    ReorderGUI reorderGUI = reorderGUIs.get(playerId);

    if (mainMenu != null && mainMenu.isInventory(event.getInventory())) {
      mainMenus.remove(playerId);
    }

    if (colorGUI != null && colorGUI.isInventory(event.getInventory())) {
      colorGUIs.remove(playerId);
    }

    if (reorderGUI != null && reorderGUI.getInventory().equals(event.getInventory())) {
      reorderGUIs.remove(playerId);
    }
  }

  /**
   * Register a MainMenuGUI for event handling
   */
  public void registerMainMenu(Player player, MainMenuGUI gui) {
    mainMenus.put(player.getUniqueId(), gui);
  }

  /**
   * Register a ColorSelectionGUI for event handling
   */
  public void registerColorGUI(Player player, ColorSelectionGUI gui) {
    colorGUIs.put(player.getUniqueId(), gui);
  }

  /**
   * Register a ReorderGUI for event handling
   */
  public void registerReorderGUI(Player player, ReorderGUI gui) {
    reorderGUIs.put(player.getUniqueId(), gui);
  }

  /**
   * Register a GlobalSettingsGUI for event handling
   */
  public void registerGlobalSettings(Player player, GlobalPresetSettingsGUI gui) {
    globalSettingsGUIs.put(player.getUniqueId(), gui);
  }

  /**
   * Unregister all GUIs for a player
   */
  public void unregisterPlayer(Player player) {
    UUID playerId = player.getUniqueId();
    mainMenus.remove(playerId);
    colorGUIs.remove(playerId);
    reorderGUIs.remove(playerId);
    globalSettingsGUIs.remove(playerId);
  }

  /**
   * Get the active main menu for a player
   */
  public MainMenuGUI getMainMenu(Player player) {
    return mainMenus.get(player.getUniqueId());
  }

  /**
   * Get the active color selection GUI for a player
   */
  public ColorSelectionGUI getColorGUI(Player player) {
    return colorGUIs.get(player.getUniqueId());
  }
}
