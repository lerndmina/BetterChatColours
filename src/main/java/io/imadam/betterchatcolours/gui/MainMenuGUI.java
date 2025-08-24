package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.gui.items.SelectPresetsItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

public class MainMenuGUI {

  public static void open(Player player) {
    boolean isAdmin = player.hasPermission("chatcolors.admin");

    Structure structure;
    if (isAdmin) {
      structure = new Structure(
          "# # # # # # # # #",
          "# s # c # e # d #",
          "# # # # # # # # #")
          .addIngredient('#', GUIUtils.createGlassPane())
          .addIngredient('s', new SelectPresetsItem())
          .addIngredient('c', new SimpleItem(new ItemBuilder(Material.CRAFTING_TABLE)
              .setDisplayName("§a§lCreate Preset")
              .addLoreLines("§7Click to create a new global preset")))
          .addIngredient('e', new SimpleItem(new ItemBuilder(Material.ANVIL)
              .setDisplayName("§e§lEdit Presets")
              .addLoreLines("§7Click to edit existing presets")))
          .addIngredient('d', new SimpleItem(new ItemBuilder(Material.BARRIER)
              .setDisplayName("§c§lDelete Presets")
              .addLoreLines("§7Click to delete presets")));
    } else {
      structure = new Structure(
          "# # # # # # # # #",
          "# # # s # # # # #",
          "# # # # # # # # #")
          .addIngredient('#', GUIUtils.createGlassPane())
          .addIngredient('s', new SelectPresetsItem());
    }

    Gui gui = Gui.normal()
        .setStructure(structure)
        .build();

    Window window = Window.single()
        .setViewer(player)
        .setTitle(isAdmin ? "§8Chat Colors - Admin Menu" : "§8Chat Colors")
        .setGui(gui)
        .build();

    window.open();
  }
}
