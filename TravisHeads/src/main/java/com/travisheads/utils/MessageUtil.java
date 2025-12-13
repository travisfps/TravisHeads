package com.travisheads.utils;

import com.travisheads.TravisHeads;
import org.bukkit.ChatColor;

public class MessageUtil {

    private static TravisHeads plugin;

    public static void setPlugin(TravisHeads pl) {
        plugin = pl;
    }

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String getMessage(String path) {
        if (plugin == null) {
            return color("&cErro ao carregar mensagem!");
        }

        String message = plugin.getConfigManager().getMessagesConfig().getString("messages." + path);

        if (message == null) {
            return color("&cMensagem não encontrada: " + path);
        }

        String prefix = plugin.getConfigManager().getMessagesConfig().getString("messages.prefix", "&8[&6StyleHeads&8] &7");

        return color(prefix + message);
    }
}