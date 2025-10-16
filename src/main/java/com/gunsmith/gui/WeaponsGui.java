package com.gunsmith.gui;

import com.gunsmith.config.WeaponConfig;
import com.gunsmith.service.WeaponRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WeaponsGui implements InventoryHolder {
    private final WeaponRegistry registry;
    private final int page;           // 0-based
    private final int pageSize = 14;  // 7列×2段（中央列両端は矢）

    public WeaponsGui(WeaponRegistry registry, int page) {
        this.registry = registry;
        this.page = Math.max(0, page);
    }

    @Override
    public Inventory getInventory() {
        List<String> ids = new ArrayList<>(registry.getIds());
        int totalPages = Math.max(1, (int) Math.ceil(ids.size() / (double) pageSize));
        int p = Math.min(page, totalPages - 1);

        String title = ChatColor.GOLD + "GunSmith — Weapons (" + (p+1) + "/" + totalPages + ")";
        Inventory inv = Bukkit.createInventory(this, 9*3, title);

        // フレーム（薄灰色ガラス・名前空白）
        ItemStack frame = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = frame.getItemMeta();
        fm.setDisplayName(" ");
        frame.setItemMeta(fm);

        int[] edgeSlots = {0, 9, 18, 8, 17, 26}; // 左縦(0,9,18)・右縦(8,17,26)
        for (int s : edgeSlots) inv.setItem(s, frame);

        // ページング矢印
        inv.setItem(9, arrowItem(ChatColor.YELLOW + "← 前のページ"));
        inv.setItem(17, arrowItem(ChatColor.YELLOW + "次のページ →"));

        // 武器アイテム配置：スロット候補（中央行の9,17を除く）
        int[] slots = {
                10,11,12,13,14,15,16,
                19,20,21,22,23,24,25
        };

        int start = p * pageSize;
        for (int i = 0; i < pageSize && (start + i) < ids.size(); i++) {
            String id = ids.get(start + i);
            WeaponConfig w = registry.getById(id);
            inv.setItem(slots[i], weaponIcon(id, w));
        }
        return inv;
    }

    public int getPage() { return page; }

    private ItemStack arrowItem(String name) {
        ItemStack it = new ItemStack(Material.ARROW);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(name);
        it.setItemMeta(m);
        return it;
    }

    private ItemStack weaponIcon(String id, WeaponConfig w) {
        Material mat = Material.matchMaterial(
                (w != null && w.material != null ? w.material : "minecraft:iron_horse_armor")
                        .replace("minecraft:","").toUpperCase()
        );
        if (mat == null) mat = Material.IRON_HORSE_ARMOR;

        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        m.setDisplayName(ChatColor.AQUA + id);
        List<String> lore = new ArrayList<>();
        if (w != null) {
            lore.add(ChatColor.GRAY + "Name: " + ChatColor.WHITE + (w.name != null ? w.name : id));
            lore.add(ChatColor.GRAY + "Fire: " + ChatColor.WHITE + (w.selectFireOrder != null ? w.selectFireOrder.toString() : "SEMI"));
            lore.add(ChatColor.GRAY + "RPM: " + ChatColor.WHITE + String.format("%.1f", w.ratePerSec * 60.0));
            lore.add(ChatColor.GRAY + "Mag: " + ChatColor.WHITE + w.magCapacity);
            lore.add(ChatColor.GRAY + "Mode: " + ChatColor.WHITE + w.mode + " (" + w.entityType + ")");
            lore.add(ChatColor.DARK_GRAY + "クリックで /gm give " + id);
        }
        m.setLore(lore);
        it.setItemMeta(m);
        return it;
    }
}
