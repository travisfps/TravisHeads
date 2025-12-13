package com.travisheads.commands;

import com.travisheads.TravisHeads;
import com.travisheads.gui.MainMenuGUI;
import com.travisheads.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HeadsCommand implements CommandExecutor {

    private final TravisHeads plugin;

    public HeadsCommand(TravisHeads plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.getMessage("only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("styleheads.admin")) {
                player.sendMessage(MessageUtil.getMessage("no-permission"));
                return true;
            }

            plugin.getConfigManager().reloadConfigs();
            plugin.getRarityManager().loadRarities();

            plugin.getHeadsCache().invalidateAll();
            plugin.getConfigCache().invalidateAll();

            player.sendMessage(MessageUtil.getMessage("reload-success"));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("stats")) {
            if (!player.hasPermission("styleheads.admin")) {
                player.sendMessage(MessageUtil.getMessage("no-permission"));
                return true;
            }

            player.sendMessage(MessageUtil.color("&6&m                    &r &e&lCache Stats &6&m                    "));
            player.sendMessage("");

            String[] headsStats = plugin.getHeadsCache().getStats().split("\n");
            for (String line : headsStats) {
                player.sendMessage(MessageUtil.color("&7" + line));
            }

            player.sendMessage("");

            String[] configStats = plugin.getConfigCache().getStats().split("\n");
            for (String line : configStats) {
                player.sendMessage(MessageUtil.color("&7" + line));
            }

            player.sendMessage("");

            String[] rarityStats = plugin.getRarityManager().getStats().split("\n");
            for (String line : rarityStats) {
                player.sendMessage(MessageUtil.color("&7" + line));
            }

            player.sendMessage(MessageUtil.color("&6&m                                                                   "));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("cache")) {
            if (!player.hasPermission("styleheads.admin")) {
                player.sendMessage(MessageUtil.getMessage("no-permission"));
                return true;
            }

            if (args.length > 1 && args[1].equalsIgnoreCase("clear")) {
                plugin.getHeadsCache().invalidateAll();
                plugin.getConfigCache().invalidateAll();
                player.sendMessage(MessageUtil.color("&aCache limpo com sucesso!"));
                return true;
            }

            if (args.length > 1 && args[1].equalsIgnoreCase("cleanup")) {
                plugin.getHeadsCache().cleanUp();
                player.sendMessage(MessageUtil.color("&aLimpeza de cache expirado realizada!"));
                return true;
            }

            player.sendMessage(MessageUtil.color("&cUso: /heads cache <clear|cleanup>"));
            return true;
        }

        new MainMenuGUI(plugin, player).open();
        return true;
    }
}