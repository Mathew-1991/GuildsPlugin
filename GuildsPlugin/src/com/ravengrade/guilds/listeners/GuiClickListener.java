package com.ravengrade.guilds.listeners;

import com.ravengrade.guilds.GuildsPlugin;
import com.ravengrade.guilds.data.Guild;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GuiClickListener implements Listener {

    private final GuildsPlugin plugin;

    // These titles must match what GUIManager uses
    private final String MENU_TITLE = ChatColor.DARK_GREEN + "Guild Menu";
    private final String LIST_TITLE = ChatColor.BLUE + "Guild Browser";

    // PDC keys used by GUI items
    private final NamespacedKey ACTION_KEY =
            new NamespacedKey(GuildsPlugin.get(), "guild_gui_action");
    private final NamespacedKey GUILD_NAME_KEY =
            new NamespacedKey(GuildsPlugin.get(), "guild_name");

    public GuiClickListener(GuildsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        String title = view.getTitle();

        // Not one of our GUIs
        if (!title.equals(MENU_TITLE) && !title.equals(LIST_TITLE)) {
            return;
        }

        // Always cancel so items cannot be moved in these GUIs
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (title.equals(MENU_TITLE)) {
            handleMainMenuClick(player, clicked, event.getRawSlot());
        } else if (title.equals(LIST_TITLE)) {
            handleBrowserClick(player, clicked);
        }
    }

    // ----------------------------------------------------
    // Main Guild Menu
    // ----------------------------------------------------

    private void handleMainMenuClick(Player player, ItemStack clicked, int slot) {
        ItemMeta meta = clicked.getItemMeta();

        // Prefer PDC action tags (new GUI), fall back to slot-based (old layout)
        if (meta != null && meta.getPersistentDataContainer().has(ACTION_KEY, PersistentDataType.STRING)) {
            String action = meta.getPersistentDataContainer().get(ACTION_KEY, PersistentDataType.STRING);
            if (action == null) return;

            switch (action.toLowerCase()) {
                case "info":
                    player.closeInventory();
                    player.performCommand("guild info");
                    break;

                case "claims":
                    player.closeInventory();
                    player.performCommand("guild claims");
                    break;

                case "sethome":
                    player.closeInventory();
                    player.performCommand("guild sethome");
                    break;

                case "home":
                    player.closeInventory();
                    player.performCommand("guild home");
                    break;

                case "claim":
                    player.closeInventory();
                    player.performCommand("guild claim");
                    break;

                case "unclaim":
                    player.closeInventory();
                    player.performCommand("guild unclaim");
                    break;

                case "invitemode":
                    player.closeInventory();
                    player.performCommand("guild invitemode");
                    break;

                case "motd":
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Use /guild motd set <text> to change your guild MOTD.");
                    break;

                case "list":
                    player.closeInventory();
                    player.performCommand("guild list");
                    break;

                case "disband":
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "To disband your guild, type /guild disband confirm");
                    break;

                default:
                    // Unknown action tag – do nothing
                    break;
            }

            return;
        }

        // Fallback for old GUI that used fixed slots (kept exactly as before)
        switch (slot) {
            case 10: // Members & Ranks
                player.closeInventory();
                player.performCommand("guild info");
                break;

            case 11: // Claims & Map
                player.closeInventory();
                player.performCommand("guild claims");
                break;

            case 12: // Settings
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Use /guild motd, /guild invitemode and /guild rename for settings.");
                break;

            case 13: // Logs
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Logs GUI is not implemented yet. Use /gadmin claims/info for admin views.");
                break;

            case 16: // Disband warning
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "To disband your guild, type /guild disband confirm");
                break;

            default:
                break;
        }
    }

    // ----------------------------------------------------
    // Guild Browser (Guild List)
    // ----------------------------------------------------

    private void handleBrowserClick(Player player, ItemStack clicked) {
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        String guildName = null;

        // Prefer PDC guild_name tag (new GUI)
        if (meta.getPersistentDataContainer().has(GUILD_NAME_KEY, PersistentDataType.STRING)) {
            guildName = meta.getPersistentDataContainer().get(GUILD_NAME_KEY, PersistentDataType.STRING);
        }

        // Fallback to displayName (old GUI)
        if (guildName == null && meta.hasDisplayName()) {
            String rawName = ChatColor.stripColor(meta.getDisplayName());
            guildName = rawName.trim();
        }

        if (guildName == null || guildName.isEmpty()) {
            return;
        }

        Guild guild = plugin.getGuildManager().getGuild(guildName);
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "Guild not found.");
            return;
        }

        player.closeInventory();
        player.sendMessage(ChatColor.GOLD + "Guild: " + guild.getName());
        player.sendMessage(ChatColor.YELLOW + "Members: " + guild.getMembers().size()
                + " | Claims: " + guild.getClaims().size());
        player.sendMessage(ChatColor.YELLOW + "MOTD: " + guild.getMotd());
        player.sendMessage(ChatColor.GRAY + "Ask the leader or officers for an invite if the guild is invite-only.");
    }
}
