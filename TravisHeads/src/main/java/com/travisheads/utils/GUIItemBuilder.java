package com.travisheads.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class GUIItemBuilder {

    public static ItemStack createItem(ConfigurationSection section, Player player, Material defaultMaterial, String defaultName) {
        return createItem(section, player, defaultMaterial, defaultName, new HashMap<>());
    }

    public static ItemStack createItem(ConfigurationSection section, Player player, Material defaultMaterial, String defaultName, Map<String, String> placeholders) {
        if (section == null) {
            return new ItemBuilder(defaultMaterial, 1, (short) 0)
                    .setDisplayName(defaultName)
                    .build();
        }

        String type = section.getString("type", "ITEM");
        ItemBuilder builder;

        if (type.equalsIgnoreCase("SKULL")) {
            builder = new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3);
            applySkullTexture(builder, section, player);
        } else {
            String materialName = section.getString("material", defaultMaterial.name());
            Material material = Material.getMaterial(materialName);
            
            if (material == null) {
                material = defaultMaterial;
            }
            
            builder = new ItemBuilder(
                    material,
                    Math.max(1, section.getInt("amount", 1)),
                    (short) section.getInt("data", 0)
            );
        }

        String displayName = section.getString("display-name", defaultName);
        displayName = replacePlaceholders(displayName, player, placeholders);
        builder.setDisplayName(displayName);

        if (section.contains("lore")) {
            for (String lore : section.getStringList("lore")) {
                if (lore != null) {
                    lore = replacePlaceholders(lore, player, placeholders);
                    builder.addLore(lore);
                }
            }
        }

        return builder.build();
    }

    private static void applySkullTexture(ItemBuilder builder, ConfigurationSection section, Player player) {
        if (section == null) return;

        if (section.getBoolean("custom-skull", false)) {
            String url = section.getString("skull-url", "");
            if (!url.isEmpty()) {
                builder.setSkullTexture(url);
            }
        } else {
            String skullOwner = section.getString("skull-owner", "");
            if (!skullOwner.isEmpty()) {
                if (skullOwner.equals("%player%") && player != null) {
                    builder.setSkullOwner(player.getName());
                } else {
                    builder.setSkullOwner(skullOwner);
                }
            } else if (player != null) {
                builder.setSkullOwner(player.getName());
            }
        }
    }

    private static String replacePlaceholders(String text, Player player, Map<String, String> customPlaceholders) {
        if (text == null) return "";

        if (customPlaceholders != null) {
            for (Map.Entry<String, String> entry : customPlaceholders.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    text = text.replace(entry.getKey(), entry.getValue());
                }
            }
        }

        if (player != null) {
            text = text.replace("%player%", player.getName());
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && player != null) {
            try {
                text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
            } catch (Exception e) {
            }
        }

        return text != null ? text : "";
    }
}
