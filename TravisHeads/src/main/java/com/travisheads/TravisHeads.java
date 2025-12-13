package com.travisheads;

import com.travisheads.commands.HeadsCommand;
import com.travisheads.listeners.PlayerDeathListener;
import com.travisheads.managers.*;
import com.travisheads.placeholder.StyleHeadsPlaceholder;
import com.travisheads.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TravisHeads extends JavaPlugin {

    private ConfigManager configManager;
    private ConfigCache configCache;
    private RarityManager rarityManager;
    private HeadsManager headsManager;
    private GUIManager guiManager;
    private HeadsCache headsCache;

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
            new StyleHeadsPlaceholder(this).register();
            getLogger().info("PlaceholderAPI detectado! Placeholders registrados.");
        }

        getLogger().info("StyleHeads iniciado com sucesso!");
    }

    @Override
    public void onDisable() {
        if (guiManager != null) {
            guiManager.closeAll();
        }

        if (headsManager != null) {
            headsManager.saveAll();
            headsManager.closeConnection();
        }

        getLogger().info("StyleHeads desativado!");
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