package com.travisheads.managers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConfigCache {

    private final ConfigManager configManager;

    private final Cache<String, ConfigurationSection> sectionCache;

    private final Cache<String, Object> valueCache;

    private final Cache<String, List<String>> listCache;

    public ConfigCache(ConfigManager configManager) {
        this.configManager = configManager;

        this.sectionCache = Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .maximumSize(500)
                .weakKeys()
                .build();

        this.valueCache = Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .maximumSize(1000)
                .weakKeys()
                .build();

        this.listCache = Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .maximumSize(300)
                .weakKeys()
                .build();
    }

    public ConfigurationSection getSection(FileConfiguration config, String path) {
        String cacheKey = getCacheKey(config, path);

        return sectionCache.get(cacheKey, k -> config.getConfigurationSection(path));
    }

    public String getString(FileConfiguration config, String path, String defaultValue) {
        String cacheKey = getCacheKey(config, path);

        return (String) valueCache.get(cacheKey, k -> {
            String value = config.getString(path);
            return value != null ? value : defaultValue;
        });
    }

    public int getInt(FileConfiguration config, String path, int defaultValue) {
        String cacheKey = getCacheKey(config, path);

        return (int) valueCache.get(cacheKey, k -> config.getInt(path, defaultValue));
    }

    public double getDouble(FileConfiguration config, String path, double defaultValue) {
        String cacheKey = getCacheKey(config, path);

        return (double) valueCache.get(cacheKey, k -> config.getDouble(path, defaultValue));
    }

    public boolean getBoolean(FileConfiguration config, String path, boolean defaultValue) {
        String cacheKey = getCacheKey(config, path);

        return (boolean) valueCache.get(cacheKey, k -> config.getBoolean(path, defaultValue));
    }

    public List<String> getStringList(FileConfiguration config, String path) {
        String cacheKey = getCacheKey(config, path);

        return listCache.get(cacheKey, k -> config.getStringList(path));
    }

    public void invalidateAll() {
        sectionCache.invalidateAll();
        valueCache.invalidateAll();
        listCache.invalidateAll();

        sectionCache.cleanUp();
        valueCache.cleanUp();
        listCache.cleanUp();
    }

    public void invalidate(String configName) {
        String prefix = configName + ":";

        sectionCache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
        valueCache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
        listCache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
    }

    public String getStats() {
        return String.format(
                "ConfigCache Stats:\n" +
                        "  Sections: %d cached, %.2f%% hit rate\n" +
                        "  Values: %d cached, %.2f%% hit rate\n" +
                        "  Lists: %d cached, %.2f%% hit rate",
                sectionCache.estimatedSize(), sectionCache.stats().hitRate() * 100,
                valueCache.estimatedSize(), valueCache.stats().hitRate() * 100,
                listCache.estimatedSize(), listCache.stats().hitRate() * 100
        );
    }

    private String getCacheKey(FileConfiguration config, String path) {
        return config.getName() + ":" + path;
    }
}