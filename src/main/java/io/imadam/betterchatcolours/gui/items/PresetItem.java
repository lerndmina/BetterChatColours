package io.imadam.betterchatcolours.gui.items;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.data.GlobalPresetData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class PresetItem extends AbstractItem {

  private final GlobalPresetData preset;

  public PresetItem(GlobalPresetData preset) {
    this.preset = preset;
  }

  @Override
  public ItemProvider getItemProvider() {
    String gradientName = applyGradientToText(preset.getName());

    return new ItemBuilder(Material.PAPER)
        .setDisplayName(gradientName)
        .addLoreLines(
            "§7Colors: " + preset.getColors().size(),
            "",
            "§aClick to equip this preset");
  }

  private String applyGradientToText(String text) {
    if (preset.getColors() == null || preset.getColors().isEmpty()) {
      return "§e§l" + text;
    }

    try {
      String gradientMessage = preset.getGradientTag() + text + preset.getClosingTag();
      var component = MiniMessage.miniMessage().deserialize(gradientMessage);
      return LegacyComponentSerializer.legacySection().serialize(component);
    } catch (Exception e) {
      // Fallback to yellow text if gradient parsing fails
      return "§e§l" + text;
    }
  }

  @Override
  public void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
    // Equip this preset for the player
    BetterChatColours plugin = JavaPlugin.getPlugin(BetterChatColours.class);
    plugin.getUserDataManager().setEquippedPreset(player.getUniqueId(), preset.getName());
    player.sendMessage(Component.text("Equipped preset: " + preset.getName(), NamedTextColor.GREEN));
    player.closeInventory();
  }
}
