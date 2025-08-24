package io.imadam.betterchatcolours.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;

import java.util.List;

public class GUIUtils {

  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

  public static SimpleItem createGlassPane() {
    return new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));
  }

  public static SimpleItem createBorderPane() {
    return new SimpleItem(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" "));
  }

  public static Component createGradientText(String text, List<String> colors) {
    if (colors == null || colors.isEmpty()) {
      return Component.text(text);
    }

    if (colors.size() == 1) {
      String colorTag = "<color:" + colors.get(0) + ">" + text + "</color>";
      return MINI_MESSAGE.deserialize(colorTag);
    }

    StringBuilder gradient = new StringBuilder("<gradient:");
    for (int i = 0; i < colors.size(); i++) {
      if (i > 0)
        gradient.append(":");
      gradient.append(colors.get(i));
    }
    gradient.append(">").append(text).append("</gradient>");

    try {
      return MINI_MESSAGE.deserialize(gradient.toString());
    } catch (Exception e) {
      // Fallback to plain text if gradient parsing fails
      return Component.text(text);
    }
  }

  public static String createGradientTitle(String text, List<String> colors) {
    if (colors == null || colors.isEmpty()) {
      return text;
    }

    if (colors.size() == 1) {
      return "<color:" + colors.get(0) + ">" + text + "</color>";
    }

    StringBuilder gradient = new StringBuilder("<gradient:");
    for (int i = 0; i < colors.size(); i++) {
      if (i > 0)
        gradient.append(":");
      gradient.append(colors.get(i));
    }
    gradient.append(">").append(text).append("</gradient>");

    return gradient.toString();
  }

  public static Material getClosestDyeColor(String hexColor) {
    if (hexColor == null || hexColor.isEmpty()) {
      return Material.WHITE_DYE;
    }

    // Remove # if present
    if (hexColor.startsWith("#")) {
      hexColor = hexColor.substring(1);
    }

    try {
      int rgb = Integer.parseInt(hexColor, 16);
      int r = (rgb >> 16) & 0xFF;
      int g = (rgb >> 8) & 0xFF;
      int b = rgb & 0xFF;

      // Simple color matching logic
      if (r > 200 && g < 100 && b < 100)
        return Material.RED_DYE;
      if (r > 200 && g > 150 && b < 100)
        return Material.ORANGE_DYE;
      if (r > 200 && g > 200 && b < 100)
        return Material.YELLOW_DYE;
      if (r < 100 && g > 150 && b < 100)
        return Material.LIME_DYE;
      if (r < 100 && g > 100 && b < 100)
        return Material.GREEN_DYE;
      if (r < 100 && g > 150 && b > 150)
        return Material.CYAN_DYE;
      if (r < 100 && g < 150 && b > 200)
        return Material.BLUE_DYE;
      if (r > 150 && g < 100 && b > 150)
        return Material.MAGENTA_DYE;
      if (r > 150 && g < 150 && b > 150)
        return Material.PINK_DYE;
      if (r > 150 && g > 150 && g > 150)
        return Material.WHITE_DYE;
      if (r < 50 && g < 50 && b < 50)
        return Material.BLACK_DYE;
      if (r < 100 && g < 100 && b < 100)
        return Material.GRAY_DYE;

      return Material.LIGHT_GRAY_DYE;

    } catch (NumberFormatException e) {
      return Material.WHITE_DYE;
    }
  }

  public static Material getClosestConcreteColor(String hexColor) {
    if (hexColor == null || hexColor.isEmpty()) {
      return Material.WHITE_CONCRETE;
    }

    // Remove # if present
    if (hexColor.startsWith("#")) {
      hexColor = hexColor.substring(1);
    }

    try {
      int rgb = Integer.parseInt(hexColor, 16);
      int r = (rgb >> 16) & 0xFF;
      int g = (rgb >> 8) & 0xFF;
      int b = rgb & 0xFF;

      // Enhanced color matching logic for concrete blocks
      if (r > 200 && g < 100 && b < 100)
        return Material.RED_CONCRETE;
      if (r > 200 && g > 150 && b < 100)
        return Material.ORANGE_CONCRETE;
      if (r > 200 && g > 200 && b < 100)
        return Material.YELLOW_CONCRETE;
      if (r < 100 && g > 200 && b < 100)
        return Material.LIME_CONCRETE;
      if (r < 100 && g > 100 && b < 100)
        return Material.GREEN_CONCRETE;
      if (r < 100 && g > 150 && b > 150)
        return Material.CYAN_CONCRETE;
      if (r < 100 && g < 100 && b > 200)
        return Material.BLUE_CONCRETE;
      if (r < 100 && g < 150 && b > 150)
        return Material.LIGHT_BLUE_CONCRETE;
      if (r > 150 && g < 100 && b > 150)
        return Material.MAGENTA_CONCRETE;
      if (r > 150 && g < 150 && b > 150)
        return Material.PINK_CONCRETE;
      if (r > 150 && g > 100 && b > 200)
        return Material.PURPLE_CONCRETE;
      if (r > 200 && g > 200 && b > 200)
        return Material.WHITE_CONCRETE;
      if (r < 50 && g < 50 && b < 50)
        return Material.BLACK_CONCRETE;
      if (r < 100 && g < 100 && b < 100)
        return Material.GRAY_CONCRETE;
      if (r < 150 && g < 150 && b < 150)
        return Material.LIGHT_GRAY_CONCRETE;
      if (r > 100 && g > 50 && b < 50)
        return Material.BROWN_CONCRETE;

      return Material.LIGHT_GRAY_CONCRETE;

    } catch (NumberFormatException e) {
      return Material.WHITE_CONCRETE;
    }
  }
}
