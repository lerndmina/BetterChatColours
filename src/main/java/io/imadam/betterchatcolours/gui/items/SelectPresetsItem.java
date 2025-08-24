package io.imadam.betterchatcolours.gui.items;

import io.imadam.betterchatcolours.gui.PresetSelectionGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class SelectPresetsItem extends AbstractItem {

  @Override
  public ItemProvider getItemProvider() {
    return new ItemBuilder(Material.ENDER_CHEST)
        .setDisplayName("§d§lSelect Presets")
        .addLoreLines(
            "§7Click to browse available",
            "§7color presets");
  }

  @Override
  public void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
    PresetSelectionGUI.open(player);
  }
}
