// src/com/ravengrade/guilds/util/BorderVisualizer.java
package com.ravengrade.guilds.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class BorderVisualizer {

    public static void showChunkBorder(Player player, Chunk chunk) {
        int minX = chunk.getX() << 4;
        int minZ = chunk.getZ() << 4;
        int maxX = minX + 15;
        int maxZ = minZ + 15;
        int y = player.getLocation().getBlockY();

        for (int x = minX; x <= maxX; x++) {
            spawnParticle(player, new Location(chunk.getWorld(), x + 0.5, y, minZ + 0.5));
            spawnParticle(player, new Location(chunk.getWorld(), x + 0.5, y, maxZ + 0.5));
        }

        for (int z = minZ; z <= maxZ; z++) {
            spawnParticle(player, new Location(chunk.getWorld(), minX + 0.5, y, z + 0.5));
            spawnParticle(player, new Location(chunk.getWorld(), maxX + 0.5, y, z + 0.5));
        }
    }

    private static void spawnParticle(Player player, Location loc) {
        player.spawnParticle(Particle.VILLAGER_HAPPY, loc, 1, 0, 0, 0, 0);
    }
}
