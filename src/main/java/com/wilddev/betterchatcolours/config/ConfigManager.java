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
            if (colorSection == null) continue;
            
            String hexCode = colorSection.getString("colour");
            if (hexCode == null) {
                plugin.getLogger().warning("Color '" + colorName + "' has no hex code defined! Skipping...");
                continue;
            }
            
            // Validate hex code
            if (validateHexCodes && !isValidHexCode(hexCode)) {
                plugin.getLogger().warning("Invalid hex code for color '" + colorName + "': " + hexCode + ". Skipping...");
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
            ColorData colorData = new ColorData(colorName, hexCode, "chatcolors.color.use", formatDisplayName(colorName));
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
        
        if (permission == null) permission = "chatcolors.color.use";
        if (displayName == null) displayName = formatDisplayName(colorName);
        
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
}
