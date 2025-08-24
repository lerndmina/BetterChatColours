package io.imadam.betterchatcolours.data;

import io.imadam.betterchatcolours.BetterChatColours;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    // === CLEANUP OPERATIONS ===

    public void performMaintenanceCleanup() {
        // Basic cleanup - most maintenance is now handled by individual managers
        plugin.getLogger().info("Performing maintenance cleanup...");
        
        // Save all data
        userDataManager.saveAllData();
        globalPresetManager.saveGlobalPresets();
        
        plugin.getLogger().info("Maintenance cleanup completed");
    }

    // === ADDITIONAL METHODS FOR GUI COMPATIBILITY ===

    public boolean createPreset(Player player, String presetName, List<String> colors) {
        // In the global preset system, only admins can create global presets
        return false;
    }
}
