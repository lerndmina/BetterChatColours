package io.imadam.betterchatcolours.gui;

import io.imadam.betterchatcolours.gui.items.CreatePresetItem;
import io.imadam.betterchatcolours.gui.items.EditPresetItem;
import io.imadam.betterchatcolours.gui.items.SelectPresetsItem;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.window.Window;

public class MainMenuGUI {

  public static void open(Player player) {
    boolean isAdmin = player.hasPermission("betterchatcolours.admin");

    Structure structure;
    if (isAdmin) {
      structure = new Structure(
          "# # # # # # # # #",
          "# # s # c # e # #",
          "# # # # # # # # #")
          .addIngredient('#', GUIUtils.createGlassPane())
          .addIngredient('s', new SelectPresetsItem())
          .addIngredient('c', new CreatePresetItem())
          .addIngredient('e', new EditPresetItem());
    } else {
      structure = new Structure(
          "# # # # # # # # #",
          "# # # # s # # # #",
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
