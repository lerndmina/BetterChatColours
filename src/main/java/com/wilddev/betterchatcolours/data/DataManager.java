package com.wilddev.betterchatcolours.data;

import com.wilddev.betterchatcolours.BetterChatColours;
import com.wilddev.betterchatcolours.config.ConfigManager;
import com.wilddev.betterchatcolours.utils.ColorUtils;
import com.wilddev.betterchatcolours.utils.PermissionUtils;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Central data coordinator for Phase 2 - handles complex data operations
 */
public class DataManager {
    
    private final BetterChatColours plugin;
    private final UserDataManager userDataManager;
    private final ConfigManager configManager;
    
    public DataManager(BetterChatColours plugin) {
        this.plugin = plugin;
        this.userDataManager = plugin.getUserDataManager();
        this.configManager = plugin.getConfigManager();
    }
    
    /**
     * Create a new preset with full validation
     */
    public CreatePresetResult createPreset(Player player, String presetName, List<String> colors) {
        UUID uuid = player.getUniqueId();
        
        // Create preset data
        PresetData preset = new PresetData(presetName, new ArrayList<>(colors));
        
        // Validate preset
        ValidationResult validation = userDataManager.validatePreset(uuid, preset);
        if (!validation.isValid()) {
            return new CreatePresetResult(false, validation.getMessage(), null);
        }
        
        // Check player permissions for each color
        Map<String, ColorData> availableColors = configManager.getAvailableColorsForPlayer(player);
        for (String colorHex : colors) {
            boolean hasPermission = availableColors.values().stream()
                    .anyMatch(colorData -> colorData.getHexCode().equalsIgnoreCase(colorHex));
            
            if (!hasPermission) {
                return new CreatePresetResult(false, "You don't have permission to use color: " + colorHex, null);
            }
        }
        
        // Add preset
        boolean success = userDataManager.addPreset(uuid, preset);
        if (!success) {
            return new CreatePresetResult(false, "A preset with that name already exists", null);
        }
        
        return new CreatePresetResult(true, "Preset created successfully", preset);
    }
    
    /**
     * Get available colors for GUI display with wool mapping
     */
    public List<ColorDisplayData> getAvailableColorsForGUI(Player player) {
        Map<String, ColorData> colors = configManager.getAvailableColorsForPlayer(player);
        
        return colors.values().stream()
                .map(colorData -> new ColorDisplayData(
                        colorData,
                        ColorUtils.getClosestWool(colorData.getHexCode()),
                        ColorUtils.getColorDisplayName(colorData)
                ))
                .sorted(Comparator.comparing(ColorDisplayData::getDisplayName))
                .collect(Collectors.toList());
    }
    
    /**
     * Get user dashboard data for GUIs
     */
    public UserDashboard getUserDashboard(Player player) {
        UUID uuid = player.getUniqueId();
        UserStats stats = userDataManager.getUserStats(uuid);
        List<PresetData> presets = userDataManager.getPresetsSorted(uuid, true); // Newest first
        PresetData activeGradient = userDataManager.getActiveGradient(uuid);
        
        return new UserDashboard(stats, presets, activeGradient);
    }
    
    /**
     * Apply a preset to a player
     */
    public boolean applyPreset(Player player, String presetName) {
        UUID uuid = player.getUniqueId();
        PresetData preset = userDataManager.findPresetByName(uuid, presetName);
        
        if (preset == null) {
            return false;
        }
        
        // Check if player still has permission for all colors in the preset
        Map<String, ColorData> availableColors = configManager.getAvailableColorsForPlayer(player);
        for (String colorHex : preset.getColors()) {
            boolean hasPermission = availableColors.values().stream()
                    .anyMatch(colorData -> colorData.getHexCode().equalsIgnoreCase(colorHex));
            
            if (!hasPermission) {
                return false; // Permission revoked for a color in this preset
            }
        }
        
        userDataManager.setActiveGradient(uuid, preset);
        return true;
    }
    
    /**
     * Search presets by name or colors
     */
    public List<PresetData> searchPresets(Player player, String query) {
        UUID uuid = player.getUniqueId();
        Map<String, PresetData> allPresets = userDataManager.getUserPresets(uuid);
        
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(allPresets.values());
        }
        
        String lowerQuery = query.toLowerCase();
        
        return allPresets.values().stream()
                .filter(preset -> 
                    preset.getName().toLowerCase().contains(lowerQuery) ||
                    preset.getDescription().toLowerCase().contains(lowerQuery) ||
                    preset.getColors().stream().anyMatch(color -> color.toLowerCase().contains(lowerQuery))
                )
                .collect(Collectors.toList());
    }
    
    /**
     * Get statistics for admin overview
     */
    public SystemStats getSystemStats() {
        int totalUsers = userDataManager.getUserPresetsMap().size();
        int totalPresets = userDataManager.getUserPresetsMap().values().stream()
                .mapToInt(Map::size)
                .sum();
        int activeUsers = userDataManager.getActiveGradientsMap().size();
        int adminForcedUsers = userDataManager.getAdminForcedGradientsMap().size();
        
        ConfigManager.ColorStats colorStats = configManager.getColorStats();
        
        return new SystemStats(totalUsers, totalPresets, activeUsers, adminForcedUsers, colorStats);
    }
    
    /**
     * Cleanup and maintenance operations
     */
    public MaintenanceResult performMaintenance() {
        int cleanedUsers = 0;
        int removedPresets = 0;
        
        for (UUID uuid : new HashSet<>(userDataManager.getUserPresetsMap().keySet())) {
            Map<String, PresetData> presets = userDataManager.getUserPresetsMap().get(uuid);
            if (presets == null) continue;
            
            int originalSize = presets.size();
            userDataManager.cleanupUserData(uuid);
            int newSize = userDataManager.getUserPresetsMap().getOrDefault(uuid, Collections.emptyMap()).size();
            
            if (newSize < originalSize) {
                cleanedUsers++;
                removedPresets += (originalSize - newSize);
            }
        }
        
        return new MaintenanceResult(cleanedUsers, removedPresets);
    }
    
    // Result classes for Phase 2 operations
    
    public static class CreatePresetResult {
        private final boolean success;
        private final String message;
        private final PresetData preset;
        
        public CreatePresetResult(boolean success, String message, PresetData preset) {
            this.success = success;
            this.message = message;
            this.preset = preset;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public PresetData getPreset() { return preset; }
    }
    
    public static class ColorDisplayData {
        private final ColorData colorData;
        private final org.bukkit.Material woolMaterial;
        private final String displayName;
        
        public ColorDisplayData(ColorData colorData, org.bukkit.Material woolMaterial, String displayName) {
            this.colorData = colorData;
            this.woolMaterial = woolMaterial;
            this.displayName = displayName;
        }
        
        public ColorData getColorData() { return colorData; }
        public org.bukkit.Material getWoolMaterial() { return woolMaterial; }
        public String getDisplayName() { return displayName; }
    }
    
    public static class UserDashboard {
        private final UserStats stats;
        private final List<PresetData> presets;
        private final PresetData activeGradient;
        
        public UserDashboard(UserStats stats, List<PresetData> presets, PresetData activeGradient) {
            this.stats = stats;
            this.presets = presets;
            this.activeGradient = activeGradient;
        }
        
        public UserStats getStats() { return stats; }
        public List<PresetData> getPresets() { return presets; }
        public PresetData getActiveGradient() { return activeGradient; }
    }
    
    public static class SystemStats {
        private final int totalUsers;
        private final int totalPresets;
        private final int activeUsers;
        private final int adminForcedUsers;
        private final ConfigManager.ColorStats colorStats;
        
        public SystemStats(int totalUsers, int totalPresets, int activeUsers, int adminForcedUsers, ConfigManager.ColorStats colorStats) {
            this.totalUsers = totalUsers;
            this.totalPresets = totalPresets;
            this.activeUsers = activeUsers;
            this.adminForcedUsers = adminForcedUsers;
            this.colorStats = colorStats;
        }
        
        public int getTotalUsers() { return totalUsers; }
        public int getTotalPresets() { return totalPresets; }
        public int getActiveUsers() { return activeUsers; }
        public int getAdminForcedUsers() { return adminForcedUsers; }
        public ConfigManager.ColorStats getColorStats() { return colorStats; }
    }
    
    public static class MaintenanceResult {
        private final int cleanedUsers;
        private final int removedPresets;
        
        public MaintenanceResult(int cleanedUsers, int removedPresets) {
            this.cleanedUsers = cleanedUsers;
            this.removedPresets = removedPresets;
        }
        
        public int getCleanedUsers() { return cleanedUsers; }
        public int getRemovedPresets() { return removedPresets; }
    }
}
