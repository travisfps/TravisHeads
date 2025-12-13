package com.travisheads.managers;

import com.travisheads.TravisHeads;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GUIManager implements Listener {

    private final TravisHeads plugin;

    private final Map<UUID, GUIHandler> activeGUIs = new ConcurrentHashMap<>();

    public GUIManager(TravisHeads plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void registerGUI(Player player, Inventory inventory, GUIHandler handler) {
        UUID playerId = player.getUniqueId();

        GUIHandler oldHandler = activeGUIs.remove(playerId);
        if (oldHandler != null) {
            oldHandler.onClose(player);
        }

        activeGUIs.put(playerId, handler);
    }

    public void unregisterGUI(Player player) {
        GUIHandler handler = activeGUIs.remove(player.getUniqueId());
        if (handler != null) {
            handler.onClose(player);
        }
    }

    public boolean hasActiveGUI(Player player) {
        return activeGUIs.containsKey(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        GUIHandler handler = activeGUIs.get(player.getUniqueId());

        if (handler != null && handler.getInventory().equals(event.getInventory())) {
            event.setCancelled(true);
            handler.onClick(player, event.getSlot(), event.getCurrentItem());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        GUIHandler handler = activeGUIs.get(player.getUniqueId());

        if (handler != null && handler.getInventory().equals(event.getInventory())) {
            unregisterGUI(player);
        }
    }

    public void closeAll() {
        activeGUIs.values().forEach(handler -> {
        });
        activeGUIs.clear();
    }

    public interface GUIHandler {
        Inventory getInventory();
        void onClick(Player player, int slot, org.bukkit.inventory.ItemStack item);
        void onClose(Player player);
    }
}