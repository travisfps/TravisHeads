package com.travisheads.listeners;

import com.travisheads.TravisHeads;
import com.travisheads.models.PlayerHead;
import com.travisheads.models.Rarity;
import com.travisheads.utils.ItemBuilder;
import com.travisheads.utils.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class PlayerDeathListener implements Listener {

    private final TravisHeads plugin;
    private final Random random = new Random();

    public PlayerDeathListener(TravisHeads plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer.equals(victim)) return;

        double dropChance = plugin.getConfig().getDouble("settings.drop-chance", 50.0);
        if (random.nextDouble() * 100 > dropChance) return;

        Rarity rarity = plugin.getRarityManager().getRandomRarity();
        if (rarity == null) return;

        PlayerHead head = new PlayerHead(victim.getName(), rarity.getId(), System.currentTimeMillis());

        String dropMode = plugin.getConfig().getString("settings.drop-mode", "INVENTORY");

        if (dropMode.equalsIgnoreCase("INVENTORY")) {
            plugin.getHeadsManager().addHead(killer, head);

            String message = MessageUtil.getMessage("head-obtained")
                    .replace("%player%", victim.getName())
                    .replace("%rarity%", rarity.getDisplayName())
                    .replace("%rarity_color%", rarity.getColor());
            killer.sendMessage(message);

        } else if (dropMode.equalsIgnoreCase("DROP")) {
            ItemStack skull = createSkullItem(victim.getName(), rarity);
            victim.getWorld().dropItemNaturally(victim.getLocation(), skull);

        } else if (dropMode.equalsIgnoreCase("BOTH")) {
            plugin.getHeadsManager().addHead(killer, head);
            ItemStack skull = createSkullItem(victim.getName(), rarity);
            victim.getWorld().dropItemNaturally(victim.getLocation(), skull);

            String message = MessageUtil.getMessage("head-obtained")
                    .replace("%player%", victim.getName())
                    .replace("%rarity%", rarity.getDisplayName())
                    .replace("%rarity_color%", rarity.getColor());
            killer.sendMessage(message);
        }
    }

    private ItemStack createSkullItem(String playerName, Rarity rarity) {
        ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3);
        builder.setSkullOwner(playerName);

        String displayName = plugin.getConfig().getString("head.display-name", "&f%player% &8[%rarity_color%%rarity%&8]")
                .replace("%player%", playerName)
                .replace("%rarity%", rarity.getDisplayName())
                .replace("%rarity_color%", rarity.getColor());

        builder.setDisplayName(displayName);

        if (plugin.getConfig().contains("head.lore")) {
            for (String lore : plugin.getConfig().getStringList("head.lore")) {
                builder.addLore(lore
                        .replace("%player%", playerName)
                        .replace("%rarity%", rarity.getDisplayName())
                        .replace("%rarity_color%", rarity.getColor()));
            }
        }

        return builder.build();
    }
}