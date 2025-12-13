package com.travisheads.gui;

import com.travisheads.TravisHeads;
import com.travisheads.managers.GUIManager;
import com.travisheads.utils.GUIItemBuilder;
import com.travisheads.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MainMenuGUI implements GUIManager.GUIHandler {

    private final TravisHeads plugin;
    private final Player player;
    private final Inventory inventory;
    private final ConfigurationSection menuConfig;

    public MainMenuGUI(TravisHeads plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        this.menuConfig = plugin.getConfigManager().getGuiConfig()
                .getConfigurationSection("menus.main");

        if (menuConfig == null) {
            throw new IllegalStateException("Menu configuration not found!");
        }

        int size = menuConfig.getInt("size", 27);
        String title = MessageUtil.color(menuConfig.getString("title", "&8Menu Principal"));
        this.inventory = Bukkit.createInventory(null, size, title);

        loadItems();
    }

    public void open() {
        plugin.getGUIManager().registerGUI(player, inventory, this);
        player.openInventory(inventory);
    }

    private void loadItems() {
        ConfigurationSection items = menuConfig.getConfigurationSection("items");
        if (items == null) return;

        String totalHeads = String.valueOf(plugin.getHeadsCache().getTotalHeads(player));

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%total_heads%", totalHeads);

        for (String key : items.getKeys(false)) {
            ConfigurationSection item = items.getConfigurationSection(key);
            if (item != null) {
                ItemStack menuItem = GUIItemBuilder.createItem(item, player, Material.PAPER, "", placeholders);
                if (menuItem != null) {
                    inventory.setItem(item.getInt("slot", 0), menuItem);
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(Player player, int slot, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        ConfigurationSection items = menuConfig.getConfigurationSection("items");
        if (items == null) return;

        for (String key : items.getKeys(false)) {
            ConfigurationSection itemConfig = items.getConfigurationSection(key);
            if (itemConfig == null || itemConfig.getInt("slot", -1) != slot) continue;

            String action = itemConfig.getString("action", "NONE");
            executeAction(player, action);
            break;
        }
    }

    private void executeAction(Player player, String action) {
        player.closeInventory();

        switch (action.toUpperCase()) {
            case "OPEN_HEADS":
                new HeadsListGUI(plugin, player).open();
                break;

            case "OPEN_EXCHANGES":
                new ExchangesGUI(plugin, player).open();
                break;

            default:
                break;
        }
    }

    @Override
    public void onClose(Player player) {
    }
}