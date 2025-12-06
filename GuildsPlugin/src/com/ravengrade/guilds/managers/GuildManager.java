package com.ravengrade.guilds.managers;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.Guild;
import com.ravengrade.guilds.data.GuildClaim;
import com.ravengrade.guilds.data.GuildRank;
import com.ravengrade.guilds.data.InviteMode;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GuildManager {

    private final GuildsPlugin plugin;

    private final Map<String, Guild> guildsByName = new HashMap<>();
    private final Map<UUID, String> guildByPlayer = new HashMap<>();
    private final Map<UUID, String> pendingInvites = new HashMap<>();

    private final File guildFile;

    public GuildManager(GuildsPlugin plugin) {
        this.plugin = plugin;
        this.guildFile = new File(plugin.getDataFolder(), "guilds.yml");
        loadAll();
    }

    public void loadAll() {
        guildsByName.clear();
        guildByPlayer.clear();

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!guildFile.exists()) {
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(guildFile);
        ConfigurationSection root = yaml.getConfigurationSection("guilds");
        if (root == null) return;

        for (String name : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(name);
            if (sec == null) continue;

            String leaderStr = sec.getString("leader");
            if (leaderStr == null) continue;
            UUID leader = UUID.fromString(leaderStr);

            Guild guild = new Guild(name, leader);

            // members
            ConfigurationSection memSec = sec.getConfigurationSection("members");
            if (memSec != null) {
                for (String uuidStr : memSec.getKeys(false)) {
                    UUID id = UUID.fromString(uuidStr);
                    String rankName = memSec.getString(uuidStr, GuildRank.RECRUIT.name());
                    GuildRank rank;
                    try {
                        rank = GuildRank.valueOf(rankName);
                    } catch (IllegalArgumentException ex) {
                        rank = GuildRank.RECRUIT;
                    }
                    guild.setRank(id, rank);
                    guildByPlayer.put(id, name);
                }
            }

            // home
            if (sec.isConfigurationSection("home")) {
                ConfigurationSection h = sec.getConfigurationSection("home");
                String world = h.getString("world");
                double x = h.getDouble("x");
                double y = h.getDouble("y");
                double z = h.getDouble("z");
                float yaw = (float) h.getDouble("yaw");
                float pitch = (float) h.getDouble("pitch");
                if (world != null && Bukkit.getWorld(world) != null) {
                    Location loc = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                    guild.setHome(loc);
                }
            }

            // motd
            guild.setMotd(sec.getString("motd", ""));

            // invite mode
            String modeName = sec.getString("invite-mode", InviteMode.INVITE_ONLY.name());
            InviteMode mode;
            try {
                mode = InviteMode.valueOf(modeName);
            } catch (IllegalArgumentException ex) {
                mode = InviteMode.INVITE_ONLY;
            }
            guild.setInviteMode(mode);

            // logs
            // (Optional: can load logs from list of strings later if needed)

            guildsByName.put(name.toLowerCase(Locale.ROOT), guild);
        }
    }

    public void saveAll() {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection root = yaml.createSection("guilds");

        for (Guild guild : guildsByName.values()) {
            ConfigurationSection sec = root.createSection(guild.getName());
            sec.set("leader", guild.getLeader().toString());

            ConfigurationSection memSec = sec.createSection("members");
            for (Map.Entry<UUID, GuildRank> e : guild.getMembers().entrySet()) {
                memSec.set(e.getKey().toString(), e.getValue().name());
            }

            if (guild.getHome() != null && guild.getHome().getWorld() != null) {
                ConfigurationSection h = sec.createSection("home");
                h.set("world", guild.getHome().getWorld().getName());
                h.set("x", guild.getHome().getX());
                h.set("y", guild.getHome().getY());
                h.set("z", guild.getHome().getZ());
                h.set("yaw", guild.getHome().getYaw());
                h.set("pitch", guild.getHome().getPitch());
            }

            sec.set("motd", guild.getMotd());
            sec.set("invite-mode", guild.getInviteMode().name());
        }

        try {
            yaml.save(guildFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Guild createGuild(String name, UUID leader) {
        Guild existing = getGuild(name);
        if (existing != null) return null;

        Guild guild = new Guild(name, leader);
        guildsByName.put(name.toLowerCase(Locale.ROOT), guild);

        for (UUID uuid : guild.getMembers().keySet()) {
            guildByPlayer.put(uuid, name);
        }

        guild.log("Guild created by " + leader);
        return guild;
    }

    public Guild getGuild(String name) {
        if (name == null) return null;
        return guildsByName.get(name.toLowerCase(Locale.ROOT));
    }

    public Guild getGuildByPlayer(UUID uuid) {
        String name = guildByPlayer.get(uuid);
        if (name == null) return null;
        return getGuild(name);
    }

    public boolean isInGuild(UUID uuid) {
        return guildByPlayer.containsKey(uuid);
    }

    public Collection<Guild> getAllGuilds() {
        return guildsByName.values();
    }

    public void addMember(Guild guild, UUID uuid) {
        guild.addMember(uuid);
        guildByPlayer.put(uuid, guild.getName());
        guild.log("Player " + uuid + " joined the guild.");
    }

    public void removeMember(Guild guild, UUID uuid) {
        guild.removeMember(uuid);
        guildByPlayer.remove(uuid);
        guild.log("Player " + uuid + " left the guild.");
    }

    public void disbandGuild(Guild guild, ClaimManager claimManager) {
        // remove claims
        for (GuildClaim claim : new ArrayList<>(guild.getClaims())) {
            claimManager.unclaimChunk(claim.getWorld(), claim.getChunkX(), claim.getChunkZ());
        }

        // remove members
        for (UUID member : new ArrayList<>(guild.getMembers().keySet())) {
            guildByPlayer.remove(member);
        }

        guildsByName.remove(guild.getName().toLowerCase(Locale.ROOT));
    }

    public void setPendingInvite(UUID player, String guildName) {
        pendingInvites.put(player, guildName);
    }

    public String consumePendingInvite(UUID player) {
        return pendingInvites.remove(player);
    }
}
