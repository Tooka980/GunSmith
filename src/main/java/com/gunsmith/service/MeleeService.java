package com.gunsmith.service;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.config.WeaponConfig;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MeleeService implements Listener {
    private final GunSmithPlugin plugin;
    private final WeaponRegistry registry;
    private final AmmoService ammo;
    private final Map<UUID, Long> lastHit = new HashMap<>();

    public MeleeService(GunSmithPlugin plugin, WeaponRegistry registry, AmmoService ammo){
        this.plugin = plugin; this.registry = registry; this.ammo = ammo;
    }

    @EventHandler
    public void onMelee(PlayerInteractEvent e){
        if (e.getHand()!=EquipmentSlot.HAND) return;
        if (!e.getAction().isLeftClick()) return;
        Player p = e.getPlayer();
        var w = registry.getHeldWeapon(p);
        if (w == null) return;
        if (!w.meleeEnable && w.meleeAttachmentId == null) return;

        long now = System.currentTimeMillis();
        long gate = lastHit.getOrDefault(p.getUniqueId(), 0L);
        if (now < gate) return;

        double range = Math.max(1.0, w.meleeRange<=0?3.0:w.meleeRange);
        Location eye = p.getEyeLocation();
        Vector dir = eye.getDirection();
        RayTraceResult res = p.getWorld().rayTrace(eye, dir, range, FluidCollisionMode.NEVER, false, 0.2, ent -> ent!=p);

        boolean hit = false;
        if (res != null && res.getHitEntity() instanceof LivingEntity le){
            double dmg = (w.meleeDamageBase!=null ? w.meleeDamageBase : w.damageBase);
            le.damage(Math.max(0.0, dmg), p);
            hit = true;
            lastHit.put(p.getUniqueId(), now + w.meleeHitDelay*50L);
        } else {
            // Miss mechanics placeholder
            lastHit.put(p.getUniqueId(), now + w.meleeMissDelay*50L);
            if (w.meleeConsumeOnMiss){
                ammo.consumeIfAllowed(p, w); // consume 1 ammo if configured
            }
        }
    }
}
