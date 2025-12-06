package com.ravengrade.guilds.data;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Guild {

    private final String name;
    private UUID leader;

    private Map<UUID, GuildRank> members = new HashMap<>();
    private List<GuildClaim> claims = new ArrayList<>();
    private List<GuildLogEntry> logs = new ArrayList<>();

    private String motd = "";
    private InviteMode inviteMode = InviteMode.INVITE_ONLY;

    private Location home = null;

    public Guild(String name, UUID leaderUUID) {
        this.name = name;
        this.leader = leaderUUID;
        members.put(leaderUUID, GuildRank.LEADER);
    }

    public String getName() {
        return name;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID uuid) {
        leader = uuid;
    }

    public Map<UUID, GuildRank> getMembers() {
        return members;
    }

    public GuildRank getRank(UUID uuid) {
        return members.get(uuid);
    }

    public void setRank(UUID uuid, GuildRank rank) {
        members.put(uuid, rank);
    }

    public void addMember(UUID uuid) {
        members.put(uuid, GuildRank.RECRUIT);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public List<GuildClaim> getClaims() {
        return claims;
    }

    public void addClaim(GuildClaim claim) {
        claims.add(claim);
    }

    public void removeClaim(GuildClaim claim) {
        claims.remove(claim);
    }

    public void setHome(Location loc) {
        home = loc;
    }

    public Location getHome() {
        return home;
    }

    public void setMotd(String text) {
        motd = text;
    }

    public String getMotd() {
        return motd;
    }

    public InviteMode getInviteMode() {
        return inviteMode;
    }

    public void setInviteMode(InviteMode mode) {
        inviteMode = mode;
    }

    public void log(String message) {
        logs.add(new GuildLogEntry(message));
        if (logs.size() > 200) {
            logs.remove(0);
        }
    }

    public List<GuildLogEntry> getLogs() {
        return logs;
    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    public boolean hasPermission(UUID uuid, String action) {
        GuildRank rank = members.get(uuid);
        if (rank == null) return false;

        switch (action.toLowerCase()) {
            case "invite": return rank.canInvite();
            case "kick": return rank.canKick();
            case "promote": return rank.canPromoteDemote();
            case "claim": return rank.canClaim();
            case "sethome": return rank.canSetHome();
        }
        return false;
    }
}
