package com.wilddev.betterchatcolours;

import com.wilddev.betterchatcolours.commands.ChatColorsCommand;
import com.wilddev.betterchatcolours.commands.CommandTabCompleter;
import com.wilddev.betterchatcolours.config.ConfigManager;
import com.wilddev.betterchatcolours.config.MessagesConfig;
import com.wilddev.betterchatcolours.data.DataManager;
import com.wilddev.betterchatcolours.data.UserDataManager;
import com.wilddev.betterchatcolours.listeners.GUIListener;
import com.wilddev.betterchatcolours.placeholders.ChatColorsExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class BetterChatColours extends JavaPlugin {

    private static BetterChatColours instance;
    private ConfigManager configManager;
    private MessagesConfig messagesConfig;
    private UserDataManager userDataManager;
    private DataManager dataManager;
    private ChatColorsExpansion placeholderExpansion;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize configuration
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        messagesConfig = new MessagesConfig(this);

        // Initialize data manager
        userDataManager = new UserDataManager(this);
        dataManager = new DataManager(this);

        // Register commands
        ChatColorsCommand mainCommand = new ChatColorsCommand(this);
        getCommand("chatcolors").setExecutor(mainCommand);
        getCommand("chatcolors").setTabCompleter(new CommandTabCompleter(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        // Initialize PlaceholderAPI integration
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new ChatColorsExpansion(this);
            placeholderExpansion.register();
            getLogger().info("PlaceholderAPI integration enabled");
        } else {
            getLogger().warning("PlaceholderAPI not found! Plugin will not work without it.");
        }

        getLogger().info("BetterChatColours has been enabled!");
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
    
    public DataManager getDataManager() {
        return dataManager;
    }
}
