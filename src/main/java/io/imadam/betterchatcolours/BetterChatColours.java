package io.imadam.betterchatcolours;

import io.imadam.betterchatcolours.commands.ChatColorsCommand;
import io.imadam.betterchatcolours.commands.CommandTabCompleter;
import io.imadam.betterchatcolours.config.ConfigManager;
import io.imadam.betterchatcolours.config.MessagesConfig;
import io.imadam.betterchatcolours.data.DataManager;
import io.imadam.betterchatcolours.data.GlobalPresetManager;
import io.imadam.betterchatcolours.data.UserDataManager;
import io.imadam.betterchatcolours.gui.GUIListener;
import io.imadam.betterchatcolours.placeholders.ChatColorsExpansion;
import io.imadam.betterchatcolours.utils.PerformanceMonitor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class BetterChatColours extends JavaPlugin {

    private static BetterChatColours instance;
    private ConfigManager configManager;
    private MessagesConfig messagesConfig;
    private UserDataManager userDataManager;
    private GlobalPresetManager globalPresetManager;
    private DataManager dataManager;
    private ChatColorsExpansion placeholderExpansion;
    private GUIListener guiListener;
    private PerformanceMonitor performanceMonitor;

    @Override
    public void onEnable() {
        instance = this;

        try {
            // Initialize configuration
            saveDefaultConfig();
            configManager = new ConfigManager(this);
            messagesConfig = new MessagesConfig(this);

            // Initialize data manager
            userDataManager = new UserDataManager(this);
            globalPresetManager = new GlobalPresetManager(this);
            dataManager = new DataManager(this);

            // Initialize performance monitoring
            performanceMonitor = new PerformanceMonitor(this);

            // Register commands
            ChatColorsCommand mainCommand = new ChatColorsCommand(this);
            getCommand("chatcolors").setExecutor(mainCommand);
            getCommand("chatcolors").setTabCompleter(new CommandTabCompleter(this));

            // Register listeners
            guiListener = new GUIListener(this);
            getServer().getPluginManager().registerEvents(guiListener, this);

            // Initialize PlaceholderAPI integration
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                placeholderExpansion = new ChatColorsExpansion(this);
                placeholderExpansion.register();
                getLogger().info("PlaceholderAPI integration enabled");
            } else {
                getLogger().severe("PlaceholderAPI not found! This plugin requires PlaceholderAPI to function.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            getLogger().info("BetterChatColours has been enabled successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable BetterChatColours", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // Unregister PlaceholderAPI expansion
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }

        // Save user data
        if (userDataManager != null) {
            userDataManager.saveAllData();
        }

        getLogger().info("BetterChatColours has been disabled!");
    }

    public void reload() {
        try {
            // Reload configuration
            reloadConfig();
            configManager.reload();
            messagesConfig.reload();

            // Reload user data
            userDataManager.reload();

            getLogger().info("Configuration reloaded successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to reload configuration", e);
        }
    }

    // Getters
    public static BetterChatColours getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    public UserDataManager getUserDataManager() {
        return userDataManager;
    }

    public GlobalPresetManager getGlobalPresetManager() {
        return globalPresetManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public GUIListener getGuiListener() {
        return guiListener;
    }

    public PerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }
}
