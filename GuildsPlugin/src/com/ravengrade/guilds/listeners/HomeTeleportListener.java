package com.ravengrade.guilds.listeners;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.Guild;
import com.ravengrade.guilds.util.TeleportDelayTask;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeTeleportListener implements Listener {

    private final GuildsPlugin plugin;

    // Tracks players currently in a teleport countdown
    private static final Map<UUID, TeleportDelayTask> pendingTeleports = new HashMap<>();

    public HomeTeleportListener(GuildsPlugin plugin) {
        this.plugin = plugin;
    }

    public static boolean hasPendingTeleport(Player player) {
        return pendingTeleports.containsKey(player.getUniqueId());
    }

    public static void startHomeTeleport(GuildsPlugin plugin, Player player, Guild guild) {
        cancelTeleport(player);

        int delay = plugin.getConfig().getInt("teleport.home-delay-seconds", 5);
        boolean cancelOnDamage = plugin.getConfig().getBoolean("teleport.cancel-on-damage", true);
        boolean cancelOnMove = plugin.getConfig().getBoolean("teleport.cancel-on-move", true);

        TeleportDelayTask task = new TeleportDelayTask(plugin, player, guild, delay, cancelOnDamage, cancelOnMove);
        pendingTeleports.put(player.getUniqueId(), task);
        task.start();
    }

    public static void cancelTeleport(Player player) {
        TeleportDelayTask task = pendingTeleports.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player p = (Player) event.getEntity();
        TeleportDelayTask task = pendingTeleports.get(p.getUniqueId());
        if (task != null && task.isCancelOnDamage()) {
            task.cancelWithMessage("Teleport cancelled because you took damage.");
            pendingTeleports.remove(p.getUniqueId());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        TeleportDelayTask task = pendingTeleports.get(p.getUniqueId());
        if (task == null) return;
        if (!task.isCancelOnMove()) return;

        if (event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()) {
            task.cancelWithMessage("Teleport cancelled because you moved.");
            pendingTeleports.remove(p.getUniqueId());
        }
    }
}
