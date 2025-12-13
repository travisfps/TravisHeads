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

public class ExchangesGUI implements GUIManager.GUIHandler {

    private final TravisHeads plugin;
    private final Player player;
    private final Inventory inventory;
    private final ConfigurationSection menuConfig;

    public ExchangesGUI(TravisHeads plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        this.menuConfig = plugin.getConfigManager().getGuiConfig()
                .getConfigurationSection("menus.exchanges");

        if (menuConfig == null) {
            throw new IllegalStateException("Exchanges menu configuration not found!");
        }

        int size = menuConfig.getInt("size", 54);
        String title = MessageUtil.color(menuConfig.getString("title", "&8Trocas"));
        this.inventory = Bukkit.createInventory(null, size, title);

        loadItems();
    }

    public void open() {
        plugin.getGUIManager().registerGUI(player, inventory, this);
        player.openInventory(inventory);
    }

    private void loadItems() {
        loadExchangeItems();
        loadBackButton();
    }

    private void loadExchangeItems() {
        ConfigurationSection exchanges = plugin.getConfigManager().getExchangesConfig()
                .getConfigurationSection("exchanges");
        if (exchanges == null) return;

        for (String exchangeId : exchanges.getKeys(false)) {
            ConfigurationSection exchange = exchanges.getConfigurationSection(exchangeId);
            if (exchange != null) {
                ItemStack exchangeItem = createExchangeItem(exchange);
                if (exchangeItem != null) {
                    inventory.setItem(exchange.getInt("slot", 0), exchangeItem);
                }
            }
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

    private ItemStack createExchangeItem(ConfigurationSection exchange) {
        ConfigurationSection icon = exchange.getConfigurationSection("icon");
        if (icon == null) return null;

        return GUIItemBuilder.createItem(icon, player, Material.DIAMOND, "&eExchange");
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
            return;
        }

        processExchange(player, slot);
    }

    private void processExchange(Player player, int slot) {
        ConfigurationSection exchanges = plugin.getConfigManager().getExchangesConfig()
                .getConfigurationSection("exchanges");
        if (exchanges == null) return;

        for (String exchangeId : exchanges.getKeys(false)) {
            ConfigurationSection exchange = exchanges.getConfigurationSection(exchangeId);
            if (exchange == null || exchange.getInt("slot", -1) != slot) continue;

            ConfigurationSection requirements = exchange.getConfigurationSection("requirements");
            if (requirements == null) {
                player.sendMessage(MessageUtil.getMessage("exchange-error"));
                return;
            }

            String rarityId = requirements.getString("rarity", "");
            int amount = requirements.getInt("amount", 1);
            Rarity rarity = plugin.getRarityManager().getRarity(rarityId);

            if (rarity == null) {
                player.sendMessage(MessageUtil.getMessage("exchange-error"));
                return;
            }

            int playerHeads = plugin.getHeadsCache().getHeadsByRarity(player)
                    .getOrDefault(rarityId, 0);

            if (playerHeads < amount) {
                player.sendMessage(MessageUtil.getMessage("not-enough-heads")
                        .replace("%rarity%", rarity.getDisplayName())
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%current%", String.valueOf(playerHeads)));
                return;
            }

            if (!plugin.getHeadsManager().removeHeads(player, rarityId, amount)) {
                player.sendMessage(MessageUtil.getMessage("exchange-error"));
                return;
            }

            giveRewards(player, exchange);
            player.sendMessage(MessageUtil.getMessage("exchange-success")
                    .replace("%rarity%", rarity.getDisplayName())
                    .replace("%amount%", String.valueOf(amount)));
            player.closeInventory();
            return;
        }
    }

    private void giveRewards(Player player, ConfigurationSection exchange) {
        ConfigurationSection rewards = exchange.getConfigurationSection("rewards");
        if (rewards == null) return;

        if (rewards.contains("commands")) {
            for (String command : rewards.getStringList("commands")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        command.replace("%player%", player.getName()));
            }
        }

        ConfigurationSection items = rewards.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection item = items.getConfigurationSection(key);
                if (item == null) continue;

                Material material = Material.getMaterial(item.getString("material", "DIAMOND"));
                if (material == null) continue;

                ItemBuilder builder = new ItemBuilder(material,
                        item.getInt("amount", 1),
                        (short) item.getInt("data", 0));

                if (item.contains("display-name")) {
                    builder.setDisplayName(item.getString("display-name"));
                }

                if (item.contains("lore")) {
                    for (String lore : item.getStringList("lore")) {
                        builder.addLore(lore);
                    }
                }

                player.getInventory().addItem(builder.build());
            }
        }
    }

    @Override
    public void onClose(Player player) {
    }
}