package com.ravengrade.guilds.data;

import org.bukkit.World;

public class GuildClaim {

    private final String guildName;
    private final String world;
    private final int chunkX;
    private final int chunkZ;

    public GuildClaim(String guildName, World world, int chunkX, int chunkZ) {
        this.guildName = guildName;
        this.world = world.getName();
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public String getGuildName() {
        return guildName;
    }

    public String getWorld() {
        return world;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public String getUniqueKey() {
        return world + ":" + chunkX + ":" + chunkZ;
    }
}
