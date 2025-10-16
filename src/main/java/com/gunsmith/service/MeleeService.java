package com.gunsmith.service;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.config.WeaponConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * Minimal melee implementation:
 * - Left click air/block to melee.
 * - Uses WeaponConfig.melee* fields (must exist).
 * - Does not depend on AmmoService (compiles even if its API changes).
 */
public class MeleeService implements Listener {

    private final GunSmithPlugin plugin;
    private final WeaponRegistry registry;

    private final Map<java.util.UUID, Long> lastHit = new HashMap<>();

    public MeleeService(GunSmithPlugin plugin, WeaponRegistry registry /*, AmmoService ammo if needed */) {
        this.plugin = plugin;
        this.registry = registry;
        // ammo は今回は未使用（依存を減らしてビルド安定化）
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Action a = e.getAction();
        if (a != Action.LEFT_CLICK_AIR && a != Action.LEFT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        WeaponConfig w = registry.getHeldWeapon(p);
        if (w == null) return;

        // 近接が無効＆アタッチメントも無しなら抜ける
        if (!w.meleeEnable && w.meleeAttachmentId == null) return;

        long now = System.currentTimeMillis();
        Long until = lastHit.get(p.getUniqueId());
        if (until != null && now < until) return; // クールダウン中

        double range = Math.max(1.0, (w.meleeRange <= 0 ? 3.0 : w.meleeRange));
        Vector dir = p.getEyeLocation().getDirection().normalize();
        Vector start = p.getEyeLocation().toVector();
        Vector end = start.clone().add(dir.multiply(range));
        BoundingBox box = BoundingBox.of(start.toLocation(p.getWorld()), end.toLocation(p.getWorld())).expand(0.6);

        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (var ent : p.getWorld().getNearbyEntities(box)) {
            if (ent instanceof LivingEntity le && !le.getUniqueId().equals(p.getUniqueId())) {
                double d = le.getLocation().distanceSquared(p.getEyeLocation());
                if (d < closestDist) { closestDist = d; closest = le; }
            }
        }

        if (closest != null) {
            double dmg = (w.meleeDamageBase != null ? w.meleeDamageBase : w.damageBase);
            closest.damage(Math.max(0.0, dmg), p);
            lastHit.put(p.getUniqueId(), now + Math.max(0, w.meleeHitDelay) * 50L);
        } else {
            lastHit.put(p.getUniqueId(), now + Math.max(0, w.meleeMissDelay) * 50L);
            if (w.meleeConsumeOnMiss) {
                // 将来 AmmoService に接続する場合はここで弾／耐久消費を実装
            }
        }
    }
}
