package io.imadam.betterchatcolours.gui.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class CloseMenuItem extends AbstractItem {

  @Override
  public ItemProvider getItemProvider() {
    return new ItemBuilder(Material.BARRIER)
        .setDisplayName("§c✕ Close Menu")
        .addLoreLines("§7Close this menu");
  }

  @Override
  public void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
    player.closeInventory();
  }
}
