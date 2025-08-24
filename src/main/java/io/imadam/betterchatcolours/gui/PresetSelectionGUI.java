package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.data.GlobalPresetData;
import io.imadam.betterchatcolours.data.GlobalPresetManager;
import io.imadam.betterchatcolours.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GUI for users to select and equip global presets
 * Users can only equip presets they have permission for, not create new ones
 */
public class PresetSelectionGUI {

  private final BetterChatColours plugin;
  private final Player player;
  private Inventory inventory;

  // Slot positions
  private static final int CLEAR_PRESET_SLOT = 4;
  private static final int INFO_SLOT = 49;
  private static final int[] PRESET_SLOTS = {
      10, 11, 12, 13, 14, 15, 16,
      19, 20, 21, 22, 23, 24, 25,
      28, 29, 30, 31, 32, 33, 34,
      37, 38, 39, 40, 41, 42, 43
  };

  public PresetSelectionGUI(BetterChatColours plugin, Player player) {
    this.plugin = plugin;
    this.player = player;
    createInventory();
    populateInventory();
  }

  private void createInventory() {
    String title = "§6§lChat Color Presets";
    this.inventory = GUIUtils.createInventory(player, 54, title);
  }

  private void populateInventory() {
    try {
      // Clear inventory
      inventory.clear();

      // Add clear preset button
      addClearPresetButton();

      // Add available presets
      addAvailablePresets();

      // Add info item
      addInfoItem();

      // Add admin options if player has permission
      if (player.hasPermission("chatcolors.admin.create")) {
        addAdminOptions();
      }

    } catch (Exception e) {
      plugin.getLogger()
          .warning("Error populating preset selection GUI for " + player.getName() + ": " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void addClearPresetButton() {
    ItemStack clearItem = new ItemStack(Material.BARRIER);
    ItemMeta meta = clearItem.getItemMeta();
    if (meta != null) {
      meta.displayName(Component.text("Clear Active Preset").color(NamedTextColor.RED));

      List<Component> lore = new ArrayList<>();
      lore.add(Component.text("Remove your currently equipped preset").color(NamedTextColor.GRAY));
      lore.add(Component.text("Your messages will use default formatting").color(NamedTextColor.GRAY));
      lore.add(Component.empty());
      lore.add(Component.text("Click to clear").color(NamedTextColor.YELLOW));

      meta.lore(lore);
      clearItem.setItemMeta(meta);
    }

    inventory.setItem(CLEAR_PRESET_SLOT, clearItem);
  }

  private void addAvailablePresets() {
    GlobalPresetManager globalManager = plugin.getGlobalPresetManager();
    Map<String, GlobalPresetData> availablePresets = globalManager.getAvailablePresetsMap(player);

    String currentEquippedId = plugin.getUserDataManager().getEquippedPreset(player.getUniqueId());

    int slotIndex = 0;
    for (Map.Entry<String, GlobalPresetData> entry : availablePresets.entrySet()) {
      if (slotIndex >= PRESET_SLOTS.length) {
        break; // No more slots available
      }

      String presetId = entry.getKey();
      GlobalPresetData preset = entry.getValue();

      ItemStack presetItem = createPresetItem(preset, presetId, presetId.equals(currentEquippedId));
      inventory.setItem(PRESET_SLOTS[slotIndex], presetItem);
      slotIndex++;
    }
  }

  private ItemStack createPresetItem(GlobalPresetData preset, String presetId, boolean isEquipped) {
    // Use wool color based on first color in preset
    Material woolMaterial = Material.WHITE_WOOL;
    if (!preset.getColors().isEmpty()) {
      woolMaterial = ColorUtils.getClosestWool(preset.getColors().get(0));
    }

    ItemStack item = new ItemStack(woolMaterial);
    ItemMeta meta = item.getItemMeta();

    if (meta != null) {
      // Create colored title
      if (!preset.getColors().isEmpty()) {
        try {
          String miniMessageFormat = ColorUtils.createGradientPreview(preset.getColors(), preset.getName());
          Component titleComponent = MiniMessage.miniMessage().deserialize(miniMessageFormat);
          meta.displayName(titleComponent);
        } catch (Exception e) {
          meta.displayName(Component.text(preset.getName()).color(NamedTextColor.WHITE));
        }
      } else {
        meta.displayName(Component.text(preset.getName()).color(NamedTextColor.WHITE));
      }

      // Create lore
      List<Component> lore = new ArrayList<>();

      // Show colors
      lore.add(Component.text("Colors: " + String.join(", ", preset.getColors())).color(NamedTextColor.GRAY));

      // Show preset type
      if (preset.isDefault()) {
        lore.add(Component.text("Type: Default Preset").color(NamedTextColor.BLUE));
      } else {
        lore.add(Component.text("Type: Custom Preset").color(NamedTextColor.LIGHT_PURPLE));
      }

      // Show permission requirement
      lore.add(Component.text("Permission: " + preset.getRequiredPermission()).color(NamedTextColor.DARK_GRAY));

      lore.add(Component.empty());

      // Show preview
      if (!preset.getColors().isEmpty()) {
        try {
          String previewText = ColorUtils.createGradientPreview(preset.getColors(), "Sample Text");
          Component previewComponent = MiniMessage.miniMessage().deserialize(previewText);
          lore.add(Component.text("Preview: ").color(NamedTextColor.YELLOW).append(previewComponent));
        } catch (Exception e) {
          lore.add(Component.text("Preview: Sample Text").color(NamedTextColor.YELLOW));
        }
      }

      lore.add(Component.empty());

      // Show status and action
      if (isEquipped) {
        lore.add(Component.text("✓ Currently Equipped").color(NamedTextColor.GREEN));
        lore.add(Component.text("Click to re-apply").color(NamedTextColor.YELLOW));
      } else {
        lore.add(Component.text("Click to equip").color(NamedTextColor.YELLOW));
      }

      meta.lore(lore);
      item.setItemMeta(meta);
    }

    return item;
  }

  private void addInfoItem() {
    ItemStack infoItem = new ItemStack(Material.BOOK);
    ItemMeta meta = infoItem.getItemMeta();
    if (meta != null) {
      meta.displayName(Component.text("Preset Information").color(NamedTextColor.AQUA));

      List<Component> lore = new ArrayList<>();
      lore.add(Component.text("Select a preset to apply color gradients").color(NamedTextColor.GRAY));
      lore.add(Component.text("to your chat messages.").color(NamedTextColor.GRAY));
      lore.add(Component.empty());
      lore.add(Component.text("You can only use presets you have").color(NamedTextColor.GRAY));
      lore.add(Component.text("permission for.").color(NamedTextColor.GRAY));
      lore.add(Component.empty());

      // Show currently equipped preset
      String equippedId = plugin.getUserDataManager().getEquippedPreset(player.getUniqueId());
      if (equippedId != null) {
        GlobalPresetData equipped = plugin.getGlobalPresetManager().getPreset(equippedId);
        if (equipped != null) {
          lore.add(Component.text("Currently equipped: " + equipped.getName()).color(NamedTextColor.GREEN));
        } else {
          lore.add(Component.text("Currently equipped: " + equippedId).color(NamedTextColor.GREEN));
        }
      } else {
        lore.add(Component.text("No preset currently equipped").color(NamedTextColor.RED));
      }

      meta.lore(lore);
      infoItem.setItemMeta(meta);
    }

    inventory.setItem(INFO_SLOT, infoItem);
  }

  private void addAdminOptions() {
    // Add admin preset creation button
    ItemStack adminItem = new ItemStack(Material.COMMAND_BLOCK);
    ItemMeta meta = adminItem.getItemMeta();
    if (meta != null) {
      meta.displayName(Component.text("Admin: Create Preset").color(NamedTextColor.RED));

      List<Component> lore = new ArrayList<>();
      lore.add(Component.text("Create a new global preset").color(NamedTextColor.GRAY));
      lore.add(Component.text("for all players to use").color(NamedTextColor.GRAY));
      lore.add(Component.empty());
      lore.add(Component.text("Click to open preset creator").color(NamedTextColor.YELLOW));

      meta.lore(lore);
      adminItem.setItemMeta(meta);
    }

    inventory.setItem(0, adminItem); // Top-left corner
  }

  public void handleClick(int slot, ItemStack clickedItem) {
    if (slot == CLEAR_PRESET_SLOT) {
      // Clear equipped preset
      plugin.getUserDataManager().clearEquippedPreset(player.getUniqueId());
      player.sendMessage(Component.text("✓ Cleared your equipped preset").color(NamedTextColor.GREEN));

      // Refresh GUI
      populateInventory();
      return;
    }

    if (slot == 0 && player.hasPermission("chatcolors.admin.create")) {
      // Send player message about admin commands
      player.closeInventory();
      player.sendMessage(Component.text("Use admin commands to create presets:").color(NamedTextColor.YELLOW));
      player.sendMessage(
          Component.text("/chatcolors admin create <name> <color1> [color2] [color3]...").color(NamedTextColor.GRAY));
      player.sendMessage(
          Component.text("Example: /chatcolors admin create Ocean #00BFFF #1E90FF #0000FF").color(NamedTextColor.GRAY));
      return;
    }

    // Check if clicked slot is a preset slot
    for (int i = 0; i < PRESET_SLOTS.length; i++) {
      if (PRESET_SLOTS[i] == slot) {
        // Find the preset at this position
        GlobalPresetManager globalManager = plugin.getGlobalPresetManager();
        Map<String, GlobalPresetData> availablePresets = globalManager.getAvailablePresetsMap(player);

        if (i < availablePresets.size()) {
          String presetId = availablePresets.keySet().toArray(new String[0])[i];
          GlobalPresetData preset = availablePresets.get(presetId);

          if (preset != null) {
            // Equip the preset
            plugin.getUserDataManager().equipPreset(player.getUniqueId(), presetId);

            // Send confirmation message with preview
            try {
              String previewMessage = ColorUtils.createGradientPreview(preset.getColors(),
                  "✓ Equipped preset: " + preset.getName());
              Component message = MiniMessage.miniMessage().deserialize(previewMessage);
              player.sendMessage(message);
            } catch (Exception e) {
              player.sendMessage(Component.text("✓ Equipped preset: " + preset.getName()).color(NamedTextColor.GREEN));
            }

            // Refresh GUI
            populateInventory();
          }
        }
        break;
      }
    }
  }

  public void openGUI() {
    player.openInventory(inventory);
  }

  public Inventory getInventory() {
    return inventory;
  }
}
