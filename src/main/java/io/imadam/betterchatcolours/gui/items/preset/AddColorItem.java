package io.imadam.betterchatcolours.gui.items.preset;

import io.imadam.betterchatcolours.gui.ChatInputManager;
import io.imadam.betterchatcolours.gui.InvUIAdminPresetCreateGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.List;

public class AddColorItem extends AbstractItem {
  
  private final String presetName;
  private final List<String> colors;
  private final boolean isEditMode;

  public AddColorItem(String presetName, List<String> colors, boolean isEditMode) {
    this.presetName = presetName;
    this.colors = colors;
    this.isEditMode = isEditMode;
  }

  @Override
  public ItemProvider getItemProvider() {
    return new ItemBuilder(Material.LIME_DYE)
        .setDisplayName("§a§lAdd Color")
        .addLoreLines(
            "§7Click to add a new",
            "§7hex color to this preset"
        );
  }

  @Override
  public void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
    ChatInputManager.requestHexColor(player, presetName, colors,
        hexColor -> {
          colors.add(hexColor);
          InvUIAdminPresetCreateGUI.reopenColorSelectionGUI(player, presetName, colors, isEditMode);
        },
        () -> {
          InvUIAdminPresetCreateGUI.reopenColorSelectionGUI(player, presetName, colors, isEditMode);
        });
  }
}
