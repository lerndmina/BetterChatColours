package io.imadam.betterchatcolours.data;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.config.ConfigManager;
import io.imadam.betterchatcolours.utils.ColorUtils;
import net.kyori.adventure.text.format.NamedTextColor;
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
    // Input validation
    if (player == null) {
      plugin.getLogger().warning("Null player provided to createPreset");
      return new CreatePresetResult(false, "Invalid player", null);
    }

    if (presetName == null || presetName.trim().isEmpty()) {
      plugin.getLogger().warning("Invalid preset name provided by player: " + player.getName());
      return new CreatePresetResult(false, "Preset name cannot be empty", null);
    }

    if (colors == null || colors.isEmpty()) {
      plugin.getLogger().warning("Invalid colors list provided by player: " + player.getName());
      return new CreatePresetResult(false, "At least one color is required", null);
    }

    if (colors.size() > configManager.getMaxColorsPerGradient()) {
      plugin.getLogger().warning("Too many colors (" + colors.size() + ") provided by player: " + player.getName());
      return new CreatePresetResult(false, "Too many colors! Maximum: " + configManager.getMaxColorsPerGradient(),
          null);
    }

    UUID uuid = player.getUniqueId();
    plugin.getLogger()
        .info("Creating preset '" + presetName + "' for player " + player.getName() + " with colors: " + colors);

    // Create preset data
    PresetData preset = new PresetData(presetName, new ArrayList<>(colors));

    // Validate preset
    ValidationResult validation = userDataManager.validatePreset(uuid, preset);
    plugin.getLogger().info("Validation result: " + validation.isValid() + " - " + validation.getMessage());
    if (!validation.isValid()) {
      return new CreatePresetResult(false, validation.getMessage(), null);
    }

    // Check player permissions for each color
    Map<String, ColorData> availableColors = configManager.getAvailableColorsForPlayer(player);
    plugin.getLogger().info("Available colors for player: " + availableColors.size());

    // Debug: List all available colors
    availableColors.forEach((name, colorData) -> {
      plugin.getLogger().info("Available color: " + name + " -> " + colorData.getHexCode());
    });

    for (String colorHex : colors) {
      boolean hasPermission = availableColors.values().stream()
          .anyMatch(colorData -> colorData.getHexCode().equalsIgnoreCase(colorHex));

      plugin.getLogger().info("Checking permission for color " + colorHex + ": " + hasPermission);

      // If player is OP, allow any color
      if (player.isOp()) {
        plugin.getLogger().info("Player is OP - allowing color " + colorHex);
        continue;
      }

      if (!hasPermission) {
        return new CreatePresetResult(false, "You don't have permission to use color: " + colorHex, null);
      }
    }

    // Add preset
    boolean success = userDataManager.addPreset(uuid, preset);
    plugin.getLogger().info("UserDataManager.addPreset result: " + success);
    if (!success) {
      return new CreatePresetResult(false, "A preset with that name already exists", null);
    }

    return new CreatePresetResult(true, "Preset created successfully", preset);
  }

  /**
   * Create a new preset with NamedTextColor list (convenience method for GUI)
   */
  public boolean createPresetFromNamedColors(Player player, String presetName, List<NamedTextColor> namedColors) {
    List<String> colorStrings = namedColors.stream()
        .map(NamedTextColor::asHexString)
        .collect(Collectors.toList());

    CreatePresetResult result = createPreset(player, presetName, colorStrings);
    return result.isSuccess();
  }

  /**
   * Update an existing preset with NamedTextColor list
   */
  public boolean updatePresetFromNamedColors(Player player, String presetName, List<NamedTextColor> namedColors) {
    List<String> colorStrings = namedColors.stream()
        .map(NamedTextColor::asHexString)
        .collect(Collectors.toList());

    UUID uuid = player.getUniqueId();
    PresetData newPreset = new PresetData(presetName, colorStrings);

    // Validate the preset
    ValidationResult validation = userDataManager.validatePreset(uuid, newPreset);
    if (!validation.isValid()) {
      return false;
    }

    // Check permissions for each color
    Map<String, ColorData> availableColors = configManager.getAvailableColorsForPlayer(player);
    for (String colorHex : colorStrings) {
      // If player is OP, allow any color
      if (player.isOp()) {
        continue;
      }

      boolean hasPermission = availableColors.values().stream()
          .anyMatch(colorData -> colorData.getHexCode().equalsIgnoreCase(colorHex));

      if (!hasPermission) {
        return false;
      }
    }

    // For now, just delete the old and create new (simple implementation)
    userDataManager.removePreset(uuid, presetName);
    return userDataManager.addPreset(uuid, newPreset);
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
            ColorUtils.getColorDisplayName(colorData)))
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
      // If player is OP, allow any color
      if (player.isOp()) {
        continue;
      }

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
        .filter(preset -> preset.getName().toLowerCase().contains(lowerQuery) ||
            preset.getDescription().toLowerCase().contains(lowerQuery) ||
            preset.getColors().stream().anyMatch(color -> color.toLowerCase().contains(lowerQuery)))
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
      if (presets == null)
        continue;

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

    public boolean isSuccess() {
      return success;
    }

    public String getMessage() {
      return message;
    }

    public PresetData getPreset() {
      return preset;
    }
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

    public ColorData getColorData() {
      return colorData;
    }

    public org.bukkit.Material getWoolMaterial() {
      return woolMaterial;
    }

    public String getDisplayName() {
      return displayName;
    }
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

    public UserStats getStats() {
      return stats;
    }

    public List<PresetData> getPresets() {
      return presets;
    }

    public PresetData getActiveGradient() {
      return activeGradient;
    }
  }

  public static class SystemStats {
    private final int totalUsers;
    private final int totalPresets;
    private final int activeUsers;
    private final int adminForcedUsers;
    private final ConfigManager.ColorStats colorStats;

    public SystemStats(int totalUsers, int totalPresets, int activeUsers, int adminForcedUsers,
        ConfigManager.ColorStats colorStats) {
      this.totalUsers = totalUsers;
      this.totalPresets = totalPresets;
      this.activeUsers = activeUsers;
      this.adminForcedUsers = adminForcedUsers;
      this.colorStats = colorStats;
    }

    public int getTotalUsers() {
      return totalUsers;
    }

    public int getTotalPresets() {
      return totalPresets;
    }

    public int getActiveUsers() {
      return activeUsers;
    }

    public int getAdminForcedUsers() {
      return adminForcedUsers;
    }

    public ConfigManager.ColorStats getColorStats() {
      return colorStats;
    }
  }

  /**
   * Save a global preset (new method for GlobalPresetData)
   */
  public boolean saveGlobalPreset(Player player, GlobalPresetData globalPreset) {
    UUID uuid = player.getUniqueId();

    // Convert to regular PresetData for validation
    PresetData presetData = new PresetData(globalPreset.getName(), globalPreset.getColors());
    ValidationResult validation = userDataManager.validatePreset(uuid, presetData);

    if (!validation.isValid()) {
      plugin.getLogger().warning("Validation failed for global preset: " + validation.getMessage());
      return false;
    }

    // Save the preset with global metadata
    return userDataManager.saveGlobalPreset(uuid, globalPreset);
  }

  /**
   * Update a global preset
   */
  public boolean updateGlobalPreset(Player player, GlobalPresetData globalPreset) {
    UUID uuid = player.getUniqueId();

    // Convert to regular PresetData for validation
    PresetData presetData = new PresetData(globalPreset.getName(), globalPreset.getColors());
    ValidationResult validation = userDataManager.validatePreset(uuid, presetData);

    if (!validation.isValid()) {
      plugin.getLogger().warning("Validation failed for global preset update: " + validation.getMessage());
      return false;
    }

    // Update the preset with global metadata
    return userDataManager.updateGlobalPreset(uuid, globalPreset);
  }

  /**
   * Check if player has a preset with given name
   */
  public boolean hasPreset(Player player, String presetName) {
    return userDataManager.hasPreset(player.getUniqueId(), presetName);
  }

  /**
   * Get all published global presets
   */
  public Map<String, GlobalPresetData> getPublishedGlobalPresets() {
    return userDataManager.getPublishedGlobalPresets();
  }

  /**
   * Admin method to publish/unpublish presets
   */
  public boolean setPresetPublished(String presetName, UUID owner, boolean published) {
    return userDataManager.setPresetPublished(presetName, owner, published);
  }

  /**
   * Admin method to make preset global/personal
   */
  public boolean setPresetGlobal(String presetName, UUID owner, boolean global) {
    return userDataManager.setPresetGlobal(presetName, owner, global);
  }

  public static class MaintenanceResult {
    private final int cleanedUsers;
    private final int removedPresets;

    public MaintenanceResult(int cleanedUsers, int removedPresets) {
      this.cleanedUsers = cleanedUsers;
      this.removedPresets = removedPresets;
    }

    public int getCleanedUsers() {
      return cleanedUsers;
    }

    public int getRemovedPresets() {
      return removedPresets;
    }
  }
}
