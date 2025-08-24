package io.imadam.betterchatcolours.utils;

import io.imadam.betterchatcolours.data.ColorData;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorUtils {

  // Map of colors to their closest wool equivalents for GUI display
  private static final Map<String, Material> WOOL_COLOR_MAP = new HashMap<>();

  static {
    // Initialize wool color mappings
    WOOL_COLOR_MAP.put("#FFFFFF", Material.WHITE_WOOL);
    WOOL_COLOR_MAP.put("#D88198", Material.PINK_WOOL);
    WOOL_COLOR_MAP.put("#B02E26", Material.RED_WOOL);
    WOOL_COLOR_MAP.put("#F9801D", Material.ORANGE_WOOL);
    WOOL_COLOR_MAP.put("#FED83D", Material.YELLOW_WOOL);
    WOOL_COLOR_MAP.put("#80C71F", Material.LIME_WOOL);
    WOOL_COLOR_MAP.put("#5E7C16", Material.GREEN_WOOL);
    WOOL_COLOR_MAP.put("#169C9C", Material.CYAN_WOOL);
    WOOL_COLOR_MAP.put("#3AB3DA", Material.LIGHT_BLUE_WOOL);
    WOOL_COLOR_MAP.put("#3C44AA", Material.BLUE_WOOL);
    WOOL_COLOR_MAP.put("#8932B8", Material.PURPLE_WOOL);
    WOOL_COLOR_MAP.put("#C74EBD", Material.MAGENTA_WOOL);
    WOOL_COLOR_MAP.put("#F38BAA", Material.LIGHT_GRAY_WOOL);
    WOOL_COLOR_MAP.put("#9D9D97", Material.GRAY_WOOL);
    WOOL_COLOR_MAP.put("#474F52", Material.BLACK_WOOL);
    WOOL_COLOR_MAP.put("#835432", Material.BROWN_WOOL);
  }

  /**
   * Convert a hex color to the closest Minecraft wool material
   */
  public static Material getClosestWool(String hexColor) {
    if (hexColor == null || !hexColor.startsWith("#") || hexColor.length() != 7) {
      return Material.WHITE_WOOL;
    }

    try {
      Color targetColor = Color.decode(hexColor);
      Material closestWool = Material.WHITE_WOOL;
      double minDistance = Double.MAX_VALUE;

      for (Map.Entry<String, Material> entry : WOOL_COLOR_MAP.entrySet()) {
        Color woolColor = Color.decode(entry.getKey());
        double distance = calculateColorDistance(targetColor, woolColor);

        if (distance < minDistance) {
          minDistance = distance;
          closestWool = entry.getValue();
        }
      }

      return closestWool;

    } catch (NumberFormatException e) {
      return Material.WHITE_WOOL;
    }
  }

  /**
   * Calculate RGB distance between two colors
   */
  private static double calculateColorDistance(Color c1, Color c2) {
    double deltaR = c1.getRed() - c2.getRed();
    double deltaG = c1.getGreen() - c2.getGreen();
    double deltaB = c1.getBlue() - c2.getBlue();

    return Math.sqrt(deltaR * deltaR + deltaG * deltaG + deltaB * deltaB);
  }

  /**
   * Validate if a string is a valid hex color
   */
  public static boolean isValidHex(String hex) {
    if (hex == null || !hex.startsWith("#") || hex.length() != 7) {
      return false;
    }

    try {
      Color.decode(hex);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Get RGB components from hex color
   */
  public static int[] getRGBFromHex(String hex) {
    if (!isValidHex(hex)) {
      return new int[] { 255, 255, 255 }; // Default to white
    }

    Color color = Color.decode(hex);
    return new int[] { color.getRed(), color.getGreen(), color.getBlue() };
  }

  /**
   * Convert RGB to hex string
   */
  public static String rgbToHex(int r, int g, int b) {
    return String.format("#%02X%02X%02X", r, g, b);
  }

  /**
   * Get a display name for a color based on its hex value
   */
  public static String getColorDisplayName(ColorData colorData) {
    if (colorData.getDisplayName() != null && !colorData.getDisplayName().isEmpty()) {
      return colorData.getDisplayName();
    }

    return formatColorName(colorData.getName());
  }

  /**
   * Format a color name (capitalize first letter of each word)
   */
  public static String formatColorName(String name) {
    if (name == null || name.isEmpty()) {
      return "Unknown";
    }

    String[] words = name.toLowerCase().split("_| |-");
    StringBuilder formatted = new StringBuilder();

    for (int i = 0; i < words.length; i++) {
      if (i > 0)
        formatted.append(" ");
      if (!words[i].isEmpty()) {
        formatted.append(Character.toUpperCase(words[i].charAt(0)));
        if (words[i].length() > 1) {
          formatted.append(words[i].substring(1));
        }
      }
    }

    return formatted.toString();
  }

  /**
   * Create a gradient preview string for GUI titles
   */
  public static String createGradientPreview(java.util.List<String> colors, String sampleText) {
    if (colors == null || colors.isEmpty()) {
      return sampleText;
    }

    if (colors.size() == 1) {
      return "<color:" + colors.get(0) + ">" + sampleText + "</color>";
    }

    return "<gradient:" + String.join(":", colors) + ">" + sampleText + "</gradient>";
  }

  /**
   * Create a gradient preview from NamedTextColor list
   */
  public static String createGradientPreviewFromNamedColors(List<NamedTextColor> colors, String sampleText) {
    if (colors == null || colors.isEmpty()) {
      return sampleText;
    }

    if (colors.size() == 1) {
      return "<color:" + colors.get(0).asHexString() + ">" + sampleText + "</color>";
    }

    StringBuilder gradient = new StringBuilder("<gradient:");
    for (int i = 0; i < colors.size(); i++) {
      if (i > 0)
        gradient.append(":");
      gradient.append(colors.get(i).asHexString());
    }
    gradient.append(">").append(sampleText).append("</gradient>");

    return gradient.toString();
  }

  /**
   * Get the wool material that best represents a NamedTextColor
   */
  public static Material getWoolMaterial(NamedTextColor color) {
    return getClosestWool(color.asHexString());
  }

  /**
   * Get a readable name for a NamedTextColor
   */
  public static String getColorName(NamedTextColor color) {
    return formatColorName(color.toString().toLowerCase());
  }
}
