package io.imadam.betterchatcolours.commands;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.data.GlobalPresetData;
import io.imadam.betterchatcolours.data.GlobalPresetManager;
import io.imadam.betterchatcolours.data.PresetData;
import io.imadam.betterchatcolours.data.UserDataManager;
import io.imadam.betterchatcolours.gui.PresetSelectionGUI;
import io.imadam.betterchatcolours.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Redesigned command system for global preset management
 * Users can only equip presets, admins can create/manage them
 */
public class ChatColorsCommand implements CommandExecutor {

    private final BetterChatColours plugin;
    private final UserDataManager userDataManager;
    private final GlobalPresetManager globalPresetManager;
    private final Pattern hexPattern = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private final Pattern presetNamePattern = Pattern.compile("^[a-zA-Z0-9 _-]{1,32}$");

    public ChatColorsCommand(BetterChatColours plugin) {
        this.plugin = plugin;
        this.userDataManager = plugin.getUserDataManager();
        this.globalPresetManager = plugin.getGlobalPresetManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Open preset selection GUI for players
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!player.hasPermission("chatcolors.use")) {
                    player.sendMessage(Component.text("You don't have permission to use chat colors.").color(NamedTextColor.RED));
                    return true;
                }

                // Open preset selection GUI
                new PresetSelectionGUI(plugin, player).openGUI();
                return true;
            } else {
                sender.sendMessage("§cThis command can only be used by players when no arguments are provided.");
                return true;
            }
        }

        // Handle subcommands
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "list":
                return handleListCommand(sender, args);
            case "equip":
                return handleEquipCommand(sender, args);
            case "clear":
                return handleClearCommand(sender, args);
            case "admin":
                return handleAdminCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender, args);
            case "help":
                return handleHelpCommand(sender, args);
            default:
                sender.sendMessage(Component.text("Unknown command. Use /chatcolors help for usage.").color(NamedTextColor.RED));
                return true;
        }
    }

    private boolean handleListCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        Map<String, GlobalPresetData> availablePresets = globalPresetManager.getAvailablePresetsMap(player);

        if (availablePresets.isEmpty()) {
            player.sendMessage(Component.text("You don't have access to any presets.").color(NamedTextColor.RED));
            return true;
        }

        player.sendMessage(Component.text("Available Presets:").color(NamedTextColor.YELLOW));
        
        String currentlyEquipped = userDataManager.getEquippedPreset(player.getUniqueId());
        
        for (Map.Entry<String, GlobalPresetData> entry : availablePresets.entrySet()) {
            String presetId = entry.getKey();
            GlobalPresetData preset = entry.getValue();
            
            String status = presetId.equals(currentlyEquipped) ? " §a(equipped)" : "";
            
            try {
                String previewText = ColorUtils.createGradientPreview(preset.getColors(), preset.getName());
                Component message = MiniMessage.miniMessage().deserialize("§7- " + previewText + status);
                player.sendMessage(message);
            } catch (Exception e) {
                player.sendMessage(Component.text("- " + preset.getName() + status).color(NamedTextColor.GRAY));
            }
        }

        return true;
    }

    private boolean handleEquipCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /chatcolors equip <preset_name>").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        String presetName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        // Find preset by name
        String presetId = findPresetIdByName(player, presetName);
        if (presetId == null) {
            player.sendMessage(Component.text("Preset not found or you don't have permission to use it.").color(NamedTextColor.RED));
            return true;
        }

        GlobalPresetData preset = globalPresetManager.getPreset(presetId);
        if (preset == null) {
            player.sendMessage(Component.text("Preset not found.").color(NamedTextColor.RED));
            return true;
        }

        // Equip the preset
        userDataManager.equipPreset(player.getUniqueId(), presetId);

        try {
            String previewMessage = ColorUtils.createGradientPreview(preset.getColors(), "✓ Equipped preset: " + preset.getName());
            Component message = MiniMessage.miniMessage().deserialize(previewMessage);
            player.sendMessage(message);
        } catch (Exception e) {
            player.sendMessage(Component.text("✓ Equipped preset: " + preset.getName()).color(NamedTextColor.GREEN));
        }

        return true;
    }

    private boolean handleClearCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        userDataManager.clearEquippedPreset(player.getUniqueId());
        player.sendMessage(Component.text("✓ Cleared your equipped preset.").color(NamedTextColor.GREEN));
        return true;
    }

    private boolean handleAdminCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chatcolors.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use admin commands.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Admin commands:").color(NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("/chatcolors admin create <name> <color1> [color2] [color3]...").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("/chatcolors admin delete <name>").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("/chatcolors admin force <player> <preset>").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("/chatcolors admin clearforce <player>").color(NamedTextColor.GRAY));
            return true;
        }

        String adminSubCommand = args[1].toLowerCase();

        switch (adminSubCommand) {
            case "create":
                return handleAdminCreateCommand(sender, args);
            case "delete":
                return handleAdminDeleteCommand(sender, args);
            case "force":
                return handleAdminForceCommand(sender, args);
            case "clearforce":
                return handleAdminClearForceCommand(sender, args);
            default:
                sender.sendMessage(Component.text("Unknown admin command.").color(NamedTextColor.RED));
                return true;
        }
    }

    private boolean handleAdminCreateCommand(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Component.text("Usage: /chatcolors admin create <name> <color1> [color2] [color3]...").color(NamedTextColor.RED));
            sender.sendMessage(Component.text("Example: /chatcolors admin create Ocean #00BFFF #1E90FF #0000FF").color(NamedTextColor.GRAY));
            return true;
        }

        String presetName = args[2];
        if (!presetNamePattern.matcher(presetName).matches()) {
            sender.sendMessage(Component.text("Invalid preset name. Use only letters, numbers, spaces, hyphens, and underscores (max 32 chars).").color(NamedTextColor.RED));
            return true;
        }

        List<String> colors = new ArrayList<>();
        for (int i = 3; i < args.length; i++) {
            String color = args[i];
            if (!hexPattern.matcher(color).matches()) {
                sender.sendMessage(Component.text("Invalid color format: " + color + ". Use hex format like #FF0000").color(NamedTextColor.RED));
                return true;
            }
            colors.add(color);
        }

        if (colors.isEmpty()) {
            sender.sendMessage(Component.text("At least one color is required.").color(NamedTextColor.RED));
            return true;
        }

        // Create preset ID from name
        String presetId = "custom_" + presetName.toLowerCase().replaceAll("[^a-z0-9]", "_");

        // Check if preset already exists
        if (globalPresetManager.hasPreset(presetId)) {
            sender.sendMessage(Component.text("A preset with this name already exists.").color(NamedTextColor.RED));
            return true;
        }

        // Create the preset
        String permission = "chatcolors.preset." + presetName.toLowerCase().replaceAll("[^a-z0-9]", "_");
        boolean success = globalPresetManager.createPreset(presetId, presetName, colors, permission);

        if (success) {
            try {
                String previewMessage = ColorUtils.createGradientPreview(colors, "✓ Created preset: " + presetName);
                Component message = MiniMessage.miniMessage().deserialize(previewMessage);
                sender.sendMessage(message);
            } catch (Exception e) {
                sender.sendMessage(Component.text("✓ Created preset: " + presetName).color(NamedTextColor.GREEN));
            }
            sender.sendMessage(Component.text("Permission required: " + permission).color(NamedTextColor.GRAY));
        } else {
            sender.sendMessage(Component.text("Failed to create preset.").color(NamedTextColor.RED));
        }

        return true;
    }

    private boolean handleAdminDeleteCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /chatcolors admin delete <name>").color(NamedTextColor.RED));
            return true;
        }

        String presetName = args[2];
        String presetId = findAnyPresetIdByName(presetName);
        
        if (presetId == null) {
            sender.sendMessage(Component.text("Preset not found: " + presetName).color(NamedTextColor.RED));
            return true;
        }

        GlobalPresetData preset = globalPresetManager.getPreset(presetId);
        if (preset != null && preset.isDefault()) {
            sender.sendMessage(Component.text("Cannot delete default presets.").color(NamedTextColor.RED));
            return true;
        }

        boolean success = globalPresetManager.deletePreset(presetId);
        if (success) {
            sender.sendMessage(Component.text("✓ Deleted preset: " + presetName).color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Failed to delete preset.").color(NamedTextColor.RED));
        }

        return true;
    }

    private boolean handleAdminForceCommand(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Component.text("Usage: /chatcolors admin force <player> <preset>").color(NamedTextColor.RED));
            return true;
        }

        String playerName = args[2];
        String presetName = args[3];

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found: " + playerName).color(NamedTextColor.RED));
            return true;
        }

        String presetId = findAnyPresetIdByName(presetName);
        if (presetId == null) {
            sender.sendMessage(Component.text("Preset not found: " + presetName).color(NamedTextColor.RED));
            return true;
        }

        GlobalPresetData preset = globalPresetManager.getPreset(presetId);
        if (preset == null) {
            sender.sendMessage(Component.text("Preset not found.").color(NamedTextColor.RED));
            return true;
        }

        // Create forced gradient
        PresetData forcedGradient = new PresetData("Admin Forced: " + preset.getName(), preset.getColors(), preset.getCreatedTime());
        userDataManager.setAdminForcedGradient(target.getUniqueId(), forcedGradient);

        sender.sendMessage(Component.text("✓ Applied forced gradient to " + target.getName()).color(NamedTextColor.GREEN));
        target.sendMessage(Component.text("An admin has applied a chat color gradient to your messages.").color(NamedTextColor.YELLOW));

        return true;
    }

    private boolean handleAdminClearForceCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /chatcolors admin clearforce <player>").color(NamedTextColor.RED));
            return true;
        }

        String playerName = args[2];
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found: " + playerName).color(NamedTextColor.RED));
            return true;
        }

        userDataManager.clearAdminForcedGradient(target.getUniqueId());
        sender.sendMessage(Component.text("✓ Cleared forced gradient for " + target.getName()).color(NamedTextColor.GREEN));
        target.sendMessage(Component.text("Your forced chat color gradient has been removed.").color(NamedTextColor.YELLOW));

        return true;
    }

    private boolean handleReloadCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chatcolors.admin.reload")) {
            sender.sendMessage(Component.text("You don't have permission to reload the plugin.").color(NamedTextColor.RED));
            return true;
        }

        plugin.reloadConfig();
        globalPresetManager.reload();
        userDataManager.reload();

        sender.sendMessage(Component.text("✓ BetterChatColours reloaded successfully.").color(NamedTextColor.GREEN));
        return true;
    }

    private boolean handleHelpCommand(CommandSender sender, String[] args) {
        sender.sendMessage(Component.text("BetterChatColours Commands:").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/chatcolors - Open preset selection GUI").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/chatcolors list - List available presets").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/chatcolors equip <preset> - Equip a preset").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/chatcolors clear - Clear equipped preset").color(NamedTextColor.GRAY));
        
        if (sender.hasPermission("chatcolors.admin")) {
            sender.sendMessage(Component.text("/chatcolors admin - Admin commands").color(NamedTextColor.GRAY));
        }
        
        if (sender.hasPermission("chatcolors.admin.reload")) {
            sender.sendMessage(Component.text("/chatcolors reload - Reload plugin").color(NamedTextColor.GRAY));
        }

        return true;
    }

    // Helper methods
    private String findPresetIdByName(Player player, String name) {
        Map<String, GlobalPresetData> availablePresets = globalPresetManager.getAvailablePresetsMap(player);
        
        for (Map.Entry<String, GlobalPresetData> entry : availablePresets.entrySet()) {
            if (entry.getValue().getName().equalsIgnoreCase(name)) {
                return entry.getKey();
            }
        }
        
        return null;
    }

    private String findAnyPresetIdByName(String name) {
        Map<String, GlobalPresetData> allPresets = globalPresetManager.getAllPresets();
        
        for (Map.Entry<String, GlobalPresetData> entry : allPresets.entrySet()) {
            if (entry.getValue().getName().equalsIgnoreCase(name)) {
                return entry.getKey();
            }
        }
        
        return null;
    }
}
