package io.imadam.betterchatcolours.gui.items;

import io.imadam.betterchatcolours.gui.InvUIAdminPresetCreateGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class CreatePresetItem extends AbstractItem {

  @Override
  public ItemProvider getItemProvider() {
    return new ItemBuilder(Material.CRAFTING_TABLE)
        .setDisplayName("§a§lCreate Preset")
        .addLoreLines(
            "§7Click to create a new",
            "§7global preset"
        );
  }

  @Override
  public void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
    if (!player.hasPermission("chatcolor.admin")) {
      player.sendMessage(Component.text("You don't have permission to create presets!", NamedTextColor.RED));
      return;
    }
    
    player.closeInventory();
    InvUIAdminPresetCreateGUI.openForCreation(player);
  }
}