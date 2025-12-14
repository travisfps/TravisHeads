package com.travisheads.managers;

import com.travisheads.TravisHeads;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
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
        if (player == null || handler == null) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        GUIHandler oldHandler = activeGUIs.put(playerId, handler);
        
        if (oldHandler != null && oldHandler != handler) {
            try {
                oldHandler.onClose(player);
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao fechar GUI anterior: " + e.getMessage());
            }
        }
    }

    public void unregisterGUI(Player player) {
        if (player == null) return;
        
        GUIHandler handler = activeGUIs.remove(player.getUniqueId());
        if (handler != null) {
            try {
                handler.onClose(player);
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao desregistrar GUI: " + e.getMessage());
            }
        }
    }

    public boolean hasActiveGUI(Player player) {
        return player != null && activeGUIs.containsKey(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        GUIHandler handler = activeGUIs.get(player.getUniqueId());

        if (handler != null && handler.getInventory().equals(event.getInventory())) {
            event.setCancelled(true);
            
            try {
                handler.onClick(player, event.getSlot(), event.getCurrentItem());
            } catch (Exception e) {
                plugin.getLogger().severe("Erro ao processar clique no GUI: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        GUIHandler handler = activeGUIs.get(player.getUniqueId());

        if (handler != null && handler.getInventory().equals(event.getInventory())) {
            event.setCancelled(true);
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
        activeGUIs.forEach((uuid, handler) -> {
            try {
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.closeInventory();
                }
                handler.onClose(player);
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao fechar GUI: " + e.getMessage());
            }
        });
        activeGUIs.clear();
    }

    public interface GUIHandler {
        Inventory getInventory();
        void onClick(Player player, int slot, org.bukkit.inventory.ItemStack item);
        void onClose(Player player);
    }
}
