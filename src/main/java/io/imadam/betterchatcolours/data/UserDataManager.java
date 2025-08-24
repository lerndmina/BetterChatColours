package io.imadam.betterchatcolours.data;

import io.imadam.betterchatcolours.BetterChatColours;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class UserDataManager {
  private final BetterChatColours plugin;
  private final Map<UUID, String> userEquippedPresets;
  private final File userDataFile;

  public UserDataManager(BetterChatColours plugin) {
    this.plugin = plugin;
    this.userEquippedPresets = new HashMap<>();
    this.userDataFile = new File(plugin.getDataFolder(), "userdata.yml");
  }

  public void loadUserData() {
    userEquippedPresets.clear();

    if (!userDataFile.exists()) {
      return;
    }

    try {
      FileConfiguration config = YamlConfiguration.loadConfiguration(userDataFile);

      if (config.getConfigurationSection("users") == null) {
        return;
      }

      for (String uuidString : config.getConfigurationSection("users").getKeys(false)) {
        try {
          UUID uuid = UUID.fromString(uuidString);
          String preset = config.getString("users." + uuidString + ".equipped");

          if (preset != null && !preset.isEmpty()) {
            userEquippedPresets.put(uuid, preset);
          }
        } catch (IllegalArgumentException e) {
          plugin.getLogger().warning("Invalid UUID in userdata.yml: " + uuidString);
        }
      }

      plugin.getLogger().info("Loaded user data for " + userEquippedPresets.size() + " players");

    } catch (Exception e) {
      plugin.getLogger().log(Level.SEVERE, "Error loading user data", e);
    }
  }

  public void saveUserData() {
    try {
      FileConfiguration config = new YamlConfiguration();

      for (Map.Entry<UUID, String> entry : userEquippedPresets.entrySet()) {
        config.set("users." + entry.getKey().toString() + ".equipped", entry.getValue());
      }

      config.save(userDataFile);

    } catch (IOException e) {
      plugin.getLogger().log(Level.SEVERE, "Error saving user data", e);
    }
  }

  public void setEquippedPreset(Player player, String presetName) {
    setEquippedPreset(player.getUniqueId(), presetName);
  }

  public void setEquippedPreset(UUID playerUuid, String presetName) {
    if (presetName == null || presetName.isEmpty()) {
      userEquippedPresets.remove(playerUuid);
    } else {
      userEquippedPresets.put(playerUuid, presetName);
    }
    saveUserData();
  }

  public String getEquippedPreset(Player player) {
    return getEquippedPreset(player.getUniqueId());
  }

  public String getEquippedPreset(UUID playerUuid) {
    return userEquippedPresets.get(playerUuid);
  }

  public boolean hasEquippedPreset(Player player) {
    return hasEquippedPreset(player.getUniqueId());
  }

  public boolean hasEquippedPreset(UUID playerUuid) {
    return userEquippedPresets.containsKey(playerUuid) &&
        userEquippedPresets.get(playerUuid) != null &&
        !userEquippedPresets.get(playerUuid).isEmpty();
  }

  public void clearEquippedPreset(Player player) {
    clearEquippedPreset(player.getUniqueId());
  }

  public void clearEquippedPreset(UUID playerUuid) {
    userEquippedPresets.remove(playerUuid);
    saveUserData();
  }
}
