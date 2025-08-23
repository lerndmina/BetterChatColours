package com.wilddev.betterchatcolours.listeners;

import com.wilddev.betterchatcolours.BetterChatColours;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GUIListener implements Listener {
    
    private final BetterChatColours plugin;
    
    public GUIListener(BetterChatColours plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // TODO: Implement GUI click handling
        // This will be implemented in Phase 3 when we create the GUI system
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // TODO: Implement GUI close handling
        // This will be implemented in Phase 3 when we create the GUI system
    }
}
