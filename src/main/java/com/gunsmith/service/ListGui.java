package com.gunsmith.service;

import com.gunsmith.config.WeaponConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ListGui implements Listener {
    private final com.gunsmith.GunSmithPlugin plugin; private final WeaponRegistry registry;
    private final NamespacedKey keyPage; private final NamespacedKey keyGui;
    public ListGui(com.gunsmith.GunSmithPlugin plugin, WeaponRegistry registry){
        this.plugin=plugin; this.registry=registry;
        this.keyPage = new NamespacedKey(plugin, "page");
        this.keyGui  = new NamespacedKey(plugin, "gm_list_gui");
    }
    public void open(Player p, int page){
        Inventory inv = Bukkit.createInventory(p, 27, Component.text("GunSmith - Weapons"));
        build(inv, page);
        p.openInventory(inv);
    }
    private void build(Inventory inv, int page){
        inv.clear();
        List<WeaponConfig> list = new ArrayList<>(registry.getAll());
        list.sort(Comparator.comparing(a -> a.id.toLowerCase(Locale.ROOT)));
        int[] slots = {1,2,3,4,5,6,7, 10,11,12,13,14,15,16, 19,20,21,22,23,24,25};
        int perPage = slots.length;
        int pages = Math.max(1, (int)Math.ceil(list.size()/(double)perPage));
        page = Math.max(0, Math.min(page, pages-1));
        ItemStack glass = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta(); gm.displayName(Component.text(" "));
        gm.getPersistentDataContainer().set(keyGui, PersistentDataType.INTEGER, 1);
        gm.getPersistentDataContainer().set(keyPage, PersistentDataType.INTEGER, page);
        glass.setItemMeta(gm);
        inv.setItem(0, glass.clone()); inv.setItem(18, glass.clone());
        inv.setItem(8, glass.clone()); inv.setItem(26, glass.clone());
        ItemStack left = new ItemStack(Material.ARROW);
        ItemMeta lm = left.getItemMeta(); lm.displayName(Component.text("左へ"));
        lm.getPersistentDataContainer().set(keyGui, PersistentDataType.INTEGER, 1);
        lm.getPersistentDataContainer().set(keyPage, PersistentDataType.INTEGER, page-1);
        left.setItemMeta(lm); inv.setItem(9, left);
        ItemStack right = new ItemStack(Material.ARROW);
        ItemMeta rm = right.getItemMeta(); rm.displayName(Component.text("右へ"));
        rm.getPersistentDataContainer().set(keyGui, PersistentDataType.INTEGER, 1);
        rm.getPersistentDataContainer().set(keyPage, PersistentDataType.INTEGER, page+1);
        right.setItemMeta(rm); inv.setItem(17, right);
        int start = page*perPage;
        for (int i=0;i<perPage;i++){
            int idx = start+i; if (idx>=list.size()) break;
            WeaponConfig w = list.get(idx);
            ItemStack it = registry.createWeaponItem(w);
            ItemMeta im = it.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY+"ID: "+w.id);
            lore.add(ChatColor.GRAY+"Mode: "+w.mode+"  Rate/s: "+w.ratePerSec);
            lore.add(ChatColor.GRAY+"Mag: "+w.magCapacity+"  HSx: "+w.hsMultiplier);
            im.setLore(lore); it.setItemMeta(im);
            inv.setItem(slots[i], it);
        }
    }
    @EventHandler public void onClick(InventoryClickEvent e){
        if (e.getClickedInventory()==null) return;
        ItemStack clicked = e.getCurrentItem(); if (clicked==null) return;
        var meta = clicked.getItemMeta(); if (meta==null) return;
        var pdc = meta.getPersistentDataContainer();
        if (!pdc.has(keyGui, PersistentDataType.INTEGER)){
            // Weapon clicked -> give one
            if (e.getWhoClicked() instanceof Player p){
                WeaponConfig w = registry.matchWeapon(clicked);
                if (w != null){
                    p.getInventory().addItem(registry.createWeaponItem(w));
                    p.sendMessage("§aGiven: §e"+w.id);
                }
            }
            e.setCancelled(true);
            return;
        }
        e.setCancelled(true);
        Integer next = pdc.get(keyPage, PersistentDataType.INTEGER); if (next==null) return;
        if (e.getWhoClicked() instanceof Player p) open(p, next);
    }
}
