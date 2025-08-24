package io.imadam.betterchatcolours.config;

import io.imadam.betterchatcolours.BetterChatColours;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class MessagesConfig {

    private final BetterChatColours plugin;
    private FileConfiguration config;
    private File configFile;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessagesConfig(BetterChatColours plugin) {
        this.plugin = plugin;
        loadConfiguration();
    }

    public void reload() {
        loadConfiguration();
    }

    private void loadConfiguration() {
        configFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!configFile.exists()) {
            // Copy from resources instead of creating hardcoded defaults
            plugin.saveResource("messages.yml", false);
            plugin.getLogger().info("Created messages.yml from plugin resources");
        } else {
            plugin.getLogger().info("Found existing messages.yml file");
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Loaded messages.yml with " + config.getKeys(true).size() + " keys");

        // Debug: Log some key messages to verify content
        plugin.getLogger().info("Sample message 'gui.main-title': " + config.getString("gui.main-title", "NOT FOUND"));
        plugin.getLogger().info("Sample message 'errors.preset-create-failed': "
                + config.getString("errors.preset-create-failed", "NOT FOUND"));
    }

    private void createDefaultMessages() {
        try {
            plugin.getDataFolder().mkdirs();
            configFile.createNewFile();

            FileConfiguration defaultConfig = new YamlConfiguration();

            // GUI messages
            defaultConfig.set("gui.main-title", "&6Chat Colors - Select Preset");
            defaultConfig.set("gui.create-title", "&6Creating Gradient: {gradient_preview}");
            defaultConfig.set("gui.reorder-title", "&6Reorder Colors: {gradient_preview}");
            defaultConfig.set("gui.color-selection-title", "&6Select Colors ({current}/{max})");
            defaultConfig.set("gui.no-presets", "&7No presets saved");
            defaultConfig.set("gui.create-new", "&aCreate New Gradient");
            defaultConfig.set("gui.save-preset", "&aSave as Preset");
            defaultConfig.set("gui.delete-preset", "&cDelete Preset");
            defaultConfig.set("gui.activate-preset", "&eActivate Preset");

            // Command messages
            defaultConfig.set("commands.no-permission", "&cYou don't have permission to use this command.");
            defaultConfig.set("commands.player-not-found", "&cPlayer {player} not found.");
            defaultConfig.set("commands.preset-not-found", "&cPreset '{preset}' not found.");
            defaultConfig.set("commands.gradient-set", "&aGradient set for {player}.");
            defaultConfig.set("commands.gradient-cleared", "&aGradient cleared for {player}.");
            defaultConfig.set("commands.config-reloaded", "&aConfiguration reloaded successfully!");
            defaultConfig.set("commands.color-added", "&aColor '{color}' added successfully!");
            defaultConfig.set("commands.preset-deleted", "&aPreset '{preset}' deleted for {player}.");

            // Error messages
            defaultConfig.set("errors.invalid-hex", "&cInvalid hex code: {hex}. Skipping color '{color}'.");
            defaultConfig.set("errors.max-presets", "&cYou've reached your maximum preset limit ({limit}).");
            defaultConfig.set("errors.invalid-color", "&cColor '{color}' not found in palette.");
            defaultConfig.set("errors.no-colors-available", "&cNo colors available! Check your permissions.");
            defaultConfig.set("errors.preset-name-exists", "&cA preset with that name already exists!");
            defaultConfig.set("errors.invalid-preset-name",
                    "&cInvalid preset name! Use only letters, numbers, and spaces.");
            defaultConfig.set("errors.gradient-too-long", "&cGradient too long! Maximum {max} colors allowed.");

            // Info messages
            defaultConfig.set("info.current-gradient", "&7Current gradient: {gradient}");
            defaultConfig.set("info.no-gradient", "&7No gradient currently set.");
            defaultConfig.set("info.preset-count", "&7Presets: {count}/{limit}");
            defaultConfig.set("info.available-colors", "&7Available colors: {colors}");

            defaultConfig.save(configFile);
            plugin.getLogger().info("Created default messages.yml file");

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create messages.yml file", e);
        }
    }

    public String getMessage(String path) {
        String message = config.getString(path, "&cMessage not found: " + path);
        plugin.getLogger().info("Retrieved message for '" + path + "': '" + message + "'");
        return message;
    }

    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }

        return message;
    }

    public Component getComponent(String path) {
        return miniMessage.deserialize(getMessage(path));
    }

    public Component getComponent(String path, String... replacements) {
        return miniMessage.deserialize(getMessage(path, replacements));
    }

    /**
     * Send a processed MiniMessage to a player
     */
    public void sendMessage(org.bukkit.entity.Player player, String path) {
        player.sendMessage(getComponent(path));
    }

    /**
     * Send a processed MiniMessage to a player with replacements
     */
    public void sendMessage(org.bukkit.entity.Player player, String path, String... replacements) {
        player.sendMessage(getComponent(path, replacements));
    }
}
