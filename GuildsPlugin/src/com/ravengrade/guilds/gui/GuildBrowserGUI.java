// src/com/ravengrade/guilds/gui/GuildBrowserGUI.java
package com.ravengrade.guilds.gui;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.Guild;
import com.ravengrade.guilds.util.ItemBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class GuildBrowserGUI {

    private final GuildsPlugin plugin;
    private final Player player;

    public GuildBrowserGUI(GuildsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Guild Browser");

        List<Guild> guilds = new ArrayList<>(plugin.getGuildManager().getAllGuilds());
        int index = 0;

        for (Guild g : guilds) {
            if (index >= 45) break;
            String motd = g.getMotd();
            if (motd == null || motd.isEmpty()) motd = "No MOTD set.";

            inv.setItem(index, new ItemBuilder(Material.PAPER)
                    .name("&e" + g.getName())
                    .lore("&7Leader: &f" + g.getLeader(),
                          "&7Members: &f" + g.getMembers().size(),
                          "&7Claims: &f" + g.getClaims().size(),
                          "&7MOTD: &f" + motd).build());
            index++;
        }

        inv.setItem(53, new ItemBuilder(Material.OAK_SIGN)
                .name("&aInfo")
                .lore("&7Browse all guilds.",
                      "&7Use commands to join / manage.",
                      "&7Open/Invite-only logic is via /guild invitemode").build());

        player.openInventory(inv);
    }
}
