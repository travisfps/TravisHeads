package com.travisheads.managers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.travisheads.TravisHeads;
import org.bukkit.entity.Player;

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
                .recordStats()
                .build();

        this.headsByRarityCache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .maximumSize(1000)
                .recordStats()
                .build();
    }

    public int getTotalHeads(Player player) {
        UUID playerId = player.getUniqueId();

        return totalHeadsCache.get(playerId, key ->
                plugin.getHeadsManager().getTotalHeads(player)
        );
    }

    public Map<String, Integer> getHeadsByRarity(Player player) {
        UUID playerId = player.getUniqueId();

        return headsByRarityCache.get(playerId, key ->
                plugin.getHeadsManager().getHeadsByRarity(player)
        );
    }

    public void invalidate(Player player) {
        UUID playerId = player.getUniqueId();
        totalHeadsCache.invalidate(playerId);
        headsByRarityCache.invalidate(playerId);
    }

    public void invalidate(UUID playerId) {
        totalHeadsCache.invalidate(playerId);
        headsByRarityCache.invalidate(playerId);
    }

    public void invalidateAll() {
        totalHeadsCache.invalidateAll();
        headsByRarityCache.invalidateAll();

        totalHeadsCache.cleanUp();
        headsByRarityCache.cleanUp();
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