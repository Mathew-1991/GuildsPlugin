package com.ravengrade.guilds;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.ravengrade.guilds.managers.GuildManager;
import com.ravengrade.guilds.managers.ClaimManager;
import com.ravengrade.guilds.managers.LogManager;
import com.ravengrade.guilds.managers.GUIManager;

import com.ravengrade.guilds.commands.GuildCommand;
import com.ravengrade.guilds.commands.GuildAdminCommand;

import com.ravengrade.guilds.listeners.ChatListener;
import com.ravengrade.guilds.listeners.TablistListener;
import com.ravengrade.guilds.listeners.HomeTeleportListener;
import com.ravengrade.guilds.listeners.ClaimProtectionListener;
import com.ravengrade.guilds.listeners.ChunkEnterListener;
import com.ravengrade.guilds.listeners.GuiClickListener;

public class GuildsPlugin extends JavaPlugin {

    private static GuildsPlugin instance;

    private GuildManager guildManager;
    private ClaimManager claimManager;
    private LogManager logManager;
    private GUIManager guiManager;

    private TablistListener tablistListener;

    public static GuildsPlugin get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getLogger().info("Loading managers...");
        guildManager = new GuildManager(this);
        claimManager = new ClaimManager(this);
        logManager = new LogManager(this);
        guiManager = new GUIManager(this);

        getLogger().info("Registering commands...");
        GuildCommand guildCmd = new GuildCommand(this);
        getCommand("guild").setExecutor(guildCmd);
        getCommand("guild").setTabCompleter(guildCmd);

        GuildAdminCommand adminCmd = new GuildAdminCommand(this);
        getCommand("gadmin").setExecutor(adminCmd);
        getCommand("gadmin").setTabCompleter(adminCmd);

        getLogger().info("Registering listeners...");
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);

        tablistListener = new TablistListener(this);
        Bukkit.getPluginManager().registerEvents(tablistListener, this);

        Bukkit.getPluginManager().registerEvents(new HomeTeleportListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ClaimProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChunkEnterListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GuiClickListener(this), this);

        // Ensure tab list is correct for players already online (on /reload etc.)
        for (Player p : Bukkit.getOnlinePlayers()) {
            tablistListener.updateTabName(p);
        }

        getLogger().info("[GuildsPlugin] Enabled successfully!");
    }

    @Override
    public void onDisable() {
        guildManager.saveAll();
        claimManager.saveAll();
        logManager.saveAll();

        getLogger().info("[GuildsPlugin] Disabled and saved all data.");
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    public ClaimManager getClaimManager() {
        return claimManager;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public TablistListener getTablistListener() {
        return tablistListener;
    }
}
