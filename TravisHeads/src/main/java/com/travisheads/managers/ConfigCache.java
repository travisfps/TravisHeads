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
                .weakValues()
                .build();

        this.valueCache = Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .maximumSize(1000)
                .weakKeys()
                .weakValues()
                .build();

        this.listCache = Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .maximumSize(300)
                .weakKeys()
                .weakValues()
                .build();
    }

    public ConfigurationSection getSection(FileConfiguration config, String path) {
        if (config == null || path == null) return null;
        
        String cacheKey = getCacheKey(config, path);
        return sectionCache.get(cacheKey, k -> config.getConfigurationSection(path));
    }

    public String getString(FileConfiguration config, String path, String defaultValue) {
        if (config == null || path == null) return defaultValue;
        
        String cacheKey = getCacheKey(config, path);
        return (String) valueCache.get(cacheKey, k -> {
            String value = config.getString(path);
            return value != null ? value : defaultValue;
        });
    }

    public int getInt(FileConfiguration config, String path, int defaultValue) {
        if (config == null || path == null) return defaultValue;
        
        String cacheKey = getCacheKey(config, path);
        Object cached = valueCache.getIfPresent(cacheKey);
        
        if (cached instanceof Integer) {
            return (Integer) cached;
        }
        
        int value = config.getInt(path, defaultValue);
        valueCache.put(cacheKey, value);
        return value;
    }

    public double getDouble(FileConfiguration config, String path, double defaultValue) {
        if (config == null || path == null) return defaultValue;
        
        String cacheKey = getCacheKey(config, path);
        Object cached = valueCache.getIfPresent(cacheKey);
        
        if (cached instanceof Double) {
            return (Double) cached;
        }
        
        double value = config.getDouble(path, defaultValue);
        valueCache.put(cacheKey, value);
        return value;
    }

    public boolean getBoolean(FileConfiguration config, String path, boolean defaultValue) {
        if (config == null || path == null) return defaultValue;
        
        String cacheKey = getCacheKey(config, path);
        Object cached = valueCache.getIfPresent(cacheKey);
        
        if (cached instanceof Boolean) {
            return (Boolean) cached;
        }
        
        boolean value = config.getBoolean(path, defaultValue);
        valueCache.put(cacheKey, value);
        return value;
    }

    public List<String> getStringList(FileConfiguration config, String path) {
        if (config == null || path == null) return null;
        
        String cacheKey = getCacheKey(config, path);
        return listCache.get(cacheKey, k -> config.getStringList(path));
    }

    public void invalidateAll() {
        sectionCache.invalidateAll();
        valueCache.invalidateAll();
        listCache.invalidateAll();
        cleanUp();
    }

    public void invalidate(String configName) {
        if (configName == null) return;
        
        String prefix = configName + ":";
        sectionCache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
        valueCache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
        listCache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
    }

    public void cleanUp() {
        sectionCache.cleanUp();
        valueCache.cleanUp();
        listCache.cleanUp();
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
