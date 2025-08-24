package com.wilddev.betterchatcolours.placeholders;

import com.wilddev.betterchatcolours.BetterChatColours;
import com.wilddev.betterchatcolours.data.PresetData;
import com.wilddev.betterchatcolours.utils.MiniMessageUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatColorsExpansion extends PlaceholderExpansion {

    private final BetterChatColours plugin;

    public ChatColorsExpansion(BetterChatColours plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "chatcolor";
    }

    @Override
    public @NotNull String getAuthor() {
        return "WildDev"; // Keep this simple since it won't change often
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion(); // Deprecated but still the standard way
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        PresetData activeGradient = plugin.getUserDataManager().getActiveGradient(player.getUniqueId());

        switch (params.toLowerCase()) {
            case "before":
                return getGradientBefore(activeGradient);

            case "after":
                return getGradientAfter(activeGradient);

            default:
                // Handle process:MESSAGE format
                if (params.toLowerCase().startsWith("process:")) {
                    String message = params.substring(8); // Remove "process:" prefix
                    return processMessage(activeGradient, message);
                }

                return "";
        }
    }

    private String getGradientBefore(PresetData gradient) {
        if (gradient == null || gradient.getColors().isEmpty()) {
            return "";
        }

        if (gradient.getColors().size() == 1) {
            return "<color:" + gradient.getColors().get(0) + ">";
        }

        return "<gradient:" + String.join(":", gradient.getColors()) + ">";
    }

    private String getGradientAfter(PresetData gradient) {
        if (gradient == null || gradient.getColors().isEmpty()) {
            return "";
        }

        if (gradient.getColors().size() == 1) {
            return "</color>";
        }

        return "</gradient>";
    }

    private String processMessage(PresetData gradient, String message) {
        if (gradient == null || gradient.getColors().isEmpty() || message.isEmpty()) {
            return message;
        }

        // Process the message with gradient
        return MiniMessageUtils.processMessage(gradient, message, plugin.getConfigManager());
    }
}
