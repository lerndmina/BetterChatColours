package com.wilddev.betterchatcolours.data;

import com.wilddev.betterchatcolours.BetterChatColours;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

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
        if (userFiles == null) return;
        
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
        if (!userFile.exists()) return;
        
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
        if (presets == null) return false;
        
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
}
