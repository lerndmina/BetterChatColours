package com.wilddev.betterchatcolours.config;

import com.wilddev.betterchatcolours.BetterChatColours;
import com.wilddev.betterchatcolours.data.ColorData;
import org.bukkit.configuration.ConfigurationSection;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class ConfigManager {

    private final BetterChatColours plugin;
    private final Pattern hexPattern = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private Map<String, ColorData> availableColors = new HashMap<>();

    // Settings
    private int maxColorsPerGradient;
    private int maxPresetsPerUser;
    private int guiSize;
    private boolean validateHexCodes;
    private boolean stripExistingColors;
    private boolean preserveFormatting;

    // Permission-based preset limits
    private Map<String, Integer> presetLimits = new HashMap<>();

    public ConfigManager(BetterChatColours plugin) {
        this.plugin = plugin;
        loadConfiguration();
    }

    public void reload() {
        plugin.reloadConfig();
        loadConfiguration();
    }

    private void loadConfiguration() {
        // Load settings
        ConfigurationSection settings = plugin.getConfig().getConfigurationSection("settings");
        if (settings != null) {
            maxColorsPerGradient = settings.getInt("max-colors-per-gradient", 5);
            maxPresetsPerUser = settings.getInt("max-presets-per-user", 10);
            guiSize = settings.getInt("gui-size", 54);
            validateHexCodes = settings.getBoolean("validate-hex-codes", true);
            stripExistingColors = settings.getBoolean("strip-existing-colors", true);
            preserveFormatting = settings.getBoolean("preserve-formatting", true);
        } else {
            // Set defaults if section doesn't exist
            maxColorsPerGradient = 5;
            maxPresetsPerUser = 10;
            guiSize = 54;
            validateHexCodes = true;
            stripExistingColors = true;
            preserveFormatting = true;
        }

        // Load preset limits
        presetLimits.clear();
        ConfigurationSection permissions = plugin.getConfig().getConfigurationSection("permissions.preset-limits");
        if (permissions != null) {
            for (String key : permissions.getKeys(false)) {
                presetLimits.put(key, permissions.getInt(key));
            }
        }

        // Load colors
        loadColors();
    }

    private void loadColors() {
        availableColors.clear();
        ConfigurationSection colorsSection = plugin.getConfig().getConfigurationSection("colours");

        if (colorsSection == null) {
            plugin.getLogger().warning("No colors section found in config! Creating default colors...");
            createDefaultColors();
            return;
        }

        for (String colorName : colorsSection.getKeys(false)) {
            ConfigurationSection colorSection = colorsSection.getConfigurationSection(colorName);
            if (colorSection == null)
                continue;

            String hexCode = colorSection.getString("colour");
            if (hexCode == null) {
                plugin.getLogger().warning("Color '" + colorName + "' has no hex code defined! Skipping...");
                continue;
            }

            // Validate hex code
            if (validateHexCodes && !isValidHexCode(hexCode)) {
                plugin.getLogger()
                        .warning("Invalid hex code for color '" + colorName + "': " + hexCode + ". Skipping...");
                continue;
            }

            String permission = colorSection.getString("permission", "chatcolors.color.use");
            String displayName = colorSection.getString("displayname", formatDisplayName(colorName));

            ColorData colorData = new ColorData(colorName, hexCode, permission, displayName);
            availableColors.put(colorName, colorData);
        }

        plugin.getLogger().info("Loaded " + availableColors.size() + " colors from configuration");
    }

    private void createDefaultColors() {
        // Create some default colors if none exist
        Map<String, String> defaultColors = new HashMap<>();
        defaultColors.put("red", "#FF0000");
        defaultColors.put("green", "#00FF00");
        defaultColors.put("blue", "#0000FF");
        defaultColors.put("yellow", "#FFFF00");
        defaultColors.put("purple", "#800080");
        defaultColors.put("orange", "#FFA500");
        defaultColors.put("pink", "#FFC0CB");
        defaultColors.put("cyan", "#00FFFF");

        for (Map.Entry<String, String> entry : defaultColors.entrySet()) {
            String colorName = entry.getKey();
            String hexCode = entry.getValue();
            ColorData colorData = new ColorData(colorName, hexCode, "chatcolors.color.use",
                    formatDisplayName(colorName));
            availableColors.put(colorName, colorData);
        }

        plugin.getLogger().info("Created " + defaultColors.size() + " default colors");
    }

    private boolean isValidHexCode(String hex) {
        if (!hexPattern.matcher(hex).matches()) {
            return false;
        }

        try {
            Color.decode(hex);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String formatDisplayName(String colorName) {
        return colorName.substring(0, 1).toUpperCase() + colorName.substring(1).toLowerCase();
    }

    public void addColor(String colorName, String hexCode, String permission, String displayName) {
        if (validateHexCodes && !isValidHexCode(hexCode)) {
            throw new IllegalArgumentException("Invalid hex code: " + hexCode);
        }

        if (permission == null)
            permission = "chatcolors.color.use";
        if (displayName == null)
            displayName = formatDisplayName(colorName);

        ColorData colorData = new ColorData(colorName, hexCode, permission, displayName);
        availableColors.put(colorName, colorData);

        // Save to config
        plugin.getConfig().set("colours." + colorName + ".colour", hexCode);
        plugin.getConfig().set("colours." + colorName + ".permission", permission);
        plugin.getConfig().set("colours." + colorName + ".displayname", displayName);
        plugin.saveConfig();

        plugin.getLogger().info("Added new color: " + colorName + " (" + hexCode + ")");
    }

    // Getters
    public Map<String, ColorData> getAvailableColors() {
        return new HashMap<>(availableColors);
    }

    public ColorData getColor(String colorName) {
        return availableColors.get(colorName);
    }

    public int getMaxColorsPerGradient() {
        return maxColorsPerGradient;
    }

    public int getMaxPresetsPerUser() {
        return maxPresetsPerUser;
    }

    public int getGuiSize() {
        return guiSize;
    }

    public boolean shouldValidateHexCodes() {
        return validateHexCodes;
    }

    public boolean shouldStripExistingColors() {
        return stripExistingColors;
    }

    public boolean shouldPreserveFormatting() {
        return preserveFormatting;
    }

    public Map<String, Integer> getPresetLimits() {
        return new HashMap<>(presetLimits);
    }
    
    // Phase 2 Enhancements: Advanced configuration management
    
    /**
     * Get colors that a player has permission to use
     */
    public Map<String, ColorData> getAvailableColorsForPlayer(org.bukkit.entity.Player player) {
        Map<String, ColorData> availableForPlayer = new HashMap<>();
        
        for (Map.Entry<String, ColorData> entry : availableColors.entrySet()) {
            ColorData color = entry.getValue();
            if (player.hasPermission(color.getPermission())) {
                availableForPlayer.put(entry.getKey(), color);
            }
        }
        
        return availableForPlayer;
    }
    
    /**
     * Check if a color exists and player has permission
     */
    public boolean canPlayerUseColor(org.bukkit.entity.Player player, String colorName) {
        ColorData color = availableColors.get(colorName);
        if (color == null) return false;
        
        return player.hasPermission(color.getPermission());
    }
    
    /**
     * Remove a color from configuration
     */
    public boolean removeColor(String colorName) {
        if (!availableColors.containsKey(colorName)) {
            return false;
        }
        
        availableColors.remove(colorName);
        plugin.getConfig().set("colours." + colorName, null);
        plugin.saveConfig();
        
        plugin.getLogger().info("Removed color: " + colorName);
        return true;
    }
    
    /**
     * Update an existing color
     */
    public boolean updateColor(String colorName, String newHexCode, String newPermission, String newDisplayName) {
        if (!availableColors.containsKey(colorName)) {
            return false;
        }
        
        if (validateHexCodes && !isValidHexCode(newHexCode)) {
            throw new IllegalArgumentException("Invalid hex code: " + newHexCode);
        }
        
        ColorData existingColor = availableColors.get(colorName);
        String permission = newPermission != null ? newPermission : existingColor.getPermission();
        String displayName = newDisplayName != null ? newDisplayName : existingColor.getDisplayName();
        
        ColorData updatedColor = new ColorData(colorName, newHexCode, permission, displayName);
        availableColors.put(colorName, updatedColor);
        
        // Update config
        plugin.getConfig().set("colours." + colorName + ".colour", newHexCode);
        plugin.getConfig().set("colours." + colorName + ".permission", permission);
        plugin.getConfig().set("colours." + colorName + ".displayname", displayName);
        plugin.saveConfig();
        
        plugin.getLogger().info("Updated color: " + colorName);
        return true;
    }
    
    /**
     * Get color statistics
     */
    public ColorStats getColorStats() {
        int totalColors = availableColors.size();
        int protectedColors = (int) availableColors.values().stream()
                .filter(color -> !color.getPermission().equals("chatcolors.color.use"))
                .count();
        int publicColors = totalColors - protectedColors;
        
        return new ColorStats(totalColors, publicColors, protectedColors);
    }
    
    /**
     * Validate configuration integrity
     */
    public java.util.List<String> validateConfiguration() {
        java.util.List<String> issues = new java.util.ArrayList<>();
        
        // Check for duplicate colors
        Map<String, java.util.List<String>> hexToNames = new HashMap<>();
        for (Map.Entry<String, ColorData> entry : availableColors.entrySet()) {
            String hex = entry.getValue().getHexCode();
            hexToNames.computeIfAbsent(hex, k -> new java.util.ArrayList<>()).add(entry.getKey());
        }
        
        for (Map.Entry<String, java.util.List<String>> entry : hexToNames.entrySet()) {
            if (entry.getValue().size() > 1) {
                issues.add("Duplicate color codes found for " + entry.getKey() + ": " + String.join(", ", entry.getValue()));
            }
        }
        
        // Check for invalid settings
        if (maxColorsPerGradient < 1 || maxColorsPerGradient > 10) {
            issues.add("max-colors-per-gradient should be between 1 and 10, found: " + maxColorsPerGradient);
        }
        
        if (maxPresetsPerUser < 1 || maxPresetsPerUser > 100) {
            issues.add("max-presets-per-user should be between 1 and 100, found: " + maxPresetsPerUser);
        }
        
        if (guiSize % 9 != 0 || guiSize < 9 || guiSize > 54) {
            issues.add("gui-size should be a multiple of 9 between 9 and 54, found: " + guiSize);
        }
        
        return issues;
    }
    
    /**
     * Get configuration summary for admin commands
     */
    public String getConfigSummary() {
        return String.format(
            "Colors: %d total (%d public, %d restricted)\n" +
            "Max colors per gradient: %d\n" +
            "Max presets per user: %d\n" +
            "GUI size: %d slots\n" +
            "Validation enabled: %s",
            availableColors.size(),
            getColorStats().getPublicColors(),
            getColorStats().getProtectedColors(),
            maxColorsPerGradient,
            maxPresetsPerUser,
            guiSize,
            validateHexCodes ? "Yes" : "No"
        );
    }
    
    /**
     * Inner class for color statistics
     */
    public static class ColorStats {
        private final int totalColors;
        private final int publicColors;
        private final int protectedColors;
        
        public ColorStats(int totalColors, int publicColors, int protectedColors) {
            this.totalColors = totalColors;
            this.publicColors = publicColors;
            this.protectedColors = protectedColors;
        }
        
        public int getTotalColors() { return totalColors; }
        public int getPublicColors() { return publicColors; }
        public int getProtectedColors() { return protectedColors; }
    }
}
