package com.ravengrade.guilds.commands;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.Guild;
import com.ravengrade.guilds.data.GuildClaim;
import com.ravengrade.guilds.data.GuildRank;
import com.ravengrade.guilds.data.InviteMode;
import com.ravengrade.guilds.listeners.ChatListener;
import com.ravengrade.guilds.listeners.HomeTeleportListener;
import com.ravengrade.guilds.util.BorderVisualizer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Main player command: /guild
 */
public class GuildCommand implements CommandExecutor, TabCompleter {

    private final GuildsPlugin plugin;

    public GuildCommand(GuildsPlugin plugin) {
        this.plugin = plugin;
    }

    // ----------------------------------------------------
    // Helpers
    // ----------------------------------------------------

    private void sendHelp(Player p) {
        p.sendMessage(ChatColor.GOLD + "Guild Commands:");
        p.sendMessage(ChatColor.YELLOW + "/guild create <name>");
        p.sendMessage(ChatColor.YELLOW + "/guild info");
        p.sendMessage(ChatColor.YELLOW + "/guild invite <player>");
        p.sendMessage(ChatColor.YELLOW + "/guild accept");
        p.sendMessage(ChatColor.YELLOW + "/guild leave");
        p.sendMessage(ChatColor.YELLOW + "/guild promote <player>");
        p.sendMessage(ChatColor.YELLOW + "/guild demote <player>");
        p.sendMessage(ChatColor.YELLOW + "/guild kick <player>");
        p.sendMessage(ChatColor.YELLOW + "/guild sethome");
        p.sendMessage(ChatColor.YELLOW + "/guild home");
        p.sendMessage(ChatColor.YELLOW + "/guild claim");
        p.sendMessage(ChatColor.YELLOW + "/guild unclaim");
        p.sendMessage(ChatColor.YELLOW + "/guild here");
        p.sendMessage(ChatColor.YELLOW + "/guild claims");
        p.sendMessage(ChatColor.YELLOW + "/guild showborders");
        p.sendMessage(ChatColor.YELLOW + "/guild motd [set <text>]");
        p.sendMessage(ChatColor.YELLOW + "/guild invitemode");
        p.sendMessage(ChatColor.YELLOW + "/guild rename <newName>");
        p.sendMessage(ChatColor.YELLOW + "/guild disband [confirm]");
        p.sendMessage(ChatColor.YELLOW + "/guild gchat");
        p.sendMessage(ChatColor.YELLOW + "/guild menu");
        p.sendMessage(ChatColor.YELLOW + "/guild list");
    }

    private void updateTab(Player player) {
        if (player == null) return;
        if (plugin.getTablistListener() != null) {
            plugin.getTablistListener().updateTabName(player);
        }
    }

    private void updateAllTabs() {
        if (plugin.getTablistListener() == null) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getTablistListener().updateTabName(p);
        }
    }

    private static class OfflinePlayerWrapper {
        UUID uuid;
        String name;
    }

    private OfflinePlayerWrapper findOfflinePlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            OfflinePlayerWrapper w = new OfflinePlayerWrapper();
            w.uuid = online.getUniqueId();
            w.name = online.getName();
            return w;
        }
        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
            if (op.getName() != null && op.getName().equalsIgnoreCase(name)) {
                OfflinePlayerWrapper w = new OfflinePlayerWrapper();
                w.uuid = op.getUniqueId();
                w.name = op.getName();
                return w;
            }
        }
        return null;
    }

    // ----------------------------------------------------
    // Command execution
    // ----------------------------------------------------

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        Player p = (Player) sender;
        UUID uuid = p.getUniqueId();

        if (args.length == 0) {
            sendHelp(p);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {

            // ----------------- CREATE -----------------
            case "create": {
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /guild create <name>");
                    return true;
                }
                if (plugin.getGuildManager().isInGuild(uuid)) {
                    p.sendMessage(ChatColor.RED + "You are already in a guild.");
                    return true;
                }
                String name = args[1];
                if (plugin.getGuildManager().getGuild(name) != null) {
                    p.sendMessage(ChatColor.RED + "A guild with that name already exists.");
                    return true;
                }
                Guild g = plugin.getGuildManager().createGuild(name, uuid);
                if (g == null) {
                    p.sendMessage(ChatColor.RED + "Could not create guild.");
                } else {
                    p.sendMessage(ChatColor.GREEN + "Created guild: " + g.getName());
                    updateTab(p);
                }
                return true;
            }

            // ----------------- INFO -----------------
            case "info": {
                Guild g = plugin.getGuildManager().getGuildByPlayer(uuid);
                if (g == null) {
                    p.sendMessage(ChatColor.RED + "You are not in a guild.");
                    return true;
                }
                p.sendMessage(ChatColor.GOLD + "Guild: " + g.getName());
                p.sendMessage(ChatColor.YELLOW + "MOTD: " + g.getMotd());
                p.sendMessage(ChatColor.YELLOW + "Invite mode: " + g.getInviteMode().name());
                p.sendMessage(ChatColor.YELLOW + "Members:");
                for (Map.Entry<UUID, GuildRank> e : g.getMembers().entrySet()) {
                    String n = Optional.ofNullable(Bukkit.getOfflinePlayer(e.getKey()).getName())
                            .orElse(e.getKey().toString());
                    p.sendMessage(ChatColor.GRAY + "- " + n + " (" + e.getValue().name() + ")");
                }
                p.sendMessage(ChatColor.YELLOW + "Claims: " + g.getClaims().size());
                return true;
            }

            // ----------------- INVITE -----------------
            case "invite": {
                Guild g = plugin.getGuildManager().getGuildByPlayer(uuid);
                if (g == null) {
                    p.sendMessage(ChatColor.RED + "You are not in a guild.");
                    return true;
                }
                if (!g.hasPermission(uuid, "invite")) {
                    p.sendMessage(ChatColor.RED + "You do not have permission to invite.");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /guild invite <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    p.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                if (plugin.getGuildManager().isInGuild(target.getUniqueId())) {
                    p.sendMessage(ChatColor.RED + "That player is already in a guild.");
                    return true;
                }

                plugin.getGuildManager().setPendingInvite(target.getUniqueId(), g.getName());
                p.sendMessage(ChatColor.GREEN + "Invited " + target.getName() + " to your guild.");
                target.sendMessage(ChatColor.GOLD + "You have been invited to guild " + g.getName()
                        + ". Use /guild accept to join.");
                return true;
            }

            // ----------------- ACCEPT -----------------
            case "accept": {
                if (plugin.getGuildManager().isInGuild(uuid)) {
                    p.sendMessage(ChatColor.RED + "You are already in a guild.");
                    return true;
                }
                String guildName = plugin.getGuildManager().consumePendingInvite(uuid);
                if (guildName == null) {
                    p.sendMessage(ChatColor.RED + "You have no pending guild invites.");
                    return true;
                }
                Guild g = plugin.getGuildManager().getGuild(guildName);
                if (g == null) {
                    p.sendMessage(ChatColor.RED + "That guild no longer exists.");
                    return true;
                }
                plugin.getGuildManager().addMember(g, uuid);
                p.sendMessage(ChatColor.GREEN + "You joined guild " + g.getName() + ".");
                updateTab(p);
                return true;
            }

            // ----------------- LEAVE -----------------
            case "leave": {
                Guild g = plugin.getGuildManager().getGuildByPlayer(uuid);
                if (g == null) {
                    p.sendMessage(ChatColor.RED + "You are not in a guild.");
                    return true;
                }
                GuildRank rank = g.getRank(uuid);
                if (rank == GuildRank.LEADER) {
                    p.sendMessage(ChatColor.RED + "You are the leader. Use /guild disband or transfer leadership before leaving.");
                    return true;
                }
                plugin.getGuildManager().removeMember(g, uuid);
                p.sendMessage(ChatColor.YELLOW + "You left the guild " + g.getName() + ".");
                updateTab(p);
                return true;
            }

            // ----------------- PROMOTE / DEMOTE -----------------
            case "promote":
            case "demote": {
                Guild g = plugin.getGuildManager().getGuildByPlayer(uuid);
                if (g == null) {
                    p.sendMessage(ChatColor.RED + "You are not in a guild.");
                    return true;
                }
                if (!g.hasPermission(uuid, "promote")) {
                    p.sendMessage(ChatColor.RED + "Only the leader can promote/demote.");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /guild " + sub + " <player>");
                    return true;
                }

                OfflinePlayerWrapper targetWrap = findOfflinePlayer(args[1]);
                if (targetWrap == null) {
                    p.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                UUID targetId = targetWrap.uuid;
                if (!g.isMember(targetId)) {
                    p.sendMessage(ChatColor.RED + "That player is not in your guild.");
                    return true;
                }

                GuildRank current = g.getRank(targetId);
                GuildRank[] order = GuildRank.values();
                int idx = Arrays.asList(order).indexOf(current);
                if (idx < 0) idx = 0;

                if (sub.equals("promote")) {
                    if (current == GuildRank.LEADER) {
                        p.sendMessage(ChatColor.RED + "They are already the leader.");
                        return true;
                    }
                    if (current == GuildRank.OFFICER) {
                        // transfer leadership
                        g.setRank(uuid, GuildRank.OFFICER);
                        g.setRank(targetId, GuildRank.LEADER);
                        g.setLeader(targetId);
                        p.sendMessage(ChatColor.GREEN + "Leadership transferred to " + targetWrap.name + ".");
                        Player target = Bukkit.getPlayer(targetId);
                        if (target != null) updateTab(target);
                        updateTab(p);
                    } else {
                        GuildRank next = order[Math.max(0, idx - 1)];
                        g.setRank(targetId, next);
                        p.sendMessage(ChatColor.GREEN + "Promoted " + targetWrap.name + " to " + next.name() + ".");
                    }
                } else {
                    if (current == GuildRank.RECRUIT) {
                        p.sendMessage(ChatColor.RED + "They are already the lowest rank.");
                        return true;
                    }
                    GuildRank next = order[Math.min(order.length - 1, idx + 1)];
                    g.setRank(targetId, next);
                    p.sendMessage(ChatColor.GREEN + "Demoted " + targetWrap.name + " to " + next.name() + ".");
                }
                return true;
            }

            // ----------------- KICK -----------------
            case "kick": {
                Guild g = plugin.getGuildManager().getGuildByPlayer(uuid);
                if (g == null) {
                    p.sendMessage(ChatColor.RED + "You are not in a guild.");
                    return true;
                }
                if (!g.hasPermission(uuid, "kick")) {
                    p.sendMessage(ChatColor.RED + "You do not have permission to kick members.");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /guild kick <player>");
                    return true;
                }

                OfflinePlayerWrapper targetWrap = findOfflinePlayer(args[1]);
                if (targetWrap == null) {
                    p.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                UUID targetId = targetWrap.uuid;
                if (!g.isMember(targetId)) {
                    p.sendMessage(ChatColor.RED + "That player is not in your guild.");
                    return true;
                }
                if (g.getRank(targetId) == GuildRank.LEADER) {
                    p.sendMessage(ChatColor.RED + "You cannot kick the leader.");
                    return true;
                }
                plugin.getGuildManager().removeMember(g, targetId);
                p.sendMessage(ChatColor.YELLOW + "Kicked " + targetWrap.name + " from the guild.");
                Player target = Bukkit.getPlayer(targetId);
                if (target != null) updateTab(target);
                return true;
            }

            // ----------------- HOME / SETHOME -----------------
            case "sethome": {
                Guild g = plugin.getGuildManager().getGuildByPlayer(uuid);
                if (g == null) {
                    p.sendMessage(ChatColor.RED + "You are not in a guild.");
                    return true;
                }
                if (!g.hasPermission(uuid, "sethome")) {
                    p.sendMessage(ChatColor.RED + "You do not have permission to set the guild home.");
                    return true;
                }
                g.setHome(p.getLocation());
                p.sendMessage(ChatColor.GREEN + "Guild home set.");
                return true;
            }

            case "home": {
                Guild g = plugin.getGuildManager().getGuildByPlayer(uuid);
                if (g == null) {
                    p.sendMessage(ChatColor.RED + "You are not in a guild.");
                    return true;
                }
                if (g.getHome() == null) {
                    p.sendMessage(ChatColor.RED + "Your guild has no home set.");
                    return true;
                }
                if (HomeTeleportListener.hasPendingTeleport(p)) {
                    p.sendMessage(ChatColor.RED + "You already have a teleport in progress.");
                    return true;
                }
                HomeTeleportListener.startHomeTeleport(plugin, p, g);
                return true;
            }

            // ----------------- CLAIMS -----------------
            case "claim": {
                Guild g = plugin.getGuildManager().getGuildByPlayer(uuid);
                if (g == null) {
                    p.sendMessage(ChatColor.RED + "You are not in a guild.");
                    return true;
                }
                if (!g.hasPermission(uuid, "claim")) {
                    p.sendMessage(ChatColor.RED + "You do not have permission to claim land.");
                    return true;
                }

                int maxClaims = plugin.getConfig().getInt("guilds.max-claims", 20);
                if (g.getClaims().size() >= maxClaims) {
                    p.sendMessage(ChatColor.RED + "Your guild has reached the max claim limit (" + maxClaims + ").");
                    return true;
                }

                Chunk chunk = p.getLocation().getChunk();
                if (plugin.getClaimManager().isClaimed(chunk)) {
                    p.sendMessage(ChatColor.RED + "This chunk is already claimed.");
                    return true;
                }

                plugin.getClaimManager().claimChunk(g, chunk);
                p.sendMessage(ChatColor.GREEN + "Claimed this chunk for your guild.");
                return true;
            }

            case "unclaim": {
                Guild g = plugin.getGuildManager().getGuildByPlayer(uuid);
                if (g == null) {
                    p.sendMessage(ChatColor.RED + "You are not in a guild.");
                    return true;
                }
                if (!g.hasPermission(uuid, "claim")) {
                    p.sendMessage(ChatColor.RED + "You do not have permission to unclaim land.");
                    return true;
                }

                Chunk chunk = p.getLocation().getChunk();
                GuildClaim claim = plugin.getClaimManager().getClaim(chunk);
                if (claim == null) {
                    p.sendMessage(ChatColor.RED + "This chunk is not claimed.");
                    return true;
                }
                if (!claim.getGuildName().equalsIgnoreCase(g.getName())) {
                    p.sendMessage(ChatColor.RED + "This chunk is not claimed by your guild.");
                    return true;
                }

                plugin.getClaimManager().unclaimChunk(claim.getWorld(), claim.getChunkX(), claim.getChunkZ());
                p.sendMessage(ChatColor.YELLOW + "Unclaimed this chunk.");
                return true;
            }

            case "here": {
                Chunk chunk = p.getLocation().getChunk();
                GuildClaim claim = plugin.getClaimManager().getClaim(chunk);
                if (claim == null) {
                    p.sendMessage(ChatColor.GRAY + "This chunk is unclaimed (Wilderness).");
                } else {
                    p.sendMessage(ChatColor.GOLD + "This chunk is claimed by " + claim.getGuildName() + ".");
                }
                return true;
            }

            case "claims": {
                Guild g = plugin.getGuildManager().getGuildByPlayer(uuid);
                if (g == null) {
                    p.sendMessage(ChatColor.RED + "You are not in a guild.");
                    return true;
                }
                List<GuildClaim> list = plugin.getClaimManager().getClaimsForGuild(g.getName());
                if (list.isEmpty()) {
                    p.sendMessage(ChatColor.YELLOW + "Your guild has no claims.");
                    return true;
                }
                p.sendMessage(ChatColor.GOLD + "Your guild's claims:");
                int i = 1;
                for (GuildClaim c : list) {
                    p.sendMessage(ChatColor.YELLOW + "#" + i + " "
                            + c.getWorld() + " x=" + c.getChunkX() + " z=" + c.getChunkZ());
                    i++;
                }
                return true;
            }

            case "showborders": {
                Chunk chunk = p.getLocation().getChunk();
                GuildClaim claim = plugin.getClaimManager().getClaim(chunk);
                if (claim == null) {
                    p.sendMessage(ChatColor.RED + "This chunk is not claimed.");
                    return true;
                }
                Guild g = plugin.getGuildManager().getGuildByPlayer(uuid);
                if (g == null || !claim.getGuildName().equalsIgnoreCase(g.getName())) {
                    p.sendMessage(ChatColor.RED + "You can only show borders of your own guild's land.");
                    return true;
                }
                BorderVisualizer.showChunkBorder(p, chunk);
                p.sendMessage(ChatColor.GREEN + "Showing borders for this claim.");
                return true;
            }

            // ----------------- MOTD -----------------
            case "motd": {
                Guild g = plugin.getGuildManager().getGuildByPlayer(uuid);
                if (g == null) {
                    p.sendMessage(ChatColor.RED + "You are not in a guild.");
                    return true;
                }
                if (args.length >= 2 && args[1].equalsIgnoreCase("set")) {
                    GuildRank rank = g.getRank(uuid);
                    if (rank != GuildRank.LEADER && rank != GuildRank.OFFICER) {
                        p.sendMessage(ChatColor.RED + "Only officers and the leader can set the MOTD.");
                        return true;
                    }
                    if (args.length < 3) {
                        p.sendMessage(ChatColor.RED + "Usage: /guild motd set <text>");
                        return true;
                    }
                    String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    g.setMotd(text);
                    p.sendMessage(ChatColor.GREEN + "Guild MOTD set.");
                } else {
                    p.sendMessage(ChatColor.GOLD + "Guild MOTD: " + ChatColor.YELLOW + g.getMotd());
                }
                return true;
            }

            // ----------------- INVITEMODE -----------------
            case "invitemode": {
                Guild g = plugin.getGuildManager().getGuildByPlayer(uuid);
                if (g == null) {
                    p.sendMessage(ChatColor.RED + "You are not in a guild.");
                    return true;
                }
                if (g.getRank(uuid) != GuildRank.LEADER) {
                    p.sendMessage(ChatColor.RED + "Only the leader can change invite mode.");
                    return true;
                }
                InviteMode current = g.getInviteMode();
                InviteMode next = (current == InviteMode.INVITE_ONLY ? InviteMode.OPEN : InviteMode.INVITE_ONLY);
                g.setInviteMode(next);
                p.sendMessage(ChatColor.GREEN + "Guild invite mode is now: " + next.name());
                return true;
            }

            // ----------------- RENAME (stub) -----------------
            case "rename": {
                p.sendMessage(ChatColor.RED + "Guild rename is not fully implemented yet.");
                return true;
            }

            // ----------------- DISBAND -----------------
            case "disband": {
                Guild g = plugin.getGuildManager().getGuildByPlayer(uuid);
                if (g == null) {
                    p.sendMessage(ChatColor.RED + "You are not in a guild.");
                    return true;
                }
                if (!g.getLeader().equals(uuid)) {
                    p.sendMessage(ChatColor.RED + "Only the leader can disband the guild.");
                    return true;
                }
                if (args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
                    plugin.getGuildManager().disbandGuild(g, plugin.getClaimManager());
                    p.sendMessage(ChatColor.YELLOW + "You disbanded the guild.");
                    updateAllTabs();
                } else {
                    p.sendMessage(ChatColor.RED + "This will delete your guild and all claims.");
                    p.sendMessage(ChatColor.RED + "Run /guild disband confirm to confirm.");
                }
                return true;
            }

            // ----------------- GUILD CHAT -----------------
            case "gchat": {
                boolean now = !ChatListener.isGuildChatToggled(uuid);
                ChatListener.setGuildChatToggled(uuid, now);
                if (now) {
                    p.sendMessage(ChatColor.GREEN + "Guild chat enabled. Your messages will go only to your guild.");
                } else {
                    p.sendMessage(ChatColor.YELLOW + "Guild chat disabled. Your messages are public again.");
                }
                return true;
            }

            // ----------------- GUI -----------------
            case "menu": {
                plugin.getGuiManager().openMainMenu(p);
                return true;
            }

            case "list": {
                plugin.getGuiManager().openGuildBrowser(p);
                return true;
            }

            // ----------------- DEFAULT -----------------
            default:
                sendHelp(p);
                return true;
        }
    }

    // ----------------------------------------------------
    // Tab completion
    // ----------------------------------------------------

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        List<String> out = new ArrayList<>();

        if (args.length == 1) {
            List<String> base = Arrays.asList(
                    "create", "info", "invite", "accept", "leave",
                    "promote", "demote", "kick",
                    "sethome", "home",
                    "claim", "unclaim", "here", "claims", "showborders",
                    "motd", "invitemode", "rename",
                    "disband",
                    "gchat", "menu", "list"
            );
            String start = args[0].toLowerCase(Locale.ROOT);
            for (String s : base) {
                if (s.startsWith(start)) out.add(s);
            }
            return out;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("invite") || sub.equals("promote") || sub.equals("demote") || sub.equals("kick")) {
                String start = args[1].toLowerCase(Locale.ROOT);
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (target.getName().toLowerCase(Locale.ROOT).startsWith(start)) {
                        out.add(target.getName());
                    }
                }
            }
            if (sub.equals("motd")) {
                if ("set".startsWith(args[1].toLowerCase(Locale.ROOT))) {
                    out.add("set");
                }
            }
            if (sub.equals("rename")) {
                out.add("<newName>");
            }
        }

        return out;
    }
}
