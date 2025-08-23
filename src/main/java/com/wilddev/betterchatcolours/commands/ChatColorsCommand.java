package com.wilddev.betterchatcolours.commands;

import com.wilddev.betterchatcolours.BetterChatColours;
import com.wilddev.betterchatcolours.config.MessagesConfig;
import com.wilddev.betterchatcolours.data.PresetData;
import com.wilddev.betterchatcolours.data.UserDataManager;
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
                
                // TODO: Open main menu GUI
                player.sendMessage("§aOpening chat colors menu... (GUI not implemented yet)");
                return true;
            } else {
                sender.sendMessage("§cThis command can only be used by players when no arguments are provided.");
                return true;
            }
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                return handleReload(sender);
            
            case "set":
                return handleSet(sender, args);
            
            case "clear":
                return handleClear(sender, args);
            
            case "presets":
                return handlePresets(sender, args);
            
            case "delete":
                return handleDelete(sender, args);
            
            case "addcolor":
                return handleAddColor(sender, args);
            
            case "info":
                return handleInfo(sender, args);
            
            default:
                sender.sendMessage("§cUnknown subcommand. Usage: /" + label + " [reload|set|clear|presets|delete|addcolor|info]");
                return true;
        }
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
                sender.sendMessage("§cToo many colors! Maximum: " + plugin.getConfigManager().getMaxColorsPerGradient());
                return true;
            }
            
            PresetData gradient = new PresetData("admin-forced", colors);
            userDataManager.setAdminForcedGradient(target.getUniqueId(), gradient);
        }
        
        sender.sendMessage(messages.getMessage("commands.gradient-set", "player", target.getName()));
        target.sendMessage("§aYour chat gradient has been set by an administrator.");
        
        return true;
    }
    
    private boolean handleClear(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chatcolors.admin")) {
            sender.sendMessage(messages.getMessage("commands.no-permission"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /chatcolors clear <player>");
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
            sender.sendMessage(messages.getMessage("commands.preset-deleted", "preset", presetName, "player", target.getName()));
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
        
        sender.sendMessage(messages.getMessage("info.preset-count", "count", String.valueOf(presetCount), "limit", "N/A"));
        
        return true;
    }
}
