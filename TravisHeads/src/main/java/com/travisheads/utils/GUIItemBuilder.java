package com.travisheads.utils;

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
        String type = section.getString("type", "ITEM");
        ItemBuilder builder;

        if (type.equalsIgnoreCase("SKULL")) {
            builder = new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3);
            applySkullTexture(builder, section, player);
        } else {
            Material material = Material.getMaterial(section.getString("material", defaultMaterial.name()));
            builder = new ItemBuilder(
                    material != null ? material : defaultMaterial,
                    1,
                    (short) section.getInt("data", 0)
            );
        }

        String displayName = section.getString("display-name", defaultName);
        displayName = replacePlaceholders(displayName, player, placeholders);
        builder.setDisplayName(displayName);

        if (section.contains("lore")) {
            for (String lore : section.getStringList("lore")) {
                lore = replacePlaceholders(lore, player, placeholders);
                builder.addLore(lore);
            }
        }

        return builder.build();
    }

    private static void applySkullTexture(ItemBuilder builder, ConfigurationSection section, Player player) {
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
            }
        }
    }

    private static String replacePlaceholders(String text, Player player, Map<String, String> customPlaceholders) {
        if (player != null) {
            text = text.replace("%player%", player.getName());
        }

        for (Map.Entry<String, String> entry : customPlaceholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }

        return text;
    }
}