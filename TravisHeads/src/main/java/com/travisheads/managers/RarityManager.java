package com.travisheads.managers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.travisheads.TravisHeads;
import com.travisheads.models.Rarity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RarityManager {

    private final TravisHeads plugin;
    private final Map<String, Rarity> rarities = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private volatile double totalChance = 0.0;
    private volatile List<Rarity> rarityList = new ArrayList<>();

    private final Cache<String, Rarity> randomRarityCache;

    public RarityManager(TravisHeads plugin) {
        this.plugin = plugin;

        this.randomRarityCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();

        loadRarities();
    }

    public void loadRarities() {
        lock.writeLock().lock();
        try {
            rarities.clear();
            randomRarityCache.invalidateAll();

            FileConfiguration config = plugin.getConfigManager().getRaritiesConfig();
            ConfigurationSection raritiesSection = config.getConfigurationSection("rarities");
            
            if (raritiesSection == null) {
                plugin.getLogger().warning("Seção 'rarities' não encontrada no config!");
                return;
            }

            double newTotalChance = 0.0;
            List<Rarity> newList = new ArrayList<>();

            for (String key : raritiesSection.getKeys(false)) {
                String path = "rarities." + key;
                String displayName = config.getString(path + ".displayName", key);
                double chance = config.getDouble(path + ".chance", 10.0);
                String color = config.getString(path + ".color", "&f");

                if (chance <= 0) {
                    plugin.getLogger().warning("Raridade '" + key + "' tem chance inválida: " + chance);
                    continue;
                }

                Rarity rarity = new Rarity(key, displayName, chance, color);
                rarities.put(key, rarity);
                newList.add(rarity);
                newTotalChance += chance;
            }

            this.totalChance = newTotalChance;
            this.rarityList = Collections.unmodifiableList(newList);

            plugin.getLogger().info(String.format(
                "Carregadas %d raridades! Total chance: %.2f", 
                rarities.size(), 
                totalChance
            ));

        } finally {
            lock.writeLock().unlock();
        }
    }

    public Rarity getRandomRarity() {
        lock.readLock().lock();
        try {
            if (rarityList.isEmpty()) {
                plugin.getLogger().warning("Nenhuma raridade disponível!");
                return null;
            }

            if (totalChance <= 0) {
                plugin.getLogger().warning("Total chance é 0 ou negativo!");
                return rarityList.get(0);
            }

            double random = Math.random() * totalChance;
            double current = 0;

            for (Rarity rarity : rarityList) {
                current += rarity.getChance();
                if (random <= current) {
                    return rarity;
                }
            }

            return rarityList.get(rarityList.size() - 1);

        } finally {
            lock.readLock().unlock();
        }
    }

    public Rarity getRarity(String id) {
        if (id == null) return null;
        
        lock.readLock().lock();
        try {
            return rarities.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Collection<Rarity> getAllRarities() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(rarityList);
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getStats() {
        lock.readLock().lock();
        try {
            StringBuilder stats = new StringBuilder("Rarities:\n");
            for (Rarity rarity : rarityList) {
                double percentage = totalChance > 0 ? (rarity.getChance() / totalChance) * 100 : 0;
                stats.append(String.format("  %s: %.2f%% chance\n",
                        rarity.getDisplayName(), percentage));
            }
            return stats.toString();
        } finally {
            lock.readLock().unlock();
        }
    }
}
