package com.travisheads.placeholder;

import com.travisheads.TravisHeads;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StyleHeadsPlaceholder extends PlaceholderExpansion {

    private final TravisHeads plugin;

    public StyleHeadsPlaceholder(TravisHeads plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "styleheads";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    @Nullable
    public String onPlaceholderRequest(@Nullable Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("heads") || identifier.equals("total")) {
            return String.valueOf(plugin.getHeadsManager().getTotalHeads(player));
        }

        if (identifier.startsWith("rarity_")) {
            String rarityId = identifier.substring(7);
            int count = plugin.getHeadsManager().getHeadsByRarity(player)
                    .getOrDefault(rarityId, 0);
            return String.valueOf(count);
        }

        int count = plugin.getHeadsManager().getHeadsByRarity(player)
                .getOrDefault(identifier, 0);
        if (count > 0 || plugin.getRarityManager().getRarity(identifier) != null) {
            return String.valueOf(count);
        }

        return null;
    }
}