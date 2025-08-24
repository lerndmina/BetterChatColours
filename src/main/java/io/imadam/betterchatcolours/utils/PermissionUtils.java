package io.imadam.betterchatcolours.utils;

import org.bukkit.entity.Player;

public class PermissionUtils {

    public static int getMaxPresetCount(Player player, int configDefault) {
        // Check for specific preset permission numbers
        for (int i = 50; i >= 1; i--) { // Check from high to low
            if (player.hasPermission("chatcolors.presets." + i)) {
                return i;
            }
        }

        // Return default if no specific permission found
        return configDefault;
    }

    public static boolean hasColorPermission(Player player, String colorPermission) {
        if (colorPermission == null || colorPermission.isEmpty()) {
            return player.hasPermission("chatcolors.color.use");
        }

        return player.hasPermission(colorPermission);
    }

    public static boolean canUsePlugin(Player player) {
        return player.hasPermission("chatcolors.use");
    }

    public static boolean canCreatePresets(Player player, int configDefault) {
        // Must have basic plugin access
        if (!canUsePlugin(player)) {
            return false;
        }

        // Must have at least 1 preset slot available
        return getMaxPresetCount(player, configDefault) > 0;
    }

    public static boolean isAdmin(Player player) {
        return player.hasPermission("chatcolors.admin");
    }
}
