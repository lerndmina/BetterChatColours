package com.wilddev.betterchatcolours.data;

import com.wilddev.betterchatcolours.BetterChatColours;
import com.wilddev.betterchatcolours.utils.ColorUtils;
import com.wilddev.betterchatcolours.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class UserDataManager {

    private final BetterChatColours plugin;
    private final Map<UUID, Map<String, PresetData>> userPresets = new ConcurrentHashMap<>();
    private final Map<UUID, PresetData> activeGradients = new ConcurrentHashMap<>();
    private final Map<UUID, PresetData> adminForcedGradients = new ConcurrentHashMap<>();
    private File dataFolder;

    public UserDataManager(BetterChatColours plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "userdata");
        loadAllData();
    }

    public void reload() {
        userPresets.clear();
        activeGradients.clear();
        adminForcedGradients.clear();
        loadAllData();
    }

    private void loadAllData() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
            return;
        }

        File[] userFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (userFiles == null)
            return;

        for (File userFile : userFiles) {
            String fileName = userFile.getName();
            String uuidString = fileName.substring(0, fileName.length() - 4); // Remove .yml

            try {
                UUID uuid = UUID.fromString(uuidString);
                loadUserData(uuid);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in filename: " + fileName);
            }
        }

        plugin.getLogger().info("Loaded data for " + userPresets.size() + " users");
    }

    private void loadUserData(UUID uuid) {
        File userFile = new File(dataFolder, uuid.toString() + ".yml");
        if (!userFile.exists())
            return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(userFile);

        // Load presets
        Map<String, PresetData> presets = new HashMap<>();
        if (config.contains("presets")) {
            for (String presetName : config.getConfigurationSection("presets").getKeys(false)) {
                List<String> colors = config.getStringList("presets." + presetName + ".colors");
                long createdTime = config.getLong("presets." + presetName + ".created", System.currentTimeMillis());

                PresetData preset = new PresetData(presetName, new ArrayList<>(colors), createdTime);
                presets.put(presetName, preset);
            }
        }
        userPresets.put(uuid, presets);

        // Load active gradient
        if (config.contains("active-gradient")) {
            List<String> colors = config.getStringList("active-gradient.colors");
            if (!colors.isEmpty()) {
                PresetData activeGradient = new PresetData("active", new ArrayList<>(colors));
                activeGradients.put(uuid, activeGradient);
            }
        }

        // Load admin forced gradient
        if (config.contains("admin-forced")) {
            List<String> colors = config.getStringList("admin-forced.colors");
            if (!colors.isEmpty()) {
                PresetData forcedGradient = new PresetData("forced", new ArrayList<>(colors));
                adminForcedGradients.put(uuid, forcedGradient);
            }
        }
    }

    public void saveUserData(UUID uuid) {
        File userFile = new File(dataFolder, uuid.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        // Save presets
        Map<String, PresetData> presets = userPresets.get(uuid);
        if (presets != null && !presets.isEmpty()) {
            for (PresetData preset : presets.values()) {
                config.set("presets." + preset.getName() + ".colors", preset.getColors());
                config.set("presets." + preset.getName() + ".created", preset.getCreatedTime());
            }
        }

        // Save active gradient
        PresetData activeGradient = activeGradients.get(uuid);
        if (activeGradient != null) {
            config.set("active-gradient.colors", activeGradient.getColors());
        }

        // Save admin forced gradient
        PresetData forcedGradient = adminForcedGradients.get(uuid);
        if (forcedGradient != null) {
            config.set("admin-forced.colors", forcedGradient.getColors());
        }

        try {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            config.save(userFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save user data for " + uuid, e);
        }
    }

    public void saveAllData() {
        Set<UUID> allUsers = new HashSet<>();
        allUsers.addAll(userPresets.keySet());
        allUsers.addAll(activeGradients.keySet());
        allUsers.addAll(adminForcedGradients.keySet());

        for (UUID uuid : allUsers) {
            saveUserData(uuid);
        }

        plugin.getLogger().info("Saved data for " + allUsers.size() + " users");
    }

    // Preset management
    public boolean addPreset(UUID uuid, PresetData preset) {
        Map<String, PresetData> presets = userPresets.computeIfAbsent(uuid, k -> new HashMap<>());

        if (presets.containsKey(preset.getName())) {
            return false; // Preset name already exists
        }

        presets.put(preset.getName(), preset);
        saveUserData(uuid);
        return true;
    }

    public boolean removePreset(UUID uuid, String presetName) {
        Map<String, PresetData> presets = userPresets.get(uuid);
        if (presets == null)
            return false;

        boolean removed = presets.remove(presetName) != null;
        if (removed) {
            saveUserData(uuid);
        }
        return removed;
    }

    public PresetData getPreset(UUID uuid, String presetName) {
        Map<String, PresetData> presets = userPresets.get(uuid);
        return presets != null ? presets.get(presetName) : null;
    }

    public Map<String, PresetData> getUserPresets(UUID uuid) {
        return new HashMap<>(userPresets.getOrDefault(uuid, new HashMap<>()));
    }

    public int getPresetCount(UUID uuid) {
        Map<String, PresetData> presets = userPresets.get(uuid);
        return presets != null ? presets.size() : 0;
    }

    // Active gradient management
    public void setActiveGradient(UUID uuid, PresetData gradient) {
        if (gradient == null) {
            activeGradients.remove(uuid);
        } else {
            activeGradients.put(uuid, gradient);
        }
        saveUserData(uuid);
    }

    public PresetData getActiveGradient(UUID uuid) {
        // Admin forced gradients override user selections
        PresetData forced = adminForcedGradients.get(uuid);
        if (forced != null) {
            return forced;
        }

        return activeGradients.get(uuid);
    }

    // Admin forced gradient management
    public void setAdminForcedGradient(UUID uuid, PresetData gradient) {
        if (gradient == null) {
            adminForcedGradients.remove(uuid);
        } else {
            adminForcedGradients.put(uuid, gradient);
        }
        saveUserData(uuid);
    }

    public boolean hasAdminForcedGradient(UUID uuid) {
        return adminForcedGradients.containsKey(uuid);
    }

    public void clearAdminForcedGradient(UUID uuid) {
        adminForcedGradients.remove(uuid);
        saveUserData(uuid);
    }
    
    // Phase 2 Enhancements: Advanced data management
    
    /**
     * Validate a preset before saving
     */
    public ValidationResult validatePreset(UUID uuid, PresetData preset) {
        if (preset == null) {
            return new ValidationResult(false, "Preset cannot be null");
        }
        
        if (preset.getName() == null || preset.getName().trim().isEmpty()) {
            return new ValidationResult(false, "Preset name cannot be empty");
        }
        
        if (!isValidPresetName(preset.getName())) {
            return new ValidationResult(false, "Invalid preset name. Use only letters, numbers, and spaces (1-16 characters)");
        }
        
        if (preset.getColors() == null || preset.getColors().isEmpty()) {
            return new ValidationResult(false, "Preset must have at least one color");
        }
        
        if (preset.getColors().size() > plugin.getConfigManager().getMaxColorsPerGradient()) {
            return new ValidationResult(false, "Too many colors. Maximum: " + plugin.getConfigManager().getMaxColorsPerGradient());
        }
        
        // Validate all colors
        for (String color : preset.getColors()) {
            if (!ColorUtils.isValidHex(color)) {
                return new ValidationResult(false, "Invalid color: " + color);
            }
        }
        
        // Check preset count limits
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            int maxPresets = PermissionUtils.getMaxPresetCount(player, plugin.getConfigManager().getMaxPresetsPerUser());
            int currentCount = getPresetCount(uuid);
            
            Map<String, PresetData> existingPresets = userPresets.get(uuid);
            boolean isUpdate = existingPresets != null && existingPresets.containsKey(preset.getName());
            
            if (!isUpdate && currentCount >= maxPresets) {
                return new ValidationResult(false, "Maximum preset limit reached: " + maxPresets);
            }
        }
        
        return new ValidationResult(true, "Valid preset");
    }
    
    /**
     * Async save operation to prevent blocking main thread
     */
    public void saveUserDataAsync(UUID uuid) {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveUserData(uuid);
            }
        }.runTaskAsynchronously(plugin);
    }
    
    /**
     * Validate preset name format
     */
    private boolean isValidPresetName(String name) {
        if (name == null || name.length() < 1 || name.length() > 16) {
            return false;
        }
        return Pattern.matches("^[a-zA-Z0-9 ]+$", name);
    }
    
    /**
     * Get user statistics
     */
    public UserStats getUserStats(UUID uuid) {
        Map<String, PresetData> presets = userPresets.get(uuid);
        PresetData activeGradient = getActiveGradient(uuid);
        boolean hasAdminForced = hasAdminForcedGradient(uuid);
        
        int presetCount = presets != null ? presets.size() : 0;
        int maxPresets = plugin.getConfigManager().getMaxPresetsPerUser();
        
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            maxPresets = PermissionUtils.getMaxPresetCount(player, maxPresets);
        }
        
        return new UserStats(uuid, presetCount, maxPresets, activeGradient != null, hasAdminForced);
    }
    
    /**
     * Clean up old or invalid data
     */
    public void cleanupUserData(UUID uuid) {
        Map<String, PresetData> presets = userPresets.get(uuid);
        if (presets == null) return;
        
        boolean changed = false;
        Iterator<Map.Entry<String, PresetData>> iterator = presets.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, PresetData> entry = iterator.next();
            PresetData preset = entry.getValue();
            
            // Remove presets with invalid colors
            boolean hasInvalidColors = preset.getColors().stream()
                    .anyMatch(color -> !ColorUtils.isValidHex(color));
            
            if (hasInvalidColors) {
                iterator.remove();
                changed = true;
                plugin.getLogger().info("Removed invalid preset '" + preset.getName() + "' for user " + uuid);
            }
        }
        
        if (changed) {
            saveUserDataAsync(uuid);
        }
    }
    
    /**
     * Get preset by name with fuzzy matching
     */
    public PresetData findPresetByName(UUID uuid, String searchName) {
        Map<String, PresetData> presets = userPresets.get(uuid);
        if (presets == null || searchName == null) return null;
        
        // Exact match first
        PresetData exact = presets.get(searchName);
        if (exact != null) return exact;
        
        // Case-insensitive match
        for (Map.Entry<String, PresetData> entry : presets.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(searchName)) {
                return entry.getValue();
            }
        }
        
        // Partial match
        String lowerSearch = searchName.toLowerCase();
        for (Map.Entry<String, PresetData> entry : presets.entrySet()) {
            if (entry.getKey().toLowerCase().contains(lowerSearch)) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * Get all presets sorted by creation time
     */
    public List<PresetData> getPresetsSorted(UUID uuid, boolean newestFirst) {
        Map<String, PresetData> presets = userPresets.get(uuid);
        if (presets == null) return new ArrayList<>();
        
        List<PresetData> sorted = new ArrayList<>(presets.values());
        sorted.sort((a, b) -> {
            long comparison = newestFirst ? 
                    Long.compare(b.getCreatedTime(), a.getCreatedTime()) :
                    Long.compare(a.getCreatedTime(), b.getCreatedTime());
            return (int) comparison;
        });
        
        return sorted;
    }
    
    /**
     * Bulk operations for admin management
     */
    public void clearAllUserData(UUID uuid) {
        userPresets.remove(uuid);
        activeGradients.remove(uuid);
        adminForcedGradients.remove(uuid);
        
        File userFile = new File(dataFolder, uuid.toString() + ".yml");
        if (userFile.exists()) {
            userFile.delete();
        }
    }
    
    /**
     * Export user data for backup/transfer
     */
    public Map<String, Object> exportUserData(UUID uuid) {
        Map<String, Object> export = new HashMap<>();
        
        Map<String, PresetData> presets = userPresets.get(uuid);
        if (presets != null) {
            export.put("presets", presets);
        }
        
        PresetData active = activeGradients.get(uuid);
        if (active != null) {
            export.put("active", active);
        }
        
        PresetData forced = adminForcedGradients.get(uuid);
        if (forced != null) {
            export.put("adminForced", forced);
        }
        
        return export;
    }
    
    // Package-private getters for DataManager access
    Map<UUID, Map<String, PresetData>> getUserPresetsMap() {
        return userPresets;
    }
    
    Map<UUID, PresetData> getActiveGradientsMap() {
        return activeGradients;
    }
    
    Map<UUID, PresetData> getAdminForcedGradientsMap() {
        return adminForcedGradients;
    }
}
