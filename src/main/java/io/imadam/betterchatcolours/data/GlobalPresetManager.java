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
    // Standard Minecraft Colors (single colors)
    presets.put("black", new GlobalPresetData("black", List.of("#000000"), "chatcolor.preset.black"));
    presets.put("dark_blue", new GlobalPresetData("dark_blue", List.of("#0000AA"), "chatcolor.preset.dark_blue"));
    presets.put("dark_green", new GlobalPresetData("dark_green", List.of("#00AA00"), "chatcolor.preset.dark_green"));
    presets.put("dark_aqua", new GlobalPresetData("dark_aqua", List.of("#00AAAA"), "chatcolor.preset.dark_aqua"));
    presets.put("dark_red", new GlobalPresetData("dark_red", List.of("#AA0000"), "chatcolor.preset.dark_red"));
    presets.put("dark_purple", new GlobalPresetData("dark_purple", List.of("#AA00AA"), "chatcolor.preset.dark_purple"));
    presets.put("gold", new GlobalPresetData("gold", List.of("#FFAA00"), "chatcolor.preset.gold"));
    presets.put("gray", new GlobalPresetData("gray", List.of("#AAAAAA"), "chatcolor.preset.gray"));
    presets.put("dark_gray", new GlobalPresetData("dark_gray", List.of("#555555"), "chatcolor.preset.dark_gray"));
    presets.put("blue", new GlobalPresetData("blue", List.of("#5555FF"), "chatcolor.preset.blue"));
    presets.put("green", new GlobalPresetData("green", List.of("#55FF55"), "chatcolor.preset.green"));
    presets.put("aqua", new GlobalPresetData("aqua", List.of("#55FFFF"), "chatcolor.preset.aqua"));
    presets.put("red", new GlobalPresetData("red", List.of("#FF5555"), "chatcolor.preset.red"));
    presets.put("light_purple", new GlobalPresetData("light_purple", List.of("#FF55FF"), "chatcolor.preset.light_purple"));
    presets.put("yellow", new GlobalPresetData("yellow", List.of("#FFFF55"), "chatcolor.preset.yellow"));
    presets.put("white", new GlobalPresetData("white", List.of("#FFFFFF"), "chatcolor.preset.white"));

    // Premium Gradient Presets
    List<String> rainbowColors = List.of("#ff0000", "#ff8000", "#ffff00", "#80ff00", "#00ff00", "#00ff80", "#00ffff",
        "#0080ff", "#0000ff", "#8000ff", "#ff00ff", "#ff0080");
    presets.put("rainbow", new GlobalPresetData("rainbow", rainbowColors, "chatcolor.preset.rainbow"));

    List<String> fireColors = List.of("#ff4500", "#ff6347", "#ffd700");
    presets.put("fire", new GlobalPresetData("fire", fireColors, "chatcolor.preset.fire"));

    List<String> oceanColors = List.of("#006994", "#0080ff", "#00bfff");
    presets.put("ocean", new GlobalPresetData("ocean", oceanColors, "chatcolor.preset.ocean"));

    List<String> forestColors = List.of("#228b22", "#32cd32", "#90ee90");
    presets.put("forest", new GlobalPresetData("forest", forestColors, "chatcolor.preset.forest"));

    // Additional Premium Gradients
    List<String> sunsetColors = List.of("#ff4500", "#ff6347", "#ff7f50", "#ffd700", "#ffb347");
    presets.put("sunset", new GlobalPresetData("sunset", sunsetColors, "chatcolor.preset.sunset"));

    List<String> crystalColors = List.of("#e6e6fa", "#dda0dd", "#da70d6", "#ba55d3", "#9370db");
    presets.put("crystal", new GlobalPresetData("crystal", crystalColors, "chatcolor.preset.crystal"));

    List<String> lavaColors = List.of("#b22222", "#dc143c", "#ff4500", "#ff6347", "#ffd700");
    presets.put("lava", new GlobalPresetData("lava", lavaColors, "chatcolor.preset.lava"));

    List<String> iceColors = List.of("#f0f8ff", "#e0ffff", "#b0e0e6", "#87ceeb", "#87cefa");
    presets.put("ice", new GlobalPresetData("ice", iceColors, "chatcolor.preset.ice"));

    List<String> shadowColors = List.of("#2f4f4f", "#696969", "#708090", "#778899", "#b0c4de");
    presets.put("shadow", new GlobalPresetData("shadow", shadowColors, "chatcolor.preset.shadow"));

    List<String> emeraldColors = List.of("#00ff00", "#32cd32", "#90ee90", "#98fb98", "#f0fff0");
    presets.put("emerald", new GlobalPresetData("emerald", emeraldColors, "chatcolor.preset.emerald"));

    List<String> rubyColors = List.of("#8b0000", "#dc143c", "#ff1493", "#ff69b4", "#ffb6c1");
    presets.put("ruby", new GlobalPresetData("ruby", rubyColors, "chatcolor.preset.ruby"));

    List<String> sapphireColors = List.of("#000080", "#0000cd", "#4169e1", "#6495ed", "#87ceeb");
    presets.put("sapphire", new GlobalPresetData("sapphire", sapphireColors, "chatcolor.preset.sapphire"));

    plugin.getLogger().info("Created " + presets.size() + " default presets (16 standard colors + 12 premium gradients)");
  }

  public void addPreset(String name, List<String> colors, String permission) {
    presets.put(name, new GlobalPresetData(name, colors, permission));
    savePresets();
  }

  public void addPreset(String name, List<String> colors) {
    // Automatically generate permission as chatcolor.preset.{name}
    String permission = "chatcolor.preset." + name.toLowerCase();
    addPreset(name, colors, permission);
  }

  public String generateUniquePresetName(String baseName) {
    String name = baseName;
    int counter = 1;
    
    while (presetExists(name)) {
      name = baseName + "_" + counter;
      counter++;
    }
    
    return name;
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
