package com.gunsmith.gui;

import com.gunsmith.service.WeaponRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class WeaponsGuiListener implements Listener {
    private final WeaponRegistry registry;

    public WeaponsGuiListener(WeaponRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof WeaponsGui gui)) return;
        e.setCancelled(true); // GUI内のクリックは全部キャンセル

        if (e.getClickedInventory() != e.getView().getTopInventory()) return;
        if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;

        var item = e.getCurrentItem();
        var type = item.getType();
        var p = (Player) e.getWhoClicked();

        // ページング矢印
        if (type == Material.ARROW && item.getItemMeta() != null && item.getItemMeta().getDisplayName() != null) {
            String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            int next = gui.getPage();
            if (name.contains("前のページ")) next = Math.max(0, next - 1);
            if (name.contains("次のページ")) next = next + 1;

            p.openInventory(new WeaponsGui(registry, next).getInventory());
            return;
        }

        // 武器アイコン：/gm give
        if (item.getItemMeta() != null && item.getItemMeta().getDisplayName() != null) {
            String id = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            // displayName はアイコンで id を設定している（AQUA + id）
            String weaponId = id.trim();
            p.performCommand("gm give " + weaponId);
        }
    }
}
