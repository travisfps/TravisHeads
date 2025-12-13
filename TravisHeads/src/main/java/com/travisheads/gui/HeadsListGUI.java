package com.travisheads.gui;

import com.travisheads.TravisHeads;
import com.travisheads.managers.GUIManager;
import com.travisheads.models.Rarity;
import com.travisheads.utils.GUIItemBuilder;
import com.travisheads.utils.ItemBuilder;
import com.travisheads.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class HeadsListGUI implements GUIManager.GUIHandler {

    private final TravisHeads plugin;
    private final Player player;
    private final Inventory inventory;
    private final ConfigurationSection menuConfig;

    public HeadsListGUI(TravisHeads plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        this.menuConfig = plugin.getConfigManager().getGuiConfig()
                .getConfigurationSection("menus.heads");

        if (menuConfig == null) {
            throw new IllegalStateException("Heads menu configuration not found!");
        }

        int size = menuConfig.getInt("size", 54);
        String title = MessageUtil.color(menuConfig.getString("title", "&8Suas Heads"));
        this.inventory = Bukkit.createInventory(null, size, title);

        loadItems();
    }

    public void open() {
        plugin.getGUIManager().registerGUI(player, inventory, this);
        player.openInventory(inventory);
    }

    private void loadItems() {
        Map<String, Integer> headsByRarity = plugin.getHeadsCache().getHeadsByRarity(player);

        loadRarityItems(headsByRarity);
        loadBackButton();
    }

    private void loadRarityItems(Map<String, Integer> headsByRarity) {
        int defaultSlot = menuConfig.getInt("heads-start-slot", 10);

        for (Rarity rarity : plugin.getRarityManager().getAllRarities()) {
            int count = headsByRarity.getOrDefault(rarity.getId(), 0);

            ConfigurationSection rarityConfig = menuConfig.getConfigurationSection(rarity.getId());
            int slot;

            if (rarityConfig != null && rarityConfig.contains("slot")) {
                slot = rarityConfig.getInt("slot");
            } else {
                slot = defaultSlot++;
            }

            inventory.setItem(slot, createRarityItem(rarity, count));
        }
    }

    private void loadBackButton() {
        ConfigurationSection backButton = menuConfig.getConfigurationSection("back-button");
        if (backButton == null) return;

        int size = menuConfig.getInt("size", 54);
        ItemStack backItem = GUIItemBuilder.createItem(backButton, player, Material.BARRIER, "&cVoltar");
        if (backItem != null) {
            inventory.setItem(backButton.getInt("slot", size - 1), backItem);
        }
    }

    private ItemStack createRarityItem(Rarity rarity, int count) {
        ConfigurationSection display = menuConfig.getConfigurationSection(rarity.getId());

        if (display == null) {
            display = menuConfig.getConfigurationSection("rarity-item");
        }

        if (display == null) {
            ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3);
            builder.setDisplayName(rarity.getColor() + rarity.getDisplayName());
            builder.addLore("&7Quantidade: &f" + count);
            return builder.build();
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%rarity_color%", rarity.getColor());
        placeholders.put("%rarity_name%", rarity.getDisplayName());
        placeholders.put("%rarity_" + rarity.getId() + "%", rarity.getDisplayName());
        placeholders.put("%count%", String.valueOf(count));

        return GUIItemBuilder.createItem(display, player, Material.SKULL_ITEM,
                rarity.getColor() + rarity.getDisplayName(), placeholders);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(Player player, int slot, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        ConfigurationSection backButton = menuConfig.getConfigurationSection("back-button");
        if (backButton != null && slot == backButton.getInt("slot", -1)) {
            player.closeInventory();
            new MainMenuGUI(plugin, player).open();
        }
    }

    @Override
    public void onClose(Player player) {
    }
}