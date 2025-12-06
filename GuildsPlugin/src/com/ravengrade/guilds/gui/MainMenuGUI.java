// src/com/ravengrade/guilds/gui/MainMenuGUI.java
package com.ravengrade.guilds.gui;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.Guild;
import com.ravengrade.guilds.util.ItemBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MainMenuGUI {

    private final GuildsPlugin plugin;
    private final Player player;
    private final Guild guild;

    public MainMenuGUI(GuildsPlugin plugin, Player player, Guild guild) {
        this.plugin = plugin;
        this.player = player;
        this.guild = guild;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Guild Menu");

        inv.setItem(10, new ItemBuilder(Material.PLAYER_HEAD)
                .name("&aMembers & Ranks")
                .lore("&7View members, ranks, and basic info.").build());

        inv.setItem(11, new ItemBuilder(Material.MAP)
                .name("&aClaims & Map")
                .lore("&7View your guild claims.",
                      "&7Use /guild claims and /guild showborders").build());

        inv.setItem(12, new ItemBuilder(Material.WRITABLE_BOOK)
                .name("&aSettings")
                .lore("&7Use commands:",
                      "&f/guild motd, /guild invitemode, /guild rename").build());

        inv.setItem(13, new ItemBuilder(Material.BOOK)
                .name("&aLogs")
                .lore("&7View guild logs with /guild logs").build());

        inv.setItem(14, new ItemBuilder(Material.PAPER)
                .name("&aInfo")
                .lore("&7Guild: " + (guild == null ? "None" : guild.getName()),
                      "&7MOTD: " + (guild == null ? "" : guild.getMotd()),
                      "&7Claims: " + (guild == null ? "0" : guild.getClaims().size() + "")).build());

        inv.setItem(16, new ItemBuilder(Material.BARRIER)
                .name("&cDisband Guild")
                .lore("&7Use /guild disband to disband your guild.",
                      "&cLeader only.").build());

        player.openInventory(inv);
    }
}
