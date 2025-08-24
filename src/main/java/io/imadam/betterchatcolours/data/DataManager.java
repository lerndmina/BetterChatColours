package io.imadam.betterchatcolours.data;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.config.ConfigManager;
import io.imadam.betterchatcolours.utils.ColorUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Simplified data coordinator for the new global preset system
 * Most functionality has been moved to GlobalPresetManager and UserDataManager
 */
public class DataManager {

    private final BetterChatColours plugin;
    private final UserDataManager userDataManager;
    private final GlobalPresetManager globalPresetManager;

    public DataManager(BetterChatColours plugin) {
        this.plugin = plugin;
        this.userDataManager = plugin.getUserDataManager();
        this.globalPresetManager = plugin.getGlobalPresetManager();
    }

    // === VALIDATION RESULTS (Legacy compatibility) ===
    
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
    }

    // === USER OPERATIONS (Redirected to new system) ===

    public boolean applyPreset(UUID uuid, String presetId) {
        // Check if preset exists and user has permission
        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) {
            return false;
        }

        if (!globalPresetManager.canPlayerUsePreset(player, presetId)) {
            return false;
        }

        userDataManager.equipPreset(uuid, presetId);
        return true;
    }

    public boolean clearActiveGradient(UUID uuid) {
        userDataManager.clearEquippedPreset(uuid);
        return true;
    }

    public PresetData getActiveGradient(UUID uuid) {
        return userDataManager.getActiveGradient(uuid);
    }

    public boolean hasActiveGradient(UUID uuid) {
        return userDataManager.hasGradient(uuid);
    }

    // === ADMIN OPERATIONS ===

    public boolean setAdminForcedGradient(UUID uuid, PresetData gradient) {
        userDataManager.setAdminForcedGradient(uuid, gradient);
        return true;
    }

    public boolean clearAdminForcedGradient(UUID uuid) {
        userDataManager.clearAdminForcedGradient(uuid);
        return true;
    }

    // === STATISTICS (Simplified) ===

    public Map<String, Object> getPluginStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic stats that we can still calculate
        Map<String, GlobalPresetData> allPresets = globalPresetManager.getAllPresets();
        
        stats.put("totalGlobalPresets", allPresets.size());
        stats.put("defaultPresets", allPresets.values().stream().mapToInt(p -> p.isDefault() ? 1 : 0).sum());
        stats.put("customPresets", allPresets.values().stream().mapToInt(p -> !p.isDefault() ? 1 : 0).sum());
        
        return stats;
    }

    // === LEGACY METHODS (Disabled/Simplified) ===

    // These methods are from the old user preset system and are no longer supported
    // They're kept for compilation compatibility but return default values

    public ValidationResult validatePreset(UUID uuid, PresetData preset) {
        // In the new system, validation is handled by GlobalPresetManager
        return ValidationResult.success();
    }

    public boolean addPreset(UUID uuid, PresetData preset) {
        // Users can no longer create presets directly
        return false;
    }

    public boolean removePreset(UUID uuid, String presetName) {
        // Users can no longer remove presets directly  
        return false;
    }

    public Map<String, PresetData> getUserPresets(UUID uuid) {
        // Users no longer have individual presets, return empty map
        return new HashMap<>();
    }

    public boolean hasPreset(UUID uuid, String presetName) {
        // Check if global preset exists and user has permission
        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) {
            return false;
        }
        
        String presetId = findGlobalPresetIdByName(presetName);
        return presetId != null && globalPresetManager.canPlayerUsePreset(player, presetId);
    }

    public List<GlobalPresetData> getPublishedGlobalPresets() {
        return globalPresetManager.getAllPresets().values().stream()
            .filter(preset -> preset.isGlobal() && preset.isPublished())
            .collect(Collectors.toList());
    }

    // === HELPER METHODS ===

    private String findGlobalPresetIdByName(String name) {
        return globalPresetManager.getAllPresets().entrySet().stream()
            .filter(entry -> entry.getValue().getName().equalsIgnoreCase(name))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    // === CLEANUP OPERATIONS ===

    public void performMaintenanceCleanup() {
        // Basic cleanup - most maintenance is now handled by individual managers
        plugin.getLogger().info("Performing maintenance cleanup...");
        
        // Save all data
        userDataManager.saveAllData();
        globalPresetManager.saveGlobalPresets();
        
        plugin.getLogger().info("Maintenance cleanup completed");
    }

    // === UNSUPPORTED LEGACY METHODS ===
    
    // These methods are from the old system and are no longer supported
    // They're included to prevent compilation errors but don't do anything

    public List<PresetData> getPresetsSorted(UUID uuid, boolean newestFirst) {
        return new ArrayList<>();
    }

    public UserStats getUserStats(UUID uuid) {
        // Return a basic stats object
        return new UserStats(uuid, 0, 0, new Date());
    }

    public PresetData findPresetByName(UUID uuid, String presetName) {
        return null;
    }

    public void setActiveGradient(UUID uuid, PresetData preset) {
        // This is handled by equipPreset now
    }

    public Map<UUID, Map<String, PresetData>> getUserPresetsMap() {
        return new HashMap<>();
    }

    public Map<UUID, PresetData> getActiveGradientsMap() {
        return new HashMap<>();
    }

    public Map<UUID, PresetData> getAdminForcedGradientsMap() {
        return new HashMap<>();
    }

    public void cleanupUserData(UUID uuid) {
        // No user-specific cleanup needed in new system
    }

    public boolean saveGlobalPreset(UUID uuid, GlobalPresetData globalPreset) {
        return false; // Not supported in simplified system
    }

    public boolean updateGlobalPreset(UUID uuid, GlobalPresetData globalPreset) {
        return false; // Not supported in simplified system
    }

    public boolean setPresetPublished(String presetName, UUID owner, boolean published) {
        return false; // Not supported in simplified system
    }

    public boolean setPresetGlobal(String presetName, UUID owner, boolean global) {
        return false; // Not supported in simplified system
    }

    // === USER STATS CLASS (Legacy compatibility) ===
    
    public static class UserStats {
        private final UUID uuid;
        private final int presetCount;
        private final int activeGradients;
        private final Date lastActive;

        public UserStats(UUID uuid, int presetCount, int activeGradients, Date lastActive) {
            this.uuid = uuid;
            this.presetCount = presetCount;
            this.activeGradients = activeGradients;
            this.lastActive = lastActive;
        }

        public UUID getUuid() { return uuid; }
        public int getPresetCount() { return presetCount; }
        public int getActiveGradients() { return activeGradients; }
        public Date getLastActive() { return lastActive; }
        
        // Additional methods for GUI compatibility
        public boolean hasActiveGradient() { return activeGradients > 0; }
        public boolean hasAdminForced() { 
            // Check if user has admin forced gradient - would need UserDataManager access
            return false; // For now, simplified
        }
        public int getMaxPresets() { 
            // In global preset system, users don't have preset limits
            return Integer.MAX_VALUE; 
        }
    }

    // === USER DASHBOARD CLASS (Legacy compatibility) ===
    
    public static class UserDashboard {
        private final String equippedPreset;
        private final int availablePresets;
        private final boolean hasAdminForced;

        public UserDashboard(String equippedPreset, int availablePresets, boolean hasAdminForced) {
            this.equippedPreset = equippedPreset;
            this.availablePresets = availablePresets;
            this.hasAdminForced = hasAdminForced;
        }

        public String getEquippedPreset() { return equippedPreset; }
        public int getAvailablePresets() { return availablePresets; }
        public boolean hasAdminForced() { return hasAdminForced; }
        
        // Legacy methods for MainMenuGUI compatibility
        public UserStats getStats() {
            return new UserStats(null, availablePresets, hasAdminForced ? 1 : 0, new Date());
        }
        
        public List<PresetData> getPresets() {
            // Return empty list - presets are now managed globally
            return new ArrayList<>();
        }
        
        public PresetData getActiveGradient() {
            // Return null - active gradients are managed differently now
            return null;
        }
    }

    // === CREATE PRESET RESULT CLASS (Legacy compatibility) ===
    
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

        public static CreatePresetResult success(PresetData preset) {
            return new CreatePresetResult(true, "Preset created successfully", preset);
        }

        public static CreatePresetResult error(String message) {
            return new CreatePresetResult(false, message, null);
        }
    }

    // === ADDITIONAL LEGACY METHODS ===

    public UserDashboard getUserDashboard(Player player) {
        String equippedId = userDataManager.getEquippedPreset(player.getUniqueId());
        String equippedName = "None";
        
        if (equippedId != null) {
            GlobalPresetData preset = globalPresetManager.getPreset(equippedId);
            if (preset != null) {
                equippedName = preset.getName();
            }
        }
        
        int availableCount = globalPresetManager.getAvailablePresets(player).size();
        boolean hasForced = userDataManager.hasAdminForcedGradient(player.getUniqueId());
        
        return new UserDashboard(equippedName, availableCount, hasForced);
    }

    public CreatePresetResult createPreset(Player player, String presetName, List<String> colors) {
        // In the new system, users cannot create presets
        return CreatePresetResult.error("Users cannot create presets. Only admins can create global presets.");
    }

    public boolean updatePresetFromNamedColors(Player player, String presetName, List<NamedTextColor> namedColors) {
        // In the new system, users cannot update presets
        return false;
    }

    public boolean applyPreset(Player player, String presetName) {
        // Find preset by name - need to iterate through all presets
        String presetId = null;
        GlobalPresetData presetData = null;
        
        for (Map.Entry<String, GlobalPresetData> entry : globalPresetManager.getAllPresets().entrySet()) {
            if (entry.getValue().getName().equals(presetName)) {
                presetId = entry.getKey();
                presetData = entry.getValue();
                break;
            }
        }
        
        if (presetData == null) {
            return false;
        }
        
        // Check permission
        if (!player.hasPermission("betterchatcolours.preset." + presetId) && 
            !player.hasPermission("betterchatcolours.preset.*")) {
            return false;
        }
        
        // Equip the preset
        userDataManager.equipPreset(player.getUniqueId(), presetId);
        return true;
    }

    // === ADMIN-ONLY GLOBAL PRESET METHODS ===
    
    public boolean saveGlobalPreset(Player player, GlobalPresetData preset) {
        // In the new system, only admins can create global presets
        if (!player.hasPermission("betterchatcolours.admin")) {
            return false;
        }
        
        // Generate a unique preset ID from the name
        String presetId = preset.getName().toLowerCase().replace(" ", "_");
        
        return globalPresetManager.createPreset(
            presetId,
            preset.getName(), 
            preset.getColors(), 
            preset.getPermission()
        );
    }

    public boolean updateGlobalPreset(Player player, GlobalPresetData preset) {
        // In the new system, only admins can update global presets
        if (!player.hasPermission("betterchatcolours.admin")) {
            return false;
        }
        
        // For updating, we'd need to add an update method to GlobalPresetManager
        // For now, return false as updates aren't implemented
        return false;
    }
}
