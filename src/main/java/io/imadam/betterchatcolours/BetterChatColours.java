package io.imadam.betterchatcolours;

import io.imadam.betterchatcolours.commands.ChatColorsCommand;
import io.imadam.betterchatcolours.data.GlobalPresetManager;
import io.imadam.betterchatcolours.data.UserDataManager;
import io.imadam.betterchatcolours.placeholders.ChatColorsExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.InvUI;

public class BetterChatColours extends JavaPlugin {

  private static BetterChatColours instance;
  private GlobalPresetManager globalPresetManager;
  private UserDataManager userDataManager;

  @Override
  public void onEnable() {
    instance = this;

    // Initialize InvUI
    InvUI.getInstance().setPlugin(this);

    // Create data directory
    if (!getDataFolder().exists()) {
      getDataFolder().mkdirs();
    }

    // Initialize managers
    globalPresetManager = new GlobalPresetManager(this);
    userDataManager = new UserDataManager(this);

    // Load data
    globalPresetManager.loadPresets();
    userDataManager.loadUserData();

    // Register commands
    getCommand("chatcolors").setExecutor(new ChatColorsCommand(this));

    // Register listeners
    getServer().getPluginManager().registerEvents(new io.imadam.betterchatcolours.gui.FallbackGUIListener(), this);
    getServer().getPluginManager().registerEvents(new io.imadam.betterchatcolours.gui.ChatInputManager(), this);

    // Register PlaceholderAPI expansion if available
    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
      new ChatColorsExpansion(this).register();
      getLogger().info("PlaceholderAPI integration enabled!");
    } else {
      getLogger().warning("PlaceholderAPI not found! Some features may not work.");
    }

    getLogger().info("BetterChatColours has been enabled!");
  }

  @Override
  public void onDisable() {
    // Save data
    if (globalPresetManager != null) {
      globalPresetManager.savePresets();
    }
    if (userDataManager != null) {
      userDataManager.saveUserData();
    }

    getLogger().info("BetterChatColours has been disabled!");
  }

  public static BetterChatColours getInstance() {
    return instance;
  }

  public GlobalPresetManager getGlobalPresetManager() {
    return globalPresetManager;
  }

  public UserDataManager getUserDataManager() {
    return userDataManager;
  }

  public void reload() {
    getLogger().info("Reloading BetterChatColours...");

    // Reload data
    globalPresetManager.loadPresets();
    userDataManager.loadUserData();

    getLogger().info("BetterChatColours reloaded successfully!");
  }
}
