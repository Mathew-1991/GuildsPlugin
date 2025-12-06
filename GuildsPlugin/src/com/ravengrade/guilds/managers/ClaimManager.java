package com.ravengrade.guilds.managers;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.Guild;
import com.ravengrade.guilds.data.GuildClaim;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClaimManager {

    private final GuildsPlugin plugin;

    private final Map<String, GuildClaim> claimsByKey = new HashMap<>();
    private final File claimFile;

    public ClaimManager(GuildsPlugin plugin) {
        this.plugin = plugin;
        this.claimFile = new File(plugin.getDataFolder(), "claims.yml");
        loadAll();
    }

    public void loadAll() {
        claimsByKey.clear();

        if (!claimFile.exists()) return;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(claimFile);
        ConfigurationSection root = yaml.getConfigurationSection("claims");
        if (root == null) return;

        for (String key : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(key);
            if (sec == null) continue;

            String guildName = sec.getString("guild");
            String world = sec.getString("world");
            int cx = sec.getInt("x");
            int cz = sec.getInt("z");

            if (guildName == null || world == null) continue;

            World w = Bukkit.getWorld(world);
            if (w == null) continue;

            GuildClaim claim = new GuildClaim(guildName, w, cx, cz);
            claimsByKey.put(claim.getUniqueKey(), claim);
        }
    }

    public void saveAll() {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection root = yaml.createSection("claims");

        for (GuildClaim claim : claimsByKey.values()) {
            ConfigurationSection sec = root.createSection(claim.getUniqueKey());
            sec.set("guild", claim.getGuildName());
            sec.set("world", claim.getWorld());
            sec.set("x", claim.getChunkX());
            sec.set("z", claim.getChunkZ());
        }

        try {
            yaml.save(claimFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GuildClaim getClaim(Chunk chunk) {
        String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        return claimsByKey.get(key);
    }

    public GuildClaim getClaim(String world, int x, int z) {
        String key = world + ":" + x + ":" + z;
        return claimsByKey.get(key);
    }

    public boolean isClaimed(Chunk chunk) {
        return getClaim(chunk) != null;
    }

    public void claimChunk(Guild guild, Chunk chunk) {
        GuildClaim claim = new GuildClaim(guild.getName(), chunk.getWorld(), chunk.getX(), chunk.getZ());
        claimsByKey.put(claim.getUniqueKey(), claim);
        guild.addClaim(claim);
        guild.log("Claimed chunk " + chunk.getX() + "," + chunk.getZ() + " in " + chunk.getWorld().getName());
    }

    public void unclaimChunk(String world, int x, int z) {
        GuildClaim claim = getClaim(world, x, z);
        if (claim == null) return;

        claimsByKey.remove(claim.getUniqueKey());

        Guild guild = plugin.getGuildManager().getGuild(claim.getGuildName());
        if (guild != null) {
            guild.removeClaim(claim);
            guild.log("Unclaimed chunk " + x + "," + z + " in " + world);
        }
    }

    public List<GuildClaim> getClaimsForGuild(String guildName) {
        List<GuildClaim> list = new ArrayList<>();
        for (GuildClaim claim : claimsByKey.values()) {
            if (claim.getGuildName().equalsIgnoreCase(guildName)) {
                list.add(claim);
            }
        }
        return list;
    }
}
