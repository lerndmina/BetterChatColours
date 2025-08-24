package io.imadam.betterchatcolours.gui.items;

import io.imadam.betterchatcolours.BetterChatColours;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class UnequipPresetItem extends AbstractItem {

  @Override
  public ItemProvider getItemProvider() {
    return new ItemBuilder(Material.BARRIER)
        .setDisplayName("§c§lUnequip Current Preset")
        .addLoreLines(
            "§7Click to remove your current",
            "§7chatcolor preset and return",
            "§7to default chat appearance.",
            "",
            "§e§lClick to unequip!"
        );
  }

  @Override
  public void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
    BetterChatColours plugin = JavaPlugin.getPlugin(BetterChatColours.class);
    
    // Remove the equipped preset
    plugin.getUserDataManager().setEquippedPreset(player.getUniqueId(), null);
    
    // Send confirmation message
    player.sendMessage("§a§lSuccess! §7Your chatcolor preset has been unequipped.");
    player.sendMessage("§7Your messages will now appear in default colors.");
    
    // Close the GUI
    player.closeInventory();
  }
}
