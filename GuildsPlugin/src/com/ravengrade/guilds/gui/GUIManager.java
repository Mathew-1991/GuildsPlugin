// src/com/ravengrade/guilds/managers/GUIManager.java
package com.ravengrade.guilds.managers;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.Guild;
import com.ravengrade.guilds.data.GuildClaim;
import com.ravengrade.guilds.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class GUIManager {

    private final GuildsPlugin plugin;

    public static final String MENU_TITLE = ChatColor.DARK_GREEN + "Guild Menu";
    public static final String BROWSER_TITLE = ChatColor.BLUE + "Guild Browser";

    public GUIManager(GuildsPlugin plugin) {
        this.plugin = plugin;
    }

    // ----------------- MAIN MENU -----------------

    public void openMainMenu(Player player) {
        Guild guild = plugin.getGuildManager().getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild.");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, MENU_TITLE);

        int maxMembers = plugin.getConfig().getInt("guilds.max-members", 20);
        int maxClaims = plugin.getConfig().getInt("guilds.max-claims", 20);
        int memberCount = guild.getMembers().size();
        List<GuildClaim> claims = plugin.getClaimManager().getClaimsForGuild(guild.getName());
        int claimCount = claims.size();
        String motd = guild.getMotd() == null || guild.getMotd().isEmpty() ? "No MOTD set." : guild.getMotd();

        ItemStack stats = new ItemBuilder(Material.PAPER)
                .name("&aGuild Stats")
                .lore(
                        "&7Guild: &f" + guild.getName(),
                        "&7Members: &f" + memberCount + "&7/&f" + maxMembers,
                        "&7Claims: &f" + claimCount + "&7/&f" + maxClaims,
                        "&7Invite: &f" + guild.getInviteMode().name(),
                        "&7MOTD: &f" + motd
                )
                .build();
        inv.setItem(4, stats);

        inv.setItem(10, new ItemBuilder(Material.PLAYER_HEAD)
                .name("&aMembers & Ranks")
                .lore("&7View members and ranks.", "&7(Executes &f/guild info&7)")
                .tag(plugin, "guild_gui_action", "info")
                .build());

        inv.setItem(11, new ItemBuilder(Material.BOOK)
                .name("&aInvites & Join Mode")
                .lore("&7Toggle invite-only or open guild.", "&7(Executes &f/guild invitemode&7)")
                .tag(plugin, "guild_gui_action", "invitemode")
                .build());

        inv.setItem(12, new ItemBuilder(Material.MAP)
                .name("&aClaims")
                .lore("&7List your claims.", "&7(Executes &f/guild claims&7)")
                .tag(plugin, "guild_gui_action", "claims")
                .build());

        inv.setItem(13, new ItemBuilder(Material.ENDER_PEARL)
                .name("&aGuild Home")
                .lore("&7Teleport to guild home.", "&7(Executes &f/guild home&7)")
                .tag(plugin, "guild_gui_action", "home")
                .build());

        inv.setItem(14, new ItemBuilder(Material.RED_BED)
                .name("&aSet Guild Home")
                .lore("&7Set the guild home at your location.", "&7(Executes &f/guild sethome&7)")
                .tag(plugin, "guild_gui_action", "sethome")
                .build());

        inv.setItem(15, new ItemBuilder(Material.NOTE_BLOCK)
                .name("&aGuild Chat Toggle")
                .lore("&7Toggle guild-only chat.", "&7(Executes &f/guild gchat&7)")
                .tag(plugin, "guild_gui_action", "gchat")
                .build());

        inv.setItem(16, new ItemBuilder(Material.COMPASS)
                .name("&aGuild Browser")
                .lore("&7Browse all guilds on the server.", "&7(Executes &f/guild list&7)")
                .tag(plugin, "guild_gui_action", "list")
                .build());

        inv.setItem(22, new ItemBuilder(Material.REDSTONE)
                .name("&cSettings & Utilities")
                .lore(
                        "&7Useful commands:",
                        "&f/guild motd set <text>",
                        "&f/guild tagcolor <color>",
                        "&f/guild seticon",
                        "&f/guild rename <name>",
                        "&f/guild disband confirm"
                )
                .tag(plugin, "guild_gui_action", "motd")
                .build());

        player.openInventory(inv);
    }

    // ----------------- GUILD BROWSER -----------------

    public void openGuildBrowser(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, BROWSER_TITLE);

        int slot = 0;
        for (Guild guild : plugin.getGuildManager().getAllGuilds()) {
            if (slot >= inv.getSize()) break;

            Material iconMat = getGuildIconMaterial(guild.getName());
            ItemBuilder builder = new ItemBuilder(iconMat)
                    .name("&e" + guild.getName())
                    .tag(plugin, "guild_name", guild.getName());

            String leaderName = getLeaderName(guild);
            int memberCount = guild.getMembers().size();
            int claimCount = plugin.getClaimManager().getClaimsForGuild(guild.getName()).size();
            String motd = guild.getMotd() == null || guild.getMotd().isEmpty() ? "No MOTD set." : guild.getMotd();

            builder.lore(
                    "&7Leader: &f" + leaderName,
                    "&7Members: &f" + memberCount,
                    "&7Claims: &f" + claimCount,
                    "&7MOTD: &f" + motd
            );

            inv.setItem(slot, builder.build());
            slot++;
        }

        player.openInventory(inv);
    }

    private String getLeaderName(Guild guild) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(guild.getLeader());
        return Optional.ofNullable(op.getName()).orElse(guild.getLeader().toString());
    }

    private Material getGuildIconMaterial(String guildName) {
        String path = "guilds-extra." + guildName + ".icon";
        String def = "PAPER";
        String name = plugin.getConfig().getString(path, def);
        if (name == null) name = def;
        Material m = Material.matchMaterial(name.toUpperCase(Locale.ROOT));
        return m != null ? m : Material.PAPER;
    }
}
