package com.ravengrade.guilds.listeners;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.Guild;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatListener implements Listener {

    // Players with guild chat toggled on
    private static final Set<UUID> guildChatToggled =
            Collections.synchronizedSet(new HashSet<>());

    private final GuildsPlugin plugin;

    public ChatListener(GuildsPlugin plugin) {
        this.plugin = plugin;
    }

    public static boolean isGuildChatToggled(UUID uuid) {
        return guildChatToggled.contains(uuid);
    }

    public static void setGuildChatToggled(UUID uuid, boolean value) {
        if (value) {
            guildChatToggled.add(uuid);
        } else {
            guildChatToggled.remove(uuid);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Guild guild = plugin.getGuildManager().getGuildByPlayer(uuid);

        // ----------------- GUILD-ONLY CHAT -----------------
        if (guild != null && isGuildChatToggled(uuid)) {
            event.setCancelled(true);

            ChatColor color = plugin.getGuildTagColor(guild);
            String msg = ChatColor.GRAY + "(Guild) "
                    + color + "[" + guild.getName() + "] "
                    + ChatColor.RESET + player.getName()
                    + ChatColor.GRAY + ": "
                    + ChatColor.WHITE + event.getMessage();

            for (UUID memberId : guild.getMembers().keySet()) {
                Player target = Bukkit.getPlayer(memberId);
                if (target != null) {
                    target.sendMessage(msg);
                }
            }
            return;
        }

        // ----------------- NORMAL PUBLIC CHAT WITH GUILD TAG -----------------
        if (guild != null) {
            ChatColor color = plugin.getGuildTagColor(guild);
            String tag = color + "[" + guild.getName() + "] " + ChatColor.RESET;
            // prepend tag to normal format
            event.setFormat(tag + "%s: %s");
        }
        // if guild == null, default Bukkit format is used
    }
}
