package com.gunsmith;

import com.gunsmith.config.WeaponConfig;
import com.gunsmith.service.FireService;
import com.gunsmith.service.WeaponRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class GunSmithListener implements Listener {
    private final GunSmithPlugin plugin;
    private final WeaponRegistry registry;
    private final FireService fireService;
    public GunSmithListener(GunSmithPlugin plugin, WeaponRegistry registry, FireService fireService){
        this.plugin = plugin; this.registry = registry; this.fireService = fireService;
    }
    @EventHandler public void onSwap(PlayerSwapHandItemsEvent e){
        Player p = e.getPlayer();
        ItemStack main = p.getInventory().getItemInMainHand();
        WeaponConfig w = registry.matchWeapon(main);
        if (w == null) return;
        e.setCancelled(true);
        fireService.cycleFireMode(p, w);
    }
    @EventHandler public void onInteract(PlayerInteractEvent e){
        if (e.getHand()!= EquipmentSlot.HAND) return;
        var a = e.getAction();
        if (a!=Action.RIGHT_CLICK_AIR && a!=Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        WeaponConfig w = registry.matchWeapon(p.getInventory().getItemInMainHand());
        if (w == null) return;
        e.setCancelled(true);
        fireService.handleTrigger(p, w);
    }
}
