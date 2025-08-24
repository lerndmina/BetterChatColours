package io.imadam.betterchatcolours.data;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.utils.ColorUtils;
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

/**
 * Manages user data for equipped presets and admin forced gradients.
 * Users can no longer create presets - they can only equip global presets.
 */
public class UserDataManager {

    private final BetterChatColours plugin;
    private final Map<UUID, String> equippedPresets = new ConcurrentHashMap<>(); // UUID -> PresetID
    private final Map<UUID, PresetData> adminForcedGradients = new ConcurrentHashMap<>();
    private File dataFolder;

    public UserDataManager(BetterChatColours plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "userdata");

        // Load data asynchronously to avoid blocking server startup
        new BukkitRunnable() {
            @Override
            public void run() {
                loadAllData();
                plugin.getLogger().info("User data loaded asynchronously");
            }
        }.runTaskAsynchronously(plugin);
    }

    public void reload() {
        equippedPresets.clear();
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

        plugin.getLogger().info("Loaded data for " + equippedPresets.size() + " users");
    }

    private void loadUserData(UUID uuid) {
        File userFile = new File(dataFolder, uuid.toString() + ".yml");
        if (!userFile.exists())
            return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(userFile);

        // Load equipped preset
        if (config.contains("equipped-preset")) {
            String presetId = config.getString("equipped-preset");
            if (presetId != null) {
                equippedPresets.put(uuid, presetId);
            }
        }

        // Load admin forced gradient
        if (config.contains("admin-forced")) {
            List<String> colors = config.getStringList("admin-forced.colors");
            if (!colors.isEmpty()) {
                String forcedName = config.getString("admin-forced.name", "Admin Forced");
                long forcedCreatedTime = config.getLong("admin-forced.created", System.currentTimeMillis());
                PresetData forcedGradient = new PresetData(forcedName, new ArrayList<>(colors), forcedCreatedTime);
                adminForcedGradients.put(uuid, forcedGradient);
            }
        }
    }

    public void saveUserData(UUID uuid) {
        File userFile = new File(dataFolder, uuid.toString() + ".yml");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        FileConfiguration config = new YamlConfiguration();

        // Save equipped preset
        String equippedPreset = equippedPresets.get(uuid);
        if (equippedPreset != null) {
            config.set("equipped-preset", equippedPreset);
        }

        // Save admin forced gradient
        PresetData forcedGradient = adminForcedGradients.get(uuid);
        if (forcedGradient != null) {
            config.set("admin-forced.name", forcedGradient.getName());
            config.set("admin-forced.colors", forcedGradient.getColors());
            config.set("admin-forced.created", forcedGradient.getCreatedTime());
        }

        try {
            config.save(userFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save user data for " + uuid, e);
        }
    }

    public void saveAllData() {
        Set<UUID> allUsers = new HashSet<>();
        allUsers.addAll(equippedPresets.keySet());
        allUsers.addAll(adminForcedGradients.keySet());

        for (UUID uuid : allUsers) {
            saveUserData(uuid);
        }
    }

    // === EQUIPPED PRESET METHODS ===

    public void equipPreset(UUID uuid, String presetId) {
        if (presetId == null) {
            equippedPresets.remove(uuid);
        } else {
            equippedPresets.put(uuid, presetId);
        }
        saveUserData(uuid);
    }

    public String getEquippedPreset(UUID uuid) {
        return equippedPresets.get(uuid);
    }

    public PresetData getActiveGradient(UUID uuid) {
        // Check for admin forced gradient first
        PresetData adminForced = adminForcedGradients.get(uuid);
        if (adminForced != null) {
            return adminForced;
        }

        // Get equipped preset from global presets
        String presetId = equippedPresets.get(uuid);
        if (presetId != null) {
            GlobalPresetData globalPreset = plugin.getGlobalPresetManager().getPreset(presetId);
            if (globalPreset != null) {
                // Convert GlobalPresetData to PresetData for compatibility
                return new PresetData(globalPreset.getName(), globalPreset.getColors(), globalPreset.getCreatedTime());
            }
        }

        return null;
    }

    public boolean hasEquippedPreset(UUID uuid) {
        return equippedPresets.containsKey(uuid);
    }

    public void clearEquippedPreset(UUID uuid) {
        equippedPresets.remove(uuid);
        saveUserData(uuid);
    }

    // === ADMIN FORCED GRADIENT METHODS ===

    public void setAdminForcedGradient(UUID uuid, PresetData gradient) {
        if (gradient == null) {
            adminForcedGradients.remove(uuid);
        } else {
            adminForcedGradients.put(uuid, gradient);
        }
        saveUserData(uuid);
    }

    public PresetData getAdminForcedGradient(UUID uuid) {
        return adminForcedGradients.get(uuid);
    }

    public boolean hasAdminForcedGradient(UUID uuid) {
        return adminForcedGradients.containsKey(uuid);
    }

    public void clearAdminForcedGradient(UUID uuid) {
        adminForcedGradients.remove(uuid);
        saveUserData(uuid);
    }

    // === UTILITY METHODS ===

    public boolean hasGradient(UUID uuid) {
        return hasAdminForcedGradient(uuid) || hasEquippedPreset(uuid);
    }

    public String processMessage(UUID uuid, String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        PresetData activeGradient = getActiveGradient(uuid);
        if (activeGradient == null) {
            return message;
        }

        return ColorUtils.createGradientPreview(activeGradient.getColors(), message);
    }

    public boolean canEquipPreset(UUID uuid, String presetId) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return false;
        }

        return plugin.getGlobalPresetManager().canPlayerUsePreset(player, presetId);
    }
}
