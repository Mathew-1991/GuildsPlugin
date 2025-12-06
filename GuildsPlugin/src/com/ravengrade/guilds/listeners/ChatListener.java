package com.ravengrade.guilds.listeners;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.Guild;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class ChatListener implements Listener {

    private static final Set<UUID> guildChatToggled = Collections.synchronizedSet(new HashSet<>());

    private final GuildsPlugin plugin;

    public ChatListener(GuildsPlugin plugin) {
        this.plugin = plugin;
    }

    public static boolean isGuildChatToggled(UUID uuid) {
        return guildChatToggled.contains(uuid);
    }

    public static void setGuildChatToggled(UUID uuid, boolean value) {
        if (value) guildChatToggled.add(uuid); else guildChatToggled.remove(uuid);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Guild guild = plugin.getGuildManager().getGuildByPlayer(uuid);

        if (guild != null && isGuildChatToggled(uuid)) {
            // Guild-only chat
            event.setCancelled(true);

            ChatColor color = getGuildTagColor(guild.getName());
            String msg = ChatColor.GRAY + "(Guild) "
                    + color + "[" + guild.getName() + "] "
                    + ChatColor.RESET + player.getName()
                    + ChatColor.GRAY + ": "
                    + ChatColor.WHITE + event.getMessage();

            for (UUID memberId : guild.getMembers().keySet()) {
                Player target = Bukkit.getPlayer(memberId);
                if (target != null) target.sendMessage(msg);
            }
            return;
        }

        // Normal public chat with guild tag (if any)
        if (guild != null) {
            ChatColor color = getGuildTagColor(guild.getName());
            String tag = color + "[" + guild.getName() + "] " + ChatColor.RESET;
            event.setFormat(tag + "%s: %s");
        }
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
}
