package io.imadam.betterchatcolours.commands;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.config.MessagesConfig;
import io.imadam.betterchatcolours.data.PresetData;
import io.imadam.betterchatcolours.data.UserDataManager;
import io.imadam.betterchatcolours.gui.MainMenuGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ChatColorsCommand implements CommandExecutor {

    private final BetterChatColours plugin;
    private final MessagesConfig messages;
    private final UserDataManager userDataManager;
    private final Pattern hexPattern = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private final Pattern presetNamePattern = Pattern.compile("^[a-zA-Z0-9 ]{1,16}$");

    public ChatColorsCommand(BetterChatColours plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesConfig();
        this.userDataManager = plugin.getUserDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Open main GUI for players
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!player.hasPermission("chatcolors.use")) {
                    player.sendMessage(messages.getMessage("commands.no-permission"));
                    return true;
                }

                // Open main menu GUI
                new MainMenuGUI(plugin, player).open();
                return true;
            } else {
                sender.sendMessage("§cThis command can only be used by players when no arguments are provided.");
                return true;
            }
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                return handleHelp(sender);

            case "list":
                return handleList(sender);

            case "apply":
                return handleApply(sender, args);

            case "clear":
                return handlePlayerClear(sender);

            case "reload":
                return handleReload(sender);

            case "set":
                return handleSet(sender, args);

            case "presets":
                return handlePresets(sender, args);

            case "delete":
                return handleDelete(sender, args);

            case "addcolor":
                return handleAddColor(sender, args);

            case "info":
                return handleInfo(sender, args);

            case "stats":
                return handleStats(sender, args);

            case "publish":
                return handlePublish(sender, args);

            case "unpublish":
                return handleUnpublish(sender, args);

            // Admin clear with player argument
            case "adminclear":
                return handleAdminClear(sender, args);

            default:
                return handleHelp(sender); // Show help for unknown commands
        }
    }

    private boolean handleHelp(CommandSender sender) {
        sender.sendMessage(messages.getMessage("info.help-header"));

        if (sender instanceof Player) {
            // Show player commands
            sender.sendMessage(messages.getMessage("info.help-gui"));
            sender.sendMessage(messages.getMessage("info.help-list"));
            sender.sendMessage(messages.getMessage("info.help-apply"));
            sender.sendMessage(messages.getMessage("info.help-clear"));
        }

        if (sender.hasPermission("chatcolors.admin")) {
            // Show admin commands
            sender.sendMessage("§6§lAdmin Commands:");
            sender.sendMessage("§e/chatcolors reload §7- Reload configuration");
            sender.sendMessage("§e/chatcolors set <player> <preset> §7- Force set player to preset");
            sender.sendMessage("§e/chatcolors set <player> <color1> [color2]... §7- Force set gradient");
            sender.sendMessage("§e/chatcolors adminclear <player> §7- Clear player's gradient");
            sender.sendMessage("§e/chatcolors presets <player> §7- View player's presets");
            sender.sendMessage("§e/chatcolors delete <player> <preset> §7- Delete preset");
            sender.sendMessage("§e/chatcolors addcolor <name> <hex> [perm] [display] §7- Add color");
            sender.sendMessage("§e/chatcolors info <player> §7- Show player's gradient info");
        }

        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.getMessage("commands.player-only"));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("chatcolors.use")) {
            player.sendMessage(messages.getMessage("commands.no-permission"));
            return true;
        }

        var presets = userDataManager.getUserPresets(player.getUniqueId());

        if (presets.isEmpty()) {
            player.sendMessage("§7You have no saved presets.");
        } else {
            player.sendMessage("§6Your presets:");
            for (PresetData preset : presets.values()) {
                player.sendMessage("§7- §e" + preset.getName() + " §7(" + preset.getColors().size() + " colors)");
            }
        }

        return true;
    }

    private boolean handleApply(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.getMessage("commands.player-only"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /chatcolors apply <preset>");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("chatcolors.use")) {
            player.sendMessage(messages.getMessage("commands.no-permission"));
            return true;
        }

        String presetName = args[1];
        boolean success = plugin.getDataManager().applyPreset(player, presetName);

        if (success) {
            plugin.getMessagesConfig().sendMessage(player, "gui.preset-activated", "preset", presetName);
        } else {
            plugin.getMessagesConfig().sendMessage(player, "errors.preset-not-found", "preset", presetName);
        }

        return true;
    }

    private boolean handlePlayerClear(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.getMessage("commands.player-only"));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("chatcolors.use")) {
            player.sendMessage(messages.getMessage("commands.no-permission"));
            return true;
        }

        userDataManager.setActiveGradient(player.getUniqueId(), null);
        player.sendMessage(messages.getMessage("success.gradient-cleared"));

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("chatcolors.admin")) {
            sender.sendMessage(messages.getMessage("commands.no-permission"));
            return true;
        }

        try {
            plugin.reload();
            sender.sendMessage(messages.getMessage("commands.config-reloaded"));
        } catch (Exception e) {
            sender.sendMessage("§cFailed to reload configuration: " + e.getMessage());
            plugin.getLogger().severe("Failed to reload configuration: " + e.getMessage());
        }

        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chatcolors.admin")) {
            sender.sendMessage(messages.getMessage("commands.no-permission"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /chatcolors set <player> <preset|color1 [color2] [color3]...>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(messages.getMessage("commands.player-not-found", "player", args[1]));
            return true;
        }

        // Check if it's a preset name or hex colors
        if (args.length == 3 && !hexPattern.matcher(args[2]).matches()) {
            // It's a preset name
            String presetName = args[2];
            PresetData preset = userDataManager.getPreset(target.getUniqueId(), presetName);

            if (preset == null) {
                sender.sendMessage(messages.getMessage("commands.preset-not-found", "preset", presetName));
                return true;
            }

            userDataManager.setAdminForcedGradient(target.getUniqueId(), preset);
        } else {
            // It's hex colors
            List<String> colors = new ArrayList<>();
            for (int i = 2; i < args.length; i++) {
                if (!hexPattern.matcher(args[i]).matches()) {
                    sender.sendMessage("§cInvalid hex code: " + args[i]);
                    return true;
                }
                colors.add(args[i]);
            }

            if (colors.size() > plugin.getConfigManager().getMaxColorsPerGradient()) {
                sender.sendMessage(
                        "§cToo many colors! Maximum: " + plugin.getConfigManager().getMaxColorsPerGradient());
                return true;
            }

            PresetData gradient = new PresetData("admin-forced", colors);
            userDataManager.setAdminForcedGradient(target.getUniqueId(), gradient);
        }

        sender.sendMessage(messages.getMessage("commands.gradient-set", "player", target.getName()));
        target.sendMessage("§aYour chat gradient has been set by an administrator.");

        return true;
    }

    private boolean handleAdminClear(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chatcolors.admin")) {
            sender.sendMessage(messages.getMessage("commands.no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /chatcolors adminclear <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(messages.getMessage("commands.player-not-found", "player", args[1]));
            return true;
        }

        userDataManager.clearAdminForcedGradient(target.getUniqueId());
        userDataManager.setActiveGradient(target.getUniqueId(), null);

        sender.sendMessage(messages.getMessage("commands.gradient-cleared", "player", target.getName()));
        target.sendMessage("§aYour chat gradient has been cleared by an administrator.");

        return true;
    }

    private boolean handlePresets(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chatcolors.admin")) {
            sender.sendMessage(messages.getMessage("commands.no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /chatcolors presets <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(messages.getMessage("commands.player-not-found", "player", args[1]));
            return true;
        }

        var presets = userDataManager.getUserPresets(target.getUniqueId());

        if (presets.isEmpty()) {
            sender.sendMessage("§7" + target.getName() + " has no saved presets.");
        } else {
            sender.sendMessage("§6" + target.getName() + "'s presets:");
            for (PresetData preset : presets.values()) {
                sender.sendMessage("§7- §e" + preset.getName() + " §7(" + preset.getColors().size() + " colors)");
            }
        }

        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chatcolors.admin")) {
            sender.sendMessage(messages.getMessage("commands.no-permission"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /chatcolors delete <player> <preset>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(messages.getMessage("commands.player-not-found", "player", args[1]));
            return true;
        }

        String presetName = args[2];
        boolean removed = userDataManager.removePreset(target.getUniqueId(), presetName);

        if (removed) {
            sender.sendMessage(
                    messages.getMessage("commands.preset-deleted", "preset", presetName, "player", target.getName()));
            target.sendMessage("§cYour preset '" + presetName + "' has been deleted by an administrator.");
        } else {
            sender.sendMessage(messages.getMessage("commands.preset-not-found", "preset", presetName));
        }

        return true;
    }

    private boolean handleAddColor(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chatcolors.admin")) {
            sender.sendMessage(messages.getMessage("commands.no-permission"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /chatcolors addcolor <colorname> <hexcode> [permission] [displayname]");
            return true;
        }

        String colorName = args[1].toLowerCase();
        String hexCode = args[2];
        String permission = args.length > 3 ? args[3] : null;
        String displayName = args.length > 4 ? String.join(" ", Arrays.copyOfRange(args, 4, args.length)) : null;

        if (!hexPattern.matcher(hexCode).matches()) {
            sender.sendMessage("§cInvalid hex code: " + hexCode);
            return true;
        }

        try {
            plugin.getConfigManager().addColor(colorName, hexCode, permission, displayName);
            sender.sendMessage(messages.getMessage("commands.color-added", "color", colorName));
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cFailed to add color: " + e.getMessage());
        }

        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chatcolors.admin")) {
            sender.sendMessage(messages.getMessage("commands.no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /chatcolors info <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(messages.getMessage("commands.player-not-found", "player", args[1]));
            return true;
        }

        PresetData activeGradient = userDataManager.getActiveGradient(target.getUniqueId());
        boolean hasForced = userDataManager.hasAdminForcedGradient(target.getUniqueId());
        int presetCount = userDataManager.getPresetCount(target.getUniqueId());

        sender.sendMessage("§6=== " + target.getName() + "'s Chat Color Info ===");

        if (activeGradient != null) {
            String gradientInfo = String.join(", ", activeGradient.getColors());
            sender.sendMessage(messages.getMessage("info.current-gradient", "gradient", gradientInfo));
            if (hasForced) {
                sender.sendMessage("§c(Admin forced)");
            }
        } else {
            sender.sendMessage(messages.getMessage("info.no-gradient"));
        }

        sender.sendMessage(
                messages.getMessage("info.preset-count", "count", String.valueOf(presetCount), "limit", "N/A"));

        return true;
    }

    private boolean handleStats(CommandSender sender, String[] args) {
        if (!sender.hasPermission("betterchatcolours.admin.stats")) {
            sender.sendMessage(messages.getMessage("commands.no-permission"));
            return true;
        }

        if (args.length == 1) {
            // Show performance statistics
            plugin.getPerformanceMonitor().logStatistics();
            sender.sendMessage("§aPerformance statistics logged to console");
        } else if (args.length == 2 && args[1].equalsIgnoreCase("reset")) {
            // Reset statistics
            plugin.getPerformanceMonitor().reset();
            sender.sendMessage("§aPerformance statistics have been reset");
        } else {
            sender.sendMessage("§cUsage: /chatcolors stats [reset]");
        }

        return true;
    }

    private boolean handlePublish(CommandSender sender, String[] args) {
        if (!sender.hasPermission("betterchatcolours.admin.publish")) {
            sender.sendMessage(messages.getMessage("commands.no-permission"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /chatcolors publish <player> <preset>");
            return true;
        }

        String playerName = args[1];
        String presetName = args[2];

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(messages.getMessage("commands.player-not-found", "player", playerName));
            return true;
        }

        // For now, just log the action - full implementation will come with global
        // preset system
        boolean success = plugin.getDataManager().setPresetPublished(presetName, target.getUniqueId(), true);

        if (success) {
            sender.sendMessage("§aPublished preset '" + presetName + "' by " + playerName);
            if (target.isOnline()) {
                target.sendMessage("§aYour preset '" + presetName + "' has been published by an admin!");
            }
        } else {
            sender.sendMessage("§cFailed to publish preset. Make sure the preset exists and player is valid.");
        }

        return true;
    }

    private boolean handleUnpublish(CommandSender sender, String[] args) {
        if (!sender.hasPermission("betterchatcolours.admin.publish")) {
            sender.sendMessage(messages.getMessage("commands.no-permission"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /chatcolors unpublish <player> <preset>");
            return true;
        }

        String playerName = args[1];
        String presetName = args[2];

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(messages.getMessage("commands.player-not-found", "player", playerName));
            return true;
        }

        // For now, just log the action - full implementation will come with global
        // preset system
        boolean success = plugin.getDataManager().setPresetPublished(presetName, target.getUniqueId(), false);

        if (success) {
            sender.sendMessage("§cUnpublished preset '" + presetName + "' by " + playerName);
            if (target.isOnline()) {
                target.sendMessage("§cYour preset '" + presetName + "' has been unpublished by an admin.");
            }
        } else {
            sender.sendMessage("§cFailed to unpublish preset. Make sure the preset exists and player is valid.");
        }

        return true;
    }
}
