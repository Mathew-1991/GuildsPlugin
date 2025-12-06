package com.ravengrade.guilds.managers;

import com.ravengrade.guilds.data.Guild;
import com.ravengrade.guilds.data.GuildLogEntry;

import java.util.List;

public class LogManager {

    // Currently logs are stored directly inside each Guild object.
    // This manager exists to keep API clean and can be extended later (e.g. file logs).

    private final com.ravengrade.guilds.GuildsPlugin plugin;

    public LogManager(com.ravengrade.guilds.GuildsPlugin plugin) {
        this.plugin = plugin;
    }

    public void log(Guild guild, String message) {
        if (guild == null) return;
        guild.log(message);
    }

    public List<GuildLogEntry> getLogs(Guild guild) {
        if (guild == null) return null;
        return guild.getLogs();
    }

    public void saveAll() {
        // Logs are saved as part of GuildManager.saveAll() if we choose
        // to serialize them. Right now, we keep them in memory only.
    }
}
