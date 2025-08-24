package io.imadam.betterchatcolours.data;

import io.imadam.betterchatcolours.BetterChatColours;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class GlobalPresetManager {

  private final BetterChatColours plugin;
  private final Map<String, GlobalPresetData> globalPresets = new ConcurrentHashMap<>();
  private File presetsFile;

  public GlobalPresetManager(BetterChatColours plugin) {
    this.plugin = plugin;
    this.presetsFile = new File(plugin.getDataFolder(), "global-presets.yml");

    loadGlobalPresets();
    createDefaultPresets();
  }

  private void loadGlobalPresets() {
    if (!presetsFile.exists()) {
      return;
    }

    FileConfiguration config = YamlConfiguration.loadConfiguration(presetsFile);

    if (config.contains("presets")) {
      ConfigurationSection presetsSection = config.getConfigurationSection("presets");
      for (String presetId : presetsSection.getKeys(false)) {
        String name = config.getString("presets." + presetId + ".name", presetId);
        List<String> colors = config.getStringList("presets." + presetId + ".colors");
        String permission = config.getString("presets." + presetId + ".permission", "chatcolors.preset.use");
        long createdTime = config.getLong("presets." + presetId + ".created", System.currentTimeMillis());
        boolean isDefault = config.getBoolean("presets." + presetId + ".default", false);

        GlobalPresetData preset = new GlobalPresetData(name, colors, permission, createdTime, isDefault);
        globalPresets.put(presetId, preset);
      }
    }

    plugin.getLogger().info("Loaded " + globalPresets.size() + " global presets");
  }

  private void createDefaultPresets() {
    FileConfiguration config = plugin.getConfig();
    boolean hasNewDefaults = false;

    if (config.contains("colours")) {
      ConfigurationSection colorsSection = config.getConfigurationSection("colours");

      for (String colorKey : colorsSection.getKeys(false)) {
        String presetId = "default_" + colorKey;

        // Skip if default preset already exists
        if (globalPresets.containsKey(presetId)) {
          continue;
        }

        String hexColor = config.getString("colours." + colorKey + ".colour");
        String displayName = config.getString("colours." + colorKey + ".displayname", colorKey);
        String permission = config.getString("colours." + colorKey + ".permission", "chatcolors.preset.use");

        if (hexColor != null) {
          List<String> colors = Arrays.asList(hexColor);
          GlobalPresetData defaultPreset = new GlobalPresetData(
              displayName,
              colors,
              permission,
              System.currentTimeMillis(),
              true);

          globalPresets.put(presetId, defaultPreset);
          hasNewDefaults = true;
          plugin.getLogger().info("Created default preset: " + displayName + " (" + hexColor + ")");
        }
      }
    }

    if (hasNewDefaults) {
      saveGlobalPresets();
    }
  }

  public void saveGlobalPresets() {
    FileConfiguration config = new YamlConfiguration();

    for (Map.Entry<String, GlobalPresetData> entry : globalPresets.entrySet()) {
      String presetId = entry.getKey();
      GlobalPresetData preset = entry.getValue();

      config.set("presets." + presetId + ".name", preset.getName());
      config.set("presets." + presetId + ".colors", preset.getColors());
      config.set("presets." + presetId + ".permission", preset.getRequiredPermission());
      config.set("presets." + presetId + ".created", preset.getCreatedTime());
      config.set("presets." + presetId + ".default", preset.isDefault());
    }

    try {
      config.save(presetsFile);
    } catch (IOException e) {
      plugin.getLogger().log(Level.SEVERE, "Failed to save global presets", e);
    }
  }

  public boolean createPreset(String presetId, String name, List<String> colors, String permission) {
    if (globalPresets.containsKey(presetId)) {
      return false; // Preset already exists
    }

    GlobalPresetData preset = new GlobalPresetData(name, colors, permission, System.currentTimeMillis(), false);
    globalPresets.put(presetId, preset);
    saveGlobalPresets();

    plugin.getLogger().info("Created new global preset: " + name + " (ID: " + presetId + ")");
    return true;
  }

  public boolean deletePreset(String presetId) {
    GlobalPresetData preset = globalPresets.remove(presetId);
    if (preset != null) {
      saveGlobalPresets();
      plugin.getLogger().info("Deleted global preset: " + preset.getName() + " (ID: " + presetId + ")");
      return true;
    }
    return false;
  }

  public GlobalPresetData getPreset(String presetId) {
    return globalPresets.get(presetId);
  }

  public Map<String, GlobalPresetData> getAllPresets() {
    return new HashMap<>(globalPresets);
  }

  public List<String> getAvailablePresets(Player player) {
    List<String> available = new ArrayList<>();

    for (Map.Entry<String, GlobalPresetData> entry : globalPresets.entrySet()) {
      String presetId = entry.getKey();
      GlobalPresetData preset = entry.getValue();

      if (player.hasPermission(preset.getRequiredPermission())) {
        available.add(presetId);
      }
    }

    return available;
  }

  public Map<String, GlobalPresetData> getAvailablePresetsMap(Player player) {
    Map<String, GlobalPresetData> available = new HashMap<>();

    for (Map.Entry<String, GlobalPresetData> entry : globalPresets.entrySet()) {
      String presetId = entry.getKey();
      GlobalPresetData preset = entry.getValue();

      if (player.hasPermission(preset.getRequiredPermission())) {
        available.put(presetId, preset);
      }
    }

    return available;
  }

  public boolean hasPreset(String presetId) {
    return globalPresets.containsKey(presetId);
  }

  public boolean canPlayerUsePreset(Player player, String presetId) {
    GlobalPresetData preset = globalPresets.get(presetId);
    if (preset == null) {
      return false;
    }

    return player.hasPermission(preset.getRequiredPermission());
  }

  public void reload() {
    globalPresets.clear();
    loadGlobalPresets();
    createDefaultPresets();
  }

  public Set<String> getPresetIds() {
    return new HashSet<>(globalPresets.keySet());
  }

  public List<GlobalPresetData> getDefaultPresets() {
    return globalPresets.values().stream()
        .filter(GlobalPresetData::isDefault)
        .toList();
  }

  public List<GlobalPresetData> getCustomPresets() {
    return globalPresets.values().stream()
        .filter(preset -> !preset.isDefault())
        .toList();
  }
}
