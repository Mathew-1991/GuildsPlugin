package com.ravengrade.guilds.listeners;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.Guild;
import com.ravengrade.guilds.data.GuildClaim;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ClaimProtectionListener implements Listener {

    private final GuildsPlugin plugin;

    public ClaimProtectionListener(GuildsPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean canBuildHere(Player player, Chunk chunk) {
        GuildClaim claim = plugin.getClaimManager().getClaim(chunk);
        if (claim == null) return true; // wilderness

        Guild owner = plugin.getGuildManager().getGuild(claim.getGuildName());
        if (owner == null) return true;

        Guild playerGuild = plugin.getGuildManager().getGuildByPlayer(player.getUniqueId());
        if (playerGuild == null) return false;

        return playerGuild.getName().equalsIgnoreCase(owner.getName());
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();

        if (!canBuildHere(p, chunk)) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.RED + "You cannot build in this guild's territory.");
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();

        if (!canBuildHere(p, chunk)) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.RED + "You cannot break blocks in this guild's territory.");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Player p = event.getPlayer();
        Chunk chunk = event.getClickedBlock().getChunk();

        if (!canBuildHere(p, chunk)) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.RED + "You cannot interact with blocks in this guild's territory.");
        }
    }
}
