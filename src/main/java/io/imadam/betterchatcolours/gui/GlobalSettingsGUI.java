package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.data.GlobalPresetData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * GUI for configuring global preset settings (permission and global status)
 */
public class GlobalSettingsGUI {
    
    private final BetterChatColours plugin;
    private final Player player;
    private final GlobalPresetData preset;
    private final boolean isNewPreset;
    private Inventory inventory;

    public GlobalSettingsGUI(BetterChatColours plugin, Player player, GlobalPresetData preset, boolean isNewPreset) {
        this.plugin = plugin;
        this.player = player;
        this.preset = preset;
        this.isNewPreset = isNewPreset;
        createInventory();
        populateInventory();
    }

    private void createInventory() {
        String title = "§6Configure: " + preset.getName();
        this.inventory = GUIUtils.createInventory(player, 27, title);
    }

    private void populateInventory() {
        // Clear inventory
        inventory.clear();

        // Global status toggle
        ItemStack globalToggle = createGlobalToggleItem();
        inventory.setItem(10, globalToggle);

        // Permission setting
        ItemStack permissionItem = createPermissionItem();
        inventory.setItem(12, permissionItem);

        // Published status (only if global)
        if (preset.isGlobal()) {
            ItemStack publishToggle = createPublishToggleItem();
            inventory.setItem(14, publishToggle);
        }

        // Save button
        ItemStack saveButton = createSaveButton();
        inventory.setItem(22, saveButton);

        // Cancel button
        ItemStack cancelButton = GUIUtils.createItem(Material.BARRIER, "§cCancel", "§7Return without saving changes");
        inventory.setItem(18, cancelButton);

        // Preview
        ItemStack previewItem = createPreviewItem();
        inventory.setItem(4, previewItem);

        // Fill empty slots
        GUIUtils.fillEmptySlots(inventory);
    }

    private ItemStack createGlobalToggleItem() {
        Material material = preset.isGlobal() ? Material.LIME_DYE : Material.GRAY_DYE;
        String name = preset.isGlobal() ? "§aGlobal Preset" : "§7Personal Preset";
        
        String[] lore = {
            "§7Click to toggle global status",
            "",
            "§ePersonal: §7Only you can use this preset",
            "§eGlobal: §7Others can use with permission",
            "",
            "§7Current: " + (preset.isGlobal() ? "§aGlobal" : "§7Personal")
        };

        return GUIUtils.createItem(material, name, lore);
    }

    private ItemStack createPermissionItem() {
        String permission = preset.getPermission();
        
        String[] lore = {
            "§7Click to change permission",
            "",
            "§7Current permission:",
            "§e" + permission,
            "",
            "§7Players need this permission",
            "§7to use this preset"
        };

        return GUIUtils.createItem(Material.WRITABLE_BOOK, "§6Permission Setting", lore);
    }

    private ItemStack createPublishToggleItem() {
        Material material = preset.isPublished() ? Material.EMERALD : Material.REDSTONE;
        String name = preset.isPublished() ? "§aPublished" : "§cUnpublished";
        
        String[] lore = {
            "§7Click to toggle publish status",
            "",
            "§ePublished: §7Visible to all players",
            "§eUnpublished: §7Hidden from public",
            "",
            "§7Current: " + (preset.isPublished() ? "§aPublished" : "§cUnpublished")
        };

        return GUIUtils.createItem(material, name, lore);
    }

    private ItemStack createSaveButton() {
        String[] lore = {
            "§7Save preset with current settings",
            "",
            "§7Type: " + (preset.isGlobal() ? "§aGlobal" : "§7Personal"),
            "§7Permission: §e" + preset.getPermission(),
        };

        if (preset.isGlobal()) {
            lore = Arrays.copyOf(lore, lore.length + 1);
            lore[lore.length - 1] = "§7Published: " + (preset.isPublished() ? "§aYes" : "§cNo");
        }

        return GUIUtils.createItem(Material.EMERALD_BLOCK, "§aSave Preset", lore);
    }

    private ItemStack createPreviewItem() {
        ItemStack item = new ItemStack(Material.PAINTING);
        item.editMeta(meta -> {
            // Create gradient preview in the item name
            String gradientPreview = preset.createColoredPreview("Preview: " + preset.getName());
            Component displayName = MiniMessage.miniMessage().deserialize(gradientPreview);
            meta.displayName(displayName);
            
            meta.lore(Arrays.asList(
                Component.text("§7Colors: " + preset.getColors().size()),
                Component.text("§7Type: " + (preset.isSingleColor() ? "Single Color" : "Gradient")),
                Component.text(""),
                Component.text("§7Configure settings below")
            ));
        });
        return item;
    }

    public void handleClick(int slot, ClickType clickType) {
        switch (slot) {
            case 10: // Global toggle
                preset.setGlobal(!preset.isGlobal());
                if (!preset.isGlobal()) {
                    preset.setPublished(false); // Can't be published if not global
                }
                populateInventory();
                break;

            case 12: // Permission setting
                openPermissionEditor();
                break;

            case 14: // Publish toggle (only if global)
                if (preset.isGlobal()) {
                    preset.setPublished(!preset.isPublished());
                    populateInventory();
                }
                break;

            case 18: // Cancel
                if (isNewPreset) {
                    new ColorSelectionGUI(plugin, player).open();
                } else {
                    new MainMenuGUI(plugin, player).open();
                }
                break;

            case 22: // Save
                savePreset();
                break;
        }
    }

    private void openPermissionEditor() {
        new AnvilGUI.Builder()
            .onClick((slot, stateSnapshot) -> {
                if (slot != AnvilGUI.Slot.OUTPUT) {
                    return Arrays.asList();
                }
                
                String permission = stateSnapshot.getText().trim();
                
                if (permission.isEmpty()) {
                    return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText("Permission cannot be empty!"));
                }
                
                // Validate permission format
                if (!permission.matches("^[a-zA-Z0-9._-]+$")) {
                    return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText("Invalid permission format!"));
                }
                
                preset.setPermission(permission);
                plugin.getLogger().info("Updated permission for preset '" + preset.getName() + "' to: " + permission);
                
                return Arrays.asList(AnvilGUI.ResponseAction.close());
            })
            .onClose(stateSnapshot -> {
                populateInventory();
                open();
            })
            .text(preset.getPermission())
            .title("Set Permission")
            .itemLeft(new ItemStack(Material.WRITABLE_BOOK))
            .plugin(plugin)
            .open(player);
    }

    private void savePreset() {
        try {
            boolean success;
            
            if (isNewPreset) {
                success = plugin.getDataManager().saveGlobalPreset(player, preset);
            } else {
                success = plugin.getDataManager().updateGlobalPreset(player, preset);
            }
            
            if (success) {
                String preview = preset.createColoredPreview("Preset saved: " + preset.getName());
                player.sendMessage(MiniMessage.miniMessage().deserialize(preview));
                
                String typeMsg = preset.isGlobal() ? 
                    (preset.isPublished() ? "§a(Global & Published)" : "§e(Global but Unpublished)") : 
                    "§7(Personal)";
                player.sendMessage("§7Type: " + typeMsg);
                player.sendMessage("§7Permission: §e" + preset.getPermission());
                
                new MainMenuGUI(plugin, player).open();
            } else {
                player.sendMessage("§cFailed to save preset. You may have reached your limit.");
                if (isNewPreset) {
                    new ColorSelectionGUI(plugin, player).open();
                } else {
                    new MainMenuGUI(plugin, player).open();
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error saving preset: " + e.getMessage());
            player.sendMessage("§cError saving preset. Please try again.");
            new MainMenuGUI(plugin, player).open();
        }
    }

    public void open() {
        // plugin.getGuiListener().registerGlobalSettingsGUI(player, this); // TODO: Fix registration
        player.openInventory(inventory);
    }

    public boolean isInventory(Inventory inv) {
        return inventory.equals(inv);
    }

    public Inventory getInventory() {
        return inventory;
    }
}
