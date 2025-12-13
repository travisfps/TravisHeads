package com.travisheads.managers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.travisheads.TravisHeads;
import com.travisheads.models.Rarity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RarityManager {

    private final TravisHeads plugin;
    private final Map<String, Rarity> rarities = new LinkedHashMap<>();

    private final Cache<String, Rarity> randomRarityCache;
    private double totalChance = 0.0;

    public RarityManager(TravisHeads plugin) {
        this.plugin = plugin;

        this.randomRarityCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();

        loadRarities();
    }

    public void loadRarities() {
        rarities.clear();
        randomRarityCache.invalidateAll();

        FileConfiguration config = plugin.getConfigManager().getRaritiesConfig();

        ConfigurationSection raritiesSection = config.getConfigurationSection("rarities");
        if (raritiesSection == null) return;

        totalChance = 0.0;

        for (String key : raritiesSection.getKeys(false)) {
            String path = "rarities." + key;
            String displayName = config.getString(path + ".displayName", key);
            double chance = config.getDouble(path + ".chance", 10.0);
            String color = config.getString(path + ".color", "&f");

            Rarity rarity = new Rarity(key, displayName, chance, color);
            rarities.put(key, rarity);
            totalChance += chance;
        }

        plugin.getLogger().info("Carregadas " + rarities.size() + " raridades! Total chance: " + totalChance);
    }

    public Rarity getRandomRarity() {
        if (rarities.isEmpty()) return null;

        double random = Math.random() * totalChance;
        double current = 0;

        for (Rarity rarity : rarities.values()) {
            current += rarity.getChance();
            if (random <= current) {
                return rarity;
            }
        }

        return rarities.values().iterator().next();
    }

    public Rarity getRarity(String id) {
        return rarities.get(id);
    }

    public Collection<Rarity> getAllRarities() {
        return rarities.values();
    }

    public String getStats() {
        StringBuilder stats = new StringBuilder("Rarities:\n");
        for (Rarity rarity : rarities.values()) {
            double percentage = (rarity.getChance() / totalChance) * 100;
            stats.append(String.format("  %s: %.2f%% chance\n",
                    rarity.getDisplayName(), percentage));
        }
        return stats.toString();
    }
}