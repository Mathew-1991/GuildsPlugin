package com.ravengrade.guilds.listeners;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.GuildClaim;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ChunkEnterListener implements Listener {

    private final GuildsPlugin plugin;

    public ChunkEnterListener(GuildsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;

        Player player = event.getPlayer();
        Chunk from = event.getFrom().getChunk();
        Chunk to = event.getTo().getChunk();

        // Only when chunk actually changes
        if (from.getX() == to.getX()
                && from.getZ() == to.getZ()
                && from.getWorld().equals(to.getWorld())) {
            return;
        }

        if (!plugin.getConfig().getBoolean("titles.show-chunk-entry", true)) return;

        GuildClaim claimFrom = plugin.getClaimManager().getClaim(from);
        GuildClaim claimTo   = plugin.getClaimManager().getClaim(to);

        // Both wilderness -> no message
        if (claimFrom == null && claimTo == null) {
            return;
        }

        // Same guild -> no message
        if (claimFrom != null && claimTo != null &&
                claimFrom.getGuildName().equalsIgnoreCase(claimTo.getGuildName())) {
            return;
        }

        // Only when territory owner actually changes
        if (claimTo == null) {
            player.sendMessage(ChatColor.GRAY + "Wilderness");
        } else {
            player.sendMessage(ChatColor.GOLD + "Territory: " + ChatColor.YELLOW + claimTo.getGuildName());
        }
    }
}
