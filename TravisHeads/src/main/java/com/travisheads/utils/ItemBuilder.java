package com.travisheads.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material, int amount, short data) {
        this.item = new ItemStack(material, amount, data);
        this.meta = item.getItemMeta();
    }

    public void setDisplayName(String name) {
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color(name));
        }
    }

    public void addLore(String line) {
        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add(MessageUtil.color(line));
            meta.setLore(lore);
        }
    }

    public void setSkullOwner(String owner) {
        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwner(owner);
        }
    }

    public void setSkullTexture(String url) {
        if (meta instanceof SkullMeta && url != null && !url.isEmpty()) {
            SkullUtils.applySkullTexture((SkullMeta) meta, url);
        }
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
}