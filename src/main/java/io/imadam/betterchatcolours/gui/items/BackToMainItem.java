package io.imadam.betterchatcolours.gui.items;

import io.imadam.betterchatcolours.gui.MainMenuGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class BackToMainItem extends AbstractItem {

  @Override
  public ItemProvider getItemProvider() {
    return new ItemBuilder(Material.ARROW)
        .setDisplayName("§e← Back to Main Menu")
        .addLoreLines("§7Return to the main menu");
  }

  @Override
  public void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
    player.closeInventory();
    // Small delay to ensure inventory closes before opening new one
    org.bukkit.Bukkit.getScheduler().runTaskLater(
        org.bukkit.plugin.java.JavaPlugin.getPlugin(io.imadam.betterchatcolours.BetterChatColours.class),
        () -> MainMenuGUI.open(player),
        1L
    );
  }
}
