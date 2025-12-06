package com.ravengrade.guilds.listeners;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.Guild;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TablistListener implements Listener {

    private final GuildsPlugin plugin;

    public TablistListener(GuildsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        updateTabName(event.getPlayer());
    }

    public void updateTabName(Player player) {
        Guild guild = plugin.getGuildManager().getGuildByPlayer(player.getUniqueId());

        if (guild == null) {
            // No guild → just show normal name
            player.setPlayerListName(player.getName());
            return;
        }

        ChatColor color = plugin.getGuildTagColor(guild);
        String name = color + "[" + guild.getName() + "] "
                + ChatColor.RESET + player.getName();

        player.setPlayerListName(name);
    }

    public void refreshAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            updateTabName(p);
        }
    }
}
