package com.ravengrade.guilds.commands;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.Guild;
import com.ravengrade.guilds.data.GuildClaim;
import com.ravengrade.guilds.managers.ClaimManager;
import com.ravengrade.guilds.managers.GuildManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Admin commands:
 * /gadmin list
 * /gadmin info <guild>
 * /gadmin disband <guild>
 * /gadmin unclaimhere
 * /gadmin unclaimall <guild>
 * /gadmin claims <guild>
 * /gadmin tpclaim <guild> <index>
 */
public class GuildAdminCommand implements CommandExecutor, TabCompleter {

    private final GuildsPlugin plugin;

    public GuildAdminCommand(GuildsPlugin plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------------------
    //  Command execution
    // ---------------------------------------------------------------------

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("guild.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use /gadmin.");
            return true;
        }

        GuildManager guildManager = plugin.getGuildManager();
        ClaimManager claimManager = plugin.getClaimManager();

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {

            case "list": {
                sender.sendMessage(ChatColor.GOLD + "All guilds:");
                for (Guild g : guildManager.getAllGuilds()) {
                    sender.sendMessage(ChatColor.YELLOW + "- " + g.getName()
                            + ChatColor.GRAY + " (members=" + g.getMembers().size()
                            + ", claims=" + g.getClaims().size() + ")");
                }
                return true;
            }

            case "info": {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /gadmin info <guild>");
                    return true;
                }

                Guild g = guildManager.getGuild(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "Guild not found.");
                    return true;
                }

                sender.sendMessage(ChatColor.GOLD + "Guild: " + g.getName());
                sender.sendMessage(ChatColor.YELLOW + "Leader: " + g.getLeader());
                sender.sendMessage(ChatColor.YELLOW + "Members (" + g.getMembers().size() + "):");
                for (Map.Entry<UUID, ?> entry : g.getMembers().entrySet()) {
                    UUID id = entry.getKey();
                    String name = Optional.ofNullable(Bukkit.getOfflinePlayer(id).getName())
                            .orElse(id.toString());
                    sender.sendMessage(ChatColor.GRAY + "- " + name);
                }

                List<GuildClaim> claims = claimManager.getClaimsForGuild(g.getName());
                sender.sendMessage(ChatColor.YELLOW + "Claims (" + claims.size() + "):");
                for (GuildClaim c : claims) {
                    sender.sendMessage(ChatColor.GRAY + "  World: " + c.getWorld()
                            + "  Chunk: [" + c.getChunkX() + ", " + c.getChunkZ() + "]");
                }
                return true;
            }

            case "disband": {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /gadmin disband <guild>");
                    return true;
                }

                Guild g = guildManager.getGuild(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "Guild not found.");
                    return true;
                }

                guildManager.disbandGuild(g, claimManager);
                sender.sendMessage(ChatColor.YELLOW + "Force-disbanded guild " + g.getName() + ".");
                return true;
            }

            case "unclaimhere": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Player only command.");
                    return true;
                }
                Player p = (Player) sender;
                Chunk c = p.getLocation().getChunk();
                GuildClaim claim = claimManager.getClaim(c);
                if (claim == null) {
                    p.sendMessage(ChatColor.RED + "This chunk is not claimed.");
                    return true;
                }
                claimManager.unclaimChunk(claim.getWorld(), claim.getChunkX(), claim.getChunkZ());
                p.sendMessage(ChatColor.YELLOW + "Unclaimed this chunk.");
                return true;
            }

            case "unclaimall": {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /gadmin unclaimall <guild>");
                    return true;
                }
                Guild g = guildManager.getGuild(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "Guild not found.");
                    return true;
                }

                List<GuildClaim> list = new ArrayList<>(claimManager.getClaimsForGuild(g.getName()));
                for (GuildClaim c : list) {
                    claimManager.unclaimChunk(c.getWorld(), c.getChunkX(), c.getChunkZ());
                }
                sender.sendMessage(ChatColor.YELLOW + "Unclaimed all land for guild " + g.getName() + ".");
                return true;
            }

            case "claims": {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /gadmin claims <guild>");
                    return true;
                }
                Guild g = guildManager.getGuild(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "Guild not found.");
                    return true;
                }

                List<GuildClaim> list = claimManager.getClaimsForGuild(g.getName());
                sender.sendMessage(ChatColor.GOLD + "Claims for " + g.getName() + ":");
                int i = 1;
                for (GuildClaim c : list) {
                    sender.sendMessage(ChatColor.YELLOW + "#" + i + " " + c.getWorld()
                            + " x=" + c.getChunkX() + " z=" + c.getChunkZ());
                    i++;
                }
                return true;
            }

            case "tpclaim": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Player only command.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /gadmin tpclaim <guild> <index>");
                    return true;
                }

                Guild g = guildManager.getGuild(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "Guild not found.");
                    return true;
                }

                List<GuildClaim> list = claimManager.getClaimsForGuild(g.getName());
                if (list.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "That guild has no claims.");
                    return true;
                }

                int index;
                try {
                    index = Integer.parseInt(args[2]) - 1;
                } catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + "Index must be a number.");
                    return true;
                }

                if (index < 0 || index >= list.size()) {
                    sender.sendMessage(ChatColor.RED + "Index out of range. (1-" + list.size() + ")");
                    return true;
                }

                GuildClaim c = list.get(index);
                World world = Bukkit.getWorld(c.getWorld());
                if (world == null) {
                    sender.sendMessage(ChatColor.RED + "World '" + c.getWorld() + "' is not loaded.");
                    return true;
                }

                int bx = (c.getChunkX() << 4) + 8;
                int bz = (c.getChunkZ() << 4) + 8;
                int by = world.getHighestBlockYAt(bx, bz) + 1;

                Player p = (Player) sender;
                p.teleport(new org.bukkit.Location(world, bx + 0.5, by, bz + 0.5));
                p.sendMessage(ChatColor.GREEN + "Teleported to claim #" + (index + 1)
                        + " of guild " + g.getName() + ".");
                return true;
            }

            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Guild Admin Commands:");
        sender.sendMessage(ChatColor.YELLOW + "/gadmin list");
        sender.sendMessage(ChatColor.YELLOW + "/gadmin info <guild>");
        sender.sendMessage(ChatColor.YELLOW + "/gadmin disband <guild>");
        sender.sendMessage(ChatColor.YELLOW + "/gadmin unclaimhere");
        sender.sendMessage(ChatColor.YELLOW + "/gadmin unclaimall <guild>");
        sender.sendMessage(ChatColor.YELLOW + "/gadmin claims <guild>");
        sender.sendMessage(ChatColor.YELLOW + "/gadmin tpclaim <guild> <index>");
    }

    // ---------------------------------------------------------------------
    //  Tab completion
    // ---------------------------------------------------------------------

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("guild.admin")) return Collections.emptyList();

        List<String> out = new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = Arrays.asList(
                    "list", "info", "disband", "unclaimhere",
                    "unclaimall", "claims", "tpclaim"
            );
            String start = args[0].toLowerCase(Locale.ROOT);
            for (String s : subs) {
                if (s.startsWith(start)) out.add(s);
            }
            return out;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (Arrays.asList("info", "disband", "unclaimall", "claims", "tpclaim").contains(sub)) {
                String start = args[1].toLowerCase(Locale.ROOT);
                for (Guild g : plugin.getGuildManager().getAllGuilds()) {
                    if (g.getName().toLowerCase(Locale.ROOT).startsWith(start)) {
                        out.add(g.getName());
                    }
                }
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("tpclaim")) {
            // Suggest numeric indices for tpclaim
            Guild g = plugin.getGuildManager().getGuild(args[1]);
            if (g != null) {
                int size = plugin.getClaimManager().getClaimsForGuild(g.getName()).size();
                for (int i = 1; i <= size; i++) {
                    out.add(String.valueOf(i));
                }
            }
        }

        return out;
    }
}
