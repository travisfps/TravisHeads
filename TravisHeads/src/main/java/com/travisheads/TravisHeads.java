package com.travisheads;

import com.travisheads.commands.HeadsCommand;
import com.travisheads.listeners.PlayerDeathListener;
import com.travisheads.managers.*;
import com.travisheads.placeholder.TravisHeadsPlaceholder;
import com.travisheads.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class TravisHeads extends JavaPlugin {

    private ConfigManager configManager;
    private ConfigCache configCache;
    private RarityManager rarityManager;
    private HeadsManager headsManager;
    private GUIManager guiManager;
    private HeadsCache headsCache;
    
    private BukkitTask cacheCleanupTask;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        configManager.loadConfigs();

        MessageUtil.setPlugin(this);

        this.configCache = new ConfigCache(configManager);
        this.rarityManager = new RarityManager(this);
        this.headsManager = new HeadsManager(this);
        this.headsCache = new HeadsCache(this);
        this.guiManager = new GUIManager(this);

        getCommand("heads").setExecutor(new HeadsCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TravisHeadsPlaceholder(this).register();
            getLogger().info("PlaceholderAPI detectado! Placeholders registrados.");
        }

        startCacheCleanupTask();

        getLogger().info("TravisHeads iniciado com sucesso!");
    }

    @Override
    public void onDisable() {
        if (cacheCleanupTask != null) {
            cacheCleanupTask.cancel();
        }

        if (guiManager != null) {
            guiManager.closeAll();
        }

        if (headsManager != null) {
            headsManager.saveAll();
            headsManager.closeConnection();
        }

        if (headsCache != null) {
            headsCache.invalidateAll();
        }
        
        if (configCache != null) {
            configCache.invalidateAll();
        }

        getLogger().info("TravisHeads desativado!");
    }

    private void startCacheCleanupTask() {
        cacheCleanupTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (headsCache != null) {
                headsCache.cleanUp();
            }
            if (configCache != null) {
                configCache.cleanUp();
            }
            
            getLogger().fine("Cache cleanup executado. Tamanho atual: " + 
                headsCache.getEstimatedSize() + " entradas");
        }, 6000L, 6000L);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ConfigCache getConfigCache() {
        return configCache;
    }

    public RarityManager getRarityManager() {
        return rarityManager;
    }

    public HeadsManager getHeadsManager() {
        return headsManager;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public HeadsCache getHeadsCache() {
        return headsCache;
    }
}
