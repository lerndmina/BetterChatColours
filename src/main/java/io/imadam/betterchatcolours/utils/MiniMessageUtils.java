package io.imadam.betterchatcolours.utils;

import io.imadam.betterchatcolours.config.ConfigManager;
import io.imadam.betterchatcolours.data.PresetData;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiniMessageUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Pattern to match existing color codes (legacy & and MiniMessage)
    private static final Pattern COLOR_PATTERN = Pattern.compile("(&[0-9a-fk-or])|(<[^>]*>)");

    // Pattern to match formatting codes that should be preserved
    private static final Pattern FORMATTING_PATTERN = Pattern
            .compile("(&[k-or])|(<(bold|italic|underlined|strikethrough|obfuscated|b|i|u|st|o)>)");

    public static String processMessage(PresetData gradient, String message, ConfigManager config) {
        if (gradient == null || gradient.getColors().isEmpty() || message == null || message.isEmpty()) {
            return message;
        }

        String processedMessage = message;

        // Strip existing colors if configured to do so
        if (config.shouldStripExistingColors()) {
            if (config.shouldPreserveFormatting()) {
                processedMessage = stripColorsPreserveFormatting(processedMessage);
            } else {
                processedMessage = stripAllFormatting(processedMessage);
            }
        }

        // Apply gradient
        return applyGradient(gradient, processedMessage);
    }

    private static String stripColorsPreserveFormatting(String message) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = COLOR_PATTERN.matcher(message);
        int lastEnd = 0;

        while (matcher.find()) {
            // Add text before the match
            result.append(message, lastEnd, matcher.start());

            String match = matcher.group();
            // Keep formatting codes, remove color codes
            if (FORMATTING_PATTERN.matcher(match).matches()) {
                result.append(match);
            }
            // Skip color codes

            lastEnd = matcher.end();
        }

        // Add remaining text
        result.append(message.substring(lastEnd));

        return result.toString();
    }

    private static String stripAllFormatting(String message) {
        return COLOR_PATTERN.matcher(message).replaceAll("");
    }

    private static String applyGradient(PresetData gradient, String message) {
        if (gradient.getColors().size() == 1) {
            // Single color
            return "<color:" + gradient.getColors().get(0) + ">" + message + "</color>";
        } else {
            // Multi-color gradient
            String gradientTag = "<gradient:" + String.join(":", gradient.getColors()) + ">";
            return gradientTag + message + "</gradient>";
        }
    }

    public static String createGradientPreview(PresetData gradient, String sampleText) {
        if (gradient == null || gradient.getColors().isEmpty()) {
            return sampleText;
        }

        return applyGradient(gradient, sampleText);
    }
}
