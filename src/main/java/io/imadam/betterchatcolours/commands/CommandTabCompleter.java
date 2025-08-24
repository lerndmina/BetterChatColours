package io.imadam.betterchatcolours.commands;

import io.imadam.betterchatcolours.BetterChatColours;
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
            // First argument - subcommands for new system
            List<String> subCommands = Arrays.asList("help", "list", "equip", "clear", "reload", "admin");

            return subCommands.stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "equip":
                    // Second argument - available global preset names for the player
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        return plugin.getGlobalPresetManager().getAvailablePresetsMap(player).values().stream()
                                .map(preset -> preset.getName())
                                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                                .collect(Collectors.toList());
                    }
                    break;

                case "admin":
                    // Second argument - admin subcommands
                    if (sender.hasPermission("chatcolors.admin")) {
                        List<String> adminCommands = Arrays.asList("create", "delete", "force", "clearforce");
                        return adminCommands.stream()
                                .filter(cmd -> cmd.startsWith(args[1].toLowerCase()))
                                .collect(Collectors.toList());
                    }
                    break;

                case "set":
                case "adminclear":
                case "presets":
                case "delete":
                case "info":
                case "publish":
                case "unpublish":
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
            
            if ("admin".equals(subCommand)) {
                String adminCommand = args[1].toLowerCase();
                
                switch (adminCommand) {
                    case "force":
                    case "clearforce":
                        // Third argument - player names
                        return plugin.getServer().getOnlinePlayers().stream()
                                .map(Player::getName)
                                .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                                .collect(Collectors.toList());
                                
                    case "delete":
                        // Third argument - preset names
                        return plugin.getGlobalPresetManager().getAllPresets().values().stream()
                                .map(preset -> preset.getName())
                                .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                                .collect(Collectors.toList());
                                
                    case "create":
                        // Third argument - preset name (no completion)
                        break;
                }
            }
        }

        if (args.length == 4) {
            String subCommand = args[0].toLowerCase();
            
            if ("admin".equals(subCommand)) {
                String adminCommand = args[1].toLowerCase();
                
                if ("force".equals(adminCommand)) {
                    // Fourth argument - preset names for force command
                    return plugin.getGlobalPresetManager().getAllPresets().values().stream()
                            .map(preset -> preset.getName())
                            .filter(name -> name.toLowerCase().startsWith(args[3].toLowerCase()))
                            .collect(Collectors.toList());
                } else if ("create".equals(adminCommand)) {
                    // Fourth argument and beyond - hex colors
                    completions.addAll(Arrays.asList("#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF"));
                    return completions.stream()
                            .filter(completion -> completion.toLowerCase().startsWith(args[3].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        }

        // For admin create command with 5+ arguments (additional colors)
        if (args.length >= 5) {
            String subCommand = args[0].toLowerCase();
            
            if ("admin".equals(subCommand) && "create".equals(args[1].toLowerCase())) {
                // Additional hex colors
                completions.addAll(Arrays.asList("#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF"));
                String currentArg = args[args.length - 1];
                return completions.stream()
                        .filter(completion -> completion.toLowerCase().startsWith(currentArg.toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        // Legacy 3-argument handling (keep for backwards compatibility)
        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "set":
                case "force":
                    // Third argument - global preset names
                    String playerName = args[1];
                    Player target = plugin.getServer().getPlayer(playerName);
                    if (target != null) {
                        // Add available global preset names for the target player
                        completions.addAll(plugin.getGlobalPresetManager().getAvailablePresetsMap(target).values().stream()
                                .map(preset -> preset.getName())
                                .collect(Collectors.toList()));
                    }

                    // Add example hex codes for admin create commands
                    completions.addAll(Arrays.asList("#FF0000", "#00FF00", "#0000FF"));
                    break;

                case "delete":
                    // Third argument - all global preset names (admin can delete any)
                    completions.addAll(plugin.getGlobalPresetManager().getAllPresets().values().stream()
                            .map(preset -> preset.getName())
                            .collect(Collectors.toList()));
                    break;

                case "publish":
                case "unpublish":
                    // These commands are no longer used in the new system
                    // Global presets are managed differently now
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
                        "chatcolors.color.premium");

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
