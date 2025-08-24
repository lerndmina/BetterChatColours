package io.imadam.betterchatcolours.commands;

import io.imadam.betterchatcolours.BetterChatColours;
import io.imadam.betterchatcolours.gui.MainMenuGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatColorsCommand implements CommandExecutor {

  private final BetterChatColours plugin;

  public ChatColorsCommand(BetterChatColours plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
      @NotNull String[] args) {

    if (!(sender instanceof Player)) {
      sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
      return true;
    }

    Player player = (Player) sender;

    // Check if player has basic permission
    if (!player.hasPermission("betterchatcolours.use")) {
      player.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
      return true;
    }

    // Handle reload subcommand
    if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
      if (!player.hasPermission("betterchatcolours.admin")) {
        player.sendMessage(Component.text("You don't have permission to reload the plugin!", NamedTextColor.RED));
        return true;
      }

      try {
        plugin.reload();
        player.sendMessage(Component.text("BetterChatColours reloaded successfully!", NamedTextColor.GREEN));
      } catch (Exception e) {
        player.sendMessage(Component.text("Error reloading plugin: " + e.getMessage(), NamedTextColor.RED));
        plugin.getLogger().severe("Error reloading plugin: " + e.getMessage());
      }
      return true;
    }

    // Open main menu GUI
    try {
      // Use fallback GUI for now (InvUI version compatibility issue)
      io.imadam.betterchatcolours.gui.FallbackMainMenuGUI.open(player);
      // MainMenuGUI.open(player);
    } catch (Exception e) {
      player.sendMessage(Component.text("Error opening GUI: " + e.getMessage(), NamedTextColor.RED));
      plugin.getLogger().severe("Error opening main menu GUI: " + e.getMessage());
    }

    return true;
  }
}
