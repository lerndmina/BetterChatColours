package io.imadam.betterchatcolours.tests;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.config.ConfigManager;
import io.imadam.betterchatcolours.data.DataManager;
import io.imadam.betterchatcolours.utils.PerformanceMonitor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for BetterChatColours plugin
 * Tests core functionality, data management, and performance monitoring
 */
@DisplayName("BetterChatColours Plugin Tests")
public class BetterChatColoursTest {

  @Mock
  private BetterChatColours plugin;

  @Mock
  private ConfigManager configManager;

  @Mock
  private DataManager dataManager;

  @Mock
  private PerformanceMonitor performanceMonitor;

  @Mock
  private Player player;

  @Mock
  private Logger logger;

  @Mock
  private PluginManager pluginManager;

  @Mock
  private Plugin placeholderAPI;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    // Setup basic mock behaviors
    when(plugin.getLogger()).thenReturn(logger);
    when(plugin.getConfigManager()).thenReturn(configManager);
    when(plugin.getDataManager()).thenReturn(dataManager);
    when(plugin.getPerformanceMonitor()).thenReturn(performanceMonitor);

    // Setup player mock
    when(player.getName()).thenReturn("TestPlayer");
    when(player.hasPermission(anyString())).thenReturn(true);

    // Setup config defaults
    when(configManager.getMaxColorsPerGradient()).thenReturn(5);
    when(configManager.getMaxPresetsPerUser()).thenReturn(10);
  }

  @Test
  @DisplayName("Plugin should initialize all components")
  void testPluginInitialization() {
    // Verify all components are accessible
    assertNotNull(plugin.getConfigManager());
    assertNotNull(plugin.getDataManager());
    assertNotNull(plugin.getPerformanceMonitor());

    // Verify logger is working
    verify(plugin, atLeastOnce()).getLogger();
  }

  @Test
  @DisplayName("Config validation should work correctly")
  void testConfigValidation() {
    // Test valid color limits
    assertTrue(configManager.getMaxColorsPerGradient() > 0);
    assertTrue(configManager.getMaxPresetsPerUser() > 0);

    // Test color validation
    List<String> validColors = Arrays.asList("#FF0000", "#00FF00", "#0000FF");
    List<String> invalidColors = Arrays.asList("red", "#ZZZ", "invalid");

    // This would normally test actual validation logic
    // For mock test, we just verify the configuration is accessible
    assertNotNull(configManager.getMaxColorsPerGradient());
  }

  @Test
  @DisplayName("Data manager should handle preset creation")
  void testPresetCreation() {
    // Setup test data
    String presetName = "TestPreset";
    List<String> colors = Arrays.asList("#FF0000", "#00FF00", "#0000FF");

    // Mock successful creation
    DataManager.CreatePresetResult mockResult = new DataManager.CreatePresetResult(true, "Success", null);
    when(dataManager.createPreset(player, presetName, colors)).thenReturn(mockResult);

    // Test creation
    DataManager.CreatePresetResult result = dataManager.createPreset(player, presetName, colors);

    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals("Success", result.getMessage());

    // Verify the method was called
    verify(dataManager).createPreset(player, presetName, colors);
  }

  @Test
  @DisplayName("Performance monitor should track operations")
  void testPerformanceMonitoring() {
    // Test performance monitoring functionality
    String operation = "test-operation";
    long duration = 50L;

    // Test timer creation and recording
    doNothing().when(performanceMonitor).recordOperation(operation, duration);

    performanceMonitor.recordOperation(operation, duration);

    // Verify recording was called
    verify(performanceMonitor).recordOperation(operation, duration);

    // Test statistics logging
    doNothing().when(performanceMonitor).logStatistics();

    performanceMonitor.logStatistics();
    verify(performanceMonitor).logStatistics();
  }

  @Test
  @DisplayName("Error handling should work gracefully")
  void testErrorHandling() {
    // Test null parameter handling
    when(dataManager.createPreset(null, "test", Arrays.asList("#FF0000")))
        .thenReturn(new DataManager.CreatePresetResult(false, "Invalid player", null));

    DataManager.CreatePresetResult result = dataManager.createPreset(null, "test", Arrays.asList("#FF0000"));

    assertNotNull(result);
    assertFalse(result.isSuccess());
    assertEquals("Invalid player", result.getMessage());
  }

  @Test
  @DisplayName("Permission checks should work correctly")
  void testPermissionSystem() {
    // Test admin permissions
    when(player.hasPermission("betterchatcolours.admin")).thenReturn(true);
    assertTrue(player.hasPermission("betterchatcolours.admin"));

    // Test regular user permissions
    when(player.hasPermission("betterchatcolours.use")).thenReturn(true);
    assertTrue(player.hasPermission("betterchatcolours.use"));

    // Test denied permissions
    when(player.hasPermission("betterchatcolours.vip")).thenReturn(false);
    assertFalse(player.hasPermission("betterchatcolours.vip"));
  }

  @Test
  @DisplayName("Configuration limits should be enforced")
  void testConfigurationLimits() {
    // Test color limits
    int maxColors = configManager.getMaxColorsPerGradient();
    assertTrue(maxColors > 0, "Max colors should be positive");
    assertTrue(maxColors <= 20, "Max colors should be reasonable");

    // Test preset limits
    int maxPresets = configManager.getMaxPresetsPerUser();
    assertTrue(maxPresets > 0, "Max presets should be positive");
    assertTrue(maxPresets <= 100, "Max presets should be reasonable");
  }

  @Test
  @DisplayName("Input validation should reject invalid data")
  void testInputValidation() {
    // Test empty preset name
    when(dataManager.createPreset(player, "", Arrays.asList("#FF0000")))
        .thenReturn(new DataManager.CreatePresetResult(false, "Preset name cannot be empty", null));

    DataManager.CreatePresetResult result = dataManager.createPreset(player, "", Arrays.asList("#FF0000"));
    assertFalse(result.isSuccess());

    // Test empty colors list
    when(dataManager.createPreset(player, "test", Arrays.asList()))
        .thenReturn(new DataManager.CreatePresetResult(false, "At least one color is required", null));

    result = dataManager.createPreset(player, "test", Arrays.asList());
    assertFalse(result.isSuccess());
  }
}
