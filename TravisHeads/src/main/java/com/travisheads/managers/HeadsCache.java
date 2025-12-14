package com.travisheads.managers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.travisheads.TravisHeads;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class HeadsCache {

    private final TravisHeads plugin;

    private final Cache<UUID, Integer> totalHeadsCache;
    private final Cache<UUID, Map<String, Integer>> headsByRarityCache;

    public HeadsCache(TravisHeads plugin) {
        this.plugin = plugin;

        this.totalHeadsCache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .maximumSize(1000)
                .weakValues()
                .recordStats()
                .build();

        this.headsByRarityCache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .maximumSize(1000)
                .weakValues()
                .recordStats()
                .build();
    }

    public int getTotalHeads(Player player) {
        if (player == null || !player.isOnline()) return 0;

        UUID playerId = player.getUniqueId();
        Integer cached = totalHeadsCache.getIfPresent(playerId);
        
        if (cached != null) {
            return cached;
        }

        int total = plugin.getHeadsManager().getTotalHeads(player);
        totalHeadsCache.put(playerId, total);
        return total;
    }

    public Map<String, Integer> getHeadsByRarity(Player player) {
        if (player == null || !player.isOnline()) {
            return Collections.emptyMap();
        }

        UUID playerId = player.getUniqueId();
        Map<String, Integer> cached = headsByRarityCache.getIfPresent(playerId);
        
        if (cached != null) {
            return cached;
        }

        Map<String, Integer> data = plugin.getHeadsManager().getHeadsByRarity(player);
        headsByRarityCache.put(playerId, data);
        return data;
    }

    public void invalidate(Player player) {
        if (player == null) return;
        invalidate(player.getUniqueId());
    }

    public void invalidate(UUID playerId) {
        if (playerId == null) return;
        
        totalHeadsCache.invalidate(playerId);
        headsByRarityCache.invalidate(playerId);
    }

    public void invalidateAll() {
        totalHeadsCache.invalidateAll();
        headsByRarityCache.invalidateAll();
        cleanUp();
    }

    public String getStats() {
        return String.format(
                "HeadsCache Stats:\n" +
                        "  Total Heads: %d cached, %.2f%% hit rate, %d evictions\n" +
                        "  Heads by Rarity: %d cached, %.2f%% hit rate, %d evictions",
                totalHeadsCache.estimatedSize(),
                totalHeadsCache.stats().hitRate() * 100,
                totalHeadsCache.stats().evictionCount(),
                headsByRarityCache.estimatedSize(),
                headsByRarityCache.stats().hitRate() * 100,
                headsByRarityCache.stats().evictionCount()
        );
    }

    public long getEstimatedSize() {
        return totalHeadsCache.estimatedSize() + headsByRarityCache.estimatedSize();
    }

    public void cleanUp() {
        totalHeadsCache.cleanUp();
        headsByRarityCache.cleanUp();
    }
}
