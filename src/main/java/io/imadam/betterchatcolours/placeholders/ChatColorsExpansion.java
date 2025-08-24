package io.imadam.betterchatcolours.placeholders;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.data.GlobalPresetData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatColorsExpansion extends PlaceholderExpansion {

  private final BetterChatColours plugin;
  private final MiniMessage miniMessage;

  public ChatColorsExpansion(BetterChatColours plugin) {
    this.plugin = plugin;
    this.miniMessage = MiniMessage.miniMessage();
  }

  @Override
  public @NotNull String getIdentifier() {
    return "chatcolor";
  }

  @Override
  public @NotNull String getAuthor() {
    return "imadam";
  }

  @Override
  public @NotNull String getVersion() {
    return plugin.getDescription().getVersion();
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public String onPlaceholderRequest(Player player, @NotNull String identifier) {
    if (player == null) {
      return "";
    }

    String equippedPreset = plugin.getUserDataManager().getEquippedPreset(player);
    if (equippedPreset == null || equippedPreset.isEmpty()) {
      return "";
    }

    GlobalPresetData preset = plugin.getGlobalPresetManager().getPreset(equippedPreset);
    if (preset == null) {
      return "";
    }

    switch (identifier.toLowerCase()) {
      case "before":
        return preset.getGradientTag();

      case "after":
        return preset.getClosingTag();

      default:
        // Handle process:MESSAGE format
        if (identifier.toLowerCase().startsWith("process:")) {
          String message = identifier.substring(8); // Remove "process:" prefix
          return processMessage(preset, message);
        }
        return "";
    }
  }

  private String processMessage(GlobalPresetData preset, String message) {
    if (message == null || message.isEmpty()) {
      return "";
    }

    // Create the full gradient message
    String gradientMessage = preset.getGradientTag() + message + preset.getClosingTag();

    try {
      // Parse with MiniMessage and convert to legacy format for compatibility
      var component = miniMessage.deserialize(gradientMessage);
      return LegacyComponentSerializer.legacySection().serialize(component);
    } catch (Exception e) {
      // If parsing fails, return the original message
      plugin.getLogger().warning("Failed to process gradient message: " + gradientMessage);
      return message;
    }
  }
}
