// src/com/ravengrade/guilds/util/TeleportDelayTask.java
package com.ravengrade.guilds.util;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.Guild;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportDelayTask extends BukkitRunnable {

    private final GuildsPlugin plugin;
    private final Player player;
    private final Guild guild;
    private final boolean cancelOnDamage;
    private final boolean cancelOnMove;

    private int secondsLeft;

    public TeleportDelayTask(GuildsPlugin plugin, Player player, Guild guild, int delaySeconds,
                             boolean cancelOnDamage, boolean cancelOnMove) {
        this.plugin = plugin;
        this.player = player;
        this.guild = guild;
        this.secondsLeft = delaySeconds;
        this.cancelOnDamage = cancelOnDamage;
        this.cancelOnMove = cancelOnMove;
    }

    public boolean isCancelOnDamage() {
        return cancelOnDamage;
    }

    public boolean isCancelOnMove() {
        return cancelOnMove;
    }

    public void start() {
        player.sendMessage(ChatColor.GREEN + "Teleporting to guild home in " + secondsLeft + " seconds. Don't move or take damage.");
        this.runTaskTimer(plugin, 20L, 20L);
    }

    public void cancelWithMessage(String msg) {
        cancel();
        player.sendMessage(ChatColor.RED + msg);
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }

        if (secondsLeft <= 0) {
            cancel();
            if (guild.getHome() == null) {
                player.sendMessage(ChatColor.RED + "Your guild does not have a home set.");
                return;
            }
            Location home = guild.getHome();
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.teleport(home);
                player.sendMessage(ChatColor.GREEN + "Teleported to guild home.");
            });
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Teleporting in " + secondsLeft + "...");
        secondsLeft--;
    }
}
