package com.travisheads.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private ItemMeta meta;

    public ItemBuilder(Material material, int amount, short data) {
        if (material == null) {
            material = Material.STONE;
        }
        
        this.item = new ItemStack(material, Math.max(1, amount), data);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder setDisplayName(String name) {
        if (meta != null && name != null) {
            meta.setDisplayName(MessageUtil.color(name));
        }
        return this;
    }

    public ItemBuilder addLore(String line) {
        if (meta != null && line != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add(MessageUtil.color(line));
            meta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        if (meta != null && lore != null) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                if (line != null) {
                    coloredLore.add(MessageUtil.color(line));
                }
            }
            meta.setLore(coloredLore);
        }
        return this;
    }

    public ItemBuilder setSkullOwner(String owner) {
        if (meta instanceof SkullMeta && owner != null && !owner.isEmpty()) {
            try {
                ((SkullMeta) meta).setOwner(owner);
            } catch (Exception e) {
            }
        }
        return this;
    }

    public ItemBuilder setSkullTexture(String url) {
        if (meta instanceof SkullMeta && url != null && !url.isEmpty()) {
            try {
                SkullUtils.applySkullTexture((SkullMeta) meta, url);
            } catch (Exception e) {
            }
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            try {
                item.setItemMeta(meta);
            } catch (Exception e) {
            }
        }
        return item;
    }
}
