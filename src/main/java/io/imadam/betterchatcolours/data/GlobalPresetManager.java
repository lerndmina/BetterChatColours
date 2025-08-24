package io.imadam.betterchatcolours.data;

import io.imadam.betterchatcolours.BetterChatColours;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class GlobalPresetManager {
  private final BetterChatColours plugin;
  private final Map<String, GlobalPresetData> presets;
  private final File presetsFile;

  public GlobalPresetManager(BetterChatColours plugin) {
    this.plugin = plugin;
    this.presets = new HashMap<>();
    this.presetsFile = new File(plugin.getDataFolder(), "presets.yml");
  }

  public void loadPresets() {
    presets.clear();

    if (!presetsFile.exists()) {
      createDefaultPresets();
      savePresets();
      return;
    }

    try {
      FileConfiguration config = YamlConfiguration.loadConfiguration(presetsFile);

      if (config.getConfigurationSection("presets") == null) {
        createDefaultPresets();
        savePresets();
        return;
      }

      for (String presetName : config.getConfigurationSection("presets").getKeys(false)) {
        String path = "presets." + presetName;
        List<String> colors = config.getStringList(path + ".colors");
        String permission = config.getString(path + ".permission", "");

        if (!colors.isEmpty()) {
          presets.put(presetName, new GlobalPresetData(presetName, colors, permission));
        }
      }

      plugin.getLogger().info("Loaded " + presets.size() + " global presets");

    } catch (Exception e) {
      plugin.getLogger().log(Level.SEVERE, "Error loading presets", e);
      createDefaultPresets();
      savePresets();
    }
  }

  public void savePresets() {
    try {
      FileConfiguration config = new YamlConfiguration();

      for (GlobalPresetData preset : presets.values()) {
        String path = "presets." + preset.getName();
        config.set(path + ".colors", preset.getColors());
        config.set(path + ".permission", preset.getPermission());
      }

      config.save(presetsFile);

    } catch (IOException e) {
      plugin.getLogger().log(Level.SEVERE, "Error saving presets", e);
    }
  }

  private void createDefaultPresets() {
    // Create some default presets
    List<String> rainbowColors = List.of("#ff0000", "#ff8000", "#ffff00", "#80ff00", "#00ff00", "#00ff80", "#00ffff",
        "#0080ff", "#0000ff", "#8000ff", "#ff00ff", "#ff0080");
    presets.put("rainbow", new GlobalPresetData("rainbow", rainbowColors, ""));

    List<String> fireColors = List.of("#ff4500", "#ff6347", "#ffd700");
    presets.put("fire", new GlobalPresetData("fire", fireColors, ""));

    List<String> oceanColors = List.of("#006994", "#0080ff", "#00bfff");
    presets.put("ocean", new GlobalPresetData("ocean", oceanColors, ""));

    List<String> forestColors = List.of("#228b22", "#32cd32", "#90ee90");
    presets.put("forest", new GlobalPresetData("forest", forestColors, ""));

    plugin.getLogger().info("Created default presets");
  }

  public void addPreset(String name, List<String> colors, String permission) {
    presets.put(name, new GlobalPresetData(name, colors, permission));
    savePresets();
  }

  public void removePreset(String name) {
    presets.remove(name);
    savePresets();
  }

  public GlobalPresetData getPreset(String name) {
    return presets.get(name);
  }

  public Map<String, GlobalPresetData> getAllPresets() {
    return new HashMap<>(presets);
  }

  public List<GlobalPresetData> getAvailablePresets(Player player) {
    List<GlobalPresetData> available = new ArrayList<>();

    for (GlobalPresetData preset : presets.values()) {
      String permission = preset.getPermission();
      if (permission.isEmpty() || player.hasPermission(permission)) {
        available.add(preset);
      }
    }

    return available;
  }

  public boolean presetExists(String name) {
    return presets.containsKey(name);
  }
}
