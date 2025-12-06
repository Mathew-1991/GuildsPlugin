package com.ravengrade.guilds.listeners;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.Guild;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Locale;

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
            player.setPlayerListName(player.getName());
            return;
        }

        ChatColor color = getGuildTagColor(guild.getName());
        String name = color + "[" + guild.getName() + "] " + ChatColor.RESET + player.getName();
        player.setPlayerListName(name);
    }

    private ChatColor getGuildTagColor(String guildName) {
        String path = "guilds-extra." + guildName + ".tag-color";
        String def = "YELLOW";
        String raw = plugin.getConfig().getString(path, def);
        if (raw == null) raw = def;
        try {
            return ChatColor.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ChatColor.YELLOW;
        }
    }

    public void refreshAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            updateTabName(p);
        }
    }
}
