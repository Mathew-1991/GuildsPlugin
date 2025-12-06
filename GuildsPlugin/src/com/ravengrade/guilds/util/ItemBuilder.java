package com.ravengrade.guilds.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.ravengrade.guilds.GuildsPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
    }

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
    }

    public ItemBuilder name(String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(String... lines) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        List<String> out = new ArrayList<>();
        for (String l : Arrays.asList(lines)) {
            out.add(ChatColor.translateAlternateColorCodes('&', l));
        }

        meta.setLore(out);
        item.setItemMeta(meta);
        return this;
    }

    /** Adds an enchantment (used for glowing GUI items) */
    public ItemBuilder enchant(Enchantment ench, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;
        meta.addEnchant(ench, level, true);
        item.setItemMeta(meta);
        return this;
    }

    /** Makes the item have a glowing effect without showing enchant text */
    public ItemBuilder glow() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
        return this;
    }

    /** Hides all attributes for clean GUI items */
    public ItemBuilder hideAllFlags() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return this;
    }

    /** Sets custom model data for resource-pack based icons */
    public ItemBuilder model(int data) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        meta.setCustomModelData(data);
        item.setItemMeta(meta);
        return this;
    }

    /** Stores a string in the item's PersistentDataContainer */
    public ItemBuilder tag(GuildsPlugin plugin, String key, String value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        NamespacedKey nsk = new NamespacedKey(plugin, key);
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.STRING, value);

        item.setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return item;
    }
}
