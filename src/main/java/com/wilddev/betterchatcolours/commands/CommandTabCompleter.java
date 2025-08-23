package com.wilddev.betterchatcolours.commands;

import com.wilddev.betterchatcolours.BetterChatColours;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandTabCompleter implements TabCompleter {
    
    private final BetterChatColours plugin;
    
    public CommandTabCompleter(BetterChatColours plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - subcommands
            List<String> subCommands = Arrays.asList("reload", "set", "clear", "presets", "delete", "addcolor", "info");
            
            return subCommands.stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "set":
                case "clear":
                case "presets":
                case "delete":
                case "info":
                    // Second argument - player names
                    return plugin.getServer().getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                
                case "addcolor":
                    // Second argument - color name (no tab completion)
                    return new ArrayList<>();
            }
        }
        
        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "set":
                    // Third argument - preset names or hex codes
                    String playerName = args[1];
                    Player target = plugin.getServer().getPlayer(playerName);
                    if (target != null) {
                        // Add user's preset names
                        completions.addAll(plugin.getUserDataManager().getUserPresets(target.getUniqueId()).keySet());
                    }
                    
                    // Add example hex codes
                    completions.addAll(Arrays.asList("#FF0000", "#00FF00", "#0000FF"));
                    break;
                
                case "delete":
                    // Third argument - preset names
                    String targetName = args[1];
                    Player deleteTarget = plugin.getServer().getPlayer(targetName);
                    if (deleteTarget != null) {
                        completions.addAll(plugin.getUserDataManager().getUserPresets(deleteTarget.getUniqueId()).keySet());
                    }
                    break;
                
                case "addcolor":
                    // Third argument - hex code (no tab completion)
                    break;
            }
            
            return completions.stream()
                    .filter(completion -> completion.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 4) {
            String subCommand = args[0].toLowerCase();
            
            if ("addcolor".equals(subCommand)) {
                // Fourth argument - permission
                List<String> permissions = Arrays.asList(
                        "chatcolors.color.use",
                        "chatcolors.color.vip",
                        "chatcolors.color.premium"
                );
                
                return permissions.stream()
                        .filter(perm -> perm.toLowerCase().startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
            }
            
            if ("set".equals(subCommand)) {
                // Additional hex colors for multi-color gradients
                return Arrays.asList("#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF");
            }
        }
        
        if (args.length >= 5 && "set".equals(args[0].toLowerCase())) {
            // Additional hex colors for multi-color gradients
            return Arrays.asList("#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF");
        }
        
        return new ArrayList<>();
    }
}
