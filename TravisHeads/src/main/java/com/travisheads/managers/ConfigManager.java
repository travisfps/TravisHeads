package com.travisheads.managers;

import com.travisheads.TravisHeads;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {

    private final TravisHeads plugin;
    private FileConfiguration guiConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration raritiesConfig;
    private FileConfiguration exchangesConfig;

    private File guiFile;
    private File messagesFile;
    private File raritiesFile;
    private File exchangesFile;

    public ConfigManager(TravisHeads plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();

        createFolders();

        guiFile = new File(plugin.getDataFolder(), "menus/gui.yml");
        if (!guiFile.exists()) {
            plugin.saveResource("menus/gui.yml", false);
        }
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);

        messagesFile = new File(plugin.getDataFolder(), "mensagens/messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("mensagens/messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        raritiesFile = new File(plugin.getDataFolder(), "heads/rarities.yml");
        if (!raritiesFile.exists()) {
            plugin.saveResource("heads/rarities.yml", false);
        }
        raritiesConfig = YamlConfiguration.loadConfiguration(raritiesFile);

        exchangesFile = new File(plugin.getDataFolder(), "heads/trocas.yml");
        if (!exchangesFile.exists()) {
            plugin.saveResource("heads/trocas.yml", false);
        }
        exchangesConfig = YamlConfiguration.loadConfiguration(exchangesFile);
    }

    private void createFolders() {
        File menusFolder = new File(plugin.getDataFolder(), "menus");
        File mensagensFolder = new File(plugin.getDataFolder(), "mensagens");
        File headsFolder = new File(plugin.getDataFolder(), "heads");

        if (!menusFolder.exists() && !menusFolder.mkdirs()) {
            plugin.getLogger().severe("Erro ao criar pasta: menus/");
        }
        if (!mensagensFolder.exists() && !mensagensFolder.mkdirs()) {
            plugin.getLogger().severe("Erro ao criar pasta: mensagens/");
        }
        if (!headsFolder.exists() && !headsFolder.mkdirs()) {
            plugin.getLogger().severe("Erro ao criar pasta: heads/");
        }
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        raritiesConfig = YamlConfiguration.loadConfiguration(raritiesFile);
        exchangesConfig = YamlConfiguration.loadConfiguration(exchangesFile);
    }

    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public FileConfiguration getRaritiesConfig() {
        return raritiesConfig;
    }

    public FileConfiguration getExchangesConfig() {
        return exchangesConfig;
    }
}