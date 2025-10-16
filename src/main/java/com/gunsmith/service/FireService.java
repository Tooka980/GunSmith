package com.gunsmith.service;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.config.WeaponConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FireService {

    private final GunSmithPlugin plugin;
    private final WeaponRegistry registry;
    private final HomingService homingService;
    private final VisualService visualService;
    private final AmmoService ammoService;
    private final Map<UUID, Integer> currentModeIndex = new HashMap<>();

    public FireService(GunSmithPlugin plugin, WeaponRegistry registry, HomingService homingService, VisualService visualService, AmmoService ammoService) {
        this.plugin = plugin;
        this.registry = registry;
        this.homingService = homingService;
        this.visualService = visualService;
        this.ammoService = ammoService;
    }

    public void cycleFireMode(Player p, WeaponConfig w) {
        if (w.selectFireOrder == null || w.selectFireOrder.isEmpty()) return;
        int idx = currentModeIndex.getOrDefault(p.getUniqueId(), 0);
        idx = (idx + 1) % w.selectFireOrder.size();
        currentModeIndex.put(p.getUniqueId(), idx);
        String mode = w.selectFireOrder.get(idx);
        p.sendActionBar(Component.text("Mode: " + mode, NamedTextColor.GOLD));
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.6f);
    }

    public String getActiveMode(Player p, WeaponConfig w) {
        if (w.selectFireOrder == null || w.selectFireOrder.isEmpty()) return "SEMI";
        int idx = currentModeIndex.getOrDefault(p.getUniqueId(), 0);
        idx = Math.min(idx, w.selectFireOrder.size()-1);
        return w.selectFireOrder.get(idx);
    }

    public void handleTrigger(Player p, WeaponConfig w) {
        String mode = getActiveMode(p, w);
        switch (mode) {
            case "AUTO" -> autoFire(p, w);
            case "BURST" -> burstFire(p, w);
            case "CHARGE" -> semiFire(p, w);
            default -> semiFire(p, w);
        }
    }

    private void autoFire(Player p, WeaponConfig w) {
        int interval = Math.max(1, (int)Math.round(20.0 / Math.max(0.1, w.ratePerSec)));
        new BukkitRunnable() {
            @Override public void run() {
                if (!p.isOnline() || !p.isHandRaised()) { cancel(); return; }
                if (!shootOnce(p, w)) { cancel(); }
            }
        }.runTaskTimer(plugin, 0L, interval);
    }

    private void burstFire(Player p, WeaponConfig w) {
        int interval = Math.max(1, w.burstInterval);
        new BukkitRunnable() {
            int left = Math.max(1, w.burstCount);
            @Override public void run() {
                if (!p.isOnline()) { cancel(); return; }
                if (!shootOnce(p, w)) { cancel(); return; }
                if (--left <= 0) cancel();
            }
        }.runTaskTimer(plugin, 0L, interval);
    }

    private void semiFire(Player p, WeaponConfig w) { shootOnce(p, w); }

    private boolean shootOnce(Player p, WeaponConfig w) {
        if (!ammoService.consumeIfAllowed(p, w)) { visualService.playEmpty(p); return false; }
        boolean ads = w.sneakAsADS && p.isSneaking();
        Vector dir = p.getEyeLocation().getDirection().normalize();
        double base = ads ? 0.16 : 0.30;
        double yaw = (Math.random()-0.5) * Math.toRadians(base*2);
        double pitch = (Math.random()-0.5) * Math.toRadians(base*2);
        dir.add(new Vector(yaw, pitch, 0)).normalize();
        if ("ITEM".equalsIgnoreCase(w.mode)) return fireItemProjectile(p, w, dir);
        return firePhysicalProjectile(p, w, dir);
    }

    private boolean firePhysicalProjectile(Player p, WeaponConfig w, Vector dir) {
        Arrow arrow = p.getWorld().spawn(p.getEyeLocation().add(dir.multiply(0.2)), Arrow.class, a -> {
            a.setShooter(p);
            a.setVelocity(dir.multiply(3.0));
            a.setGravity(w.gravityEnabled);
            a.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            a.setPierceLevel((byte)Math.max(0, w.penetrationMaxEntities));
        });
        visualService.playShoot(p.getLocation(), w);
        if (w.homingEnabled) homingService.trackHoming(arrow, p, w);
        return true;
    }

    private boolean fireItemProjectile(Player p, WeaponConfig w, Vector dir) {
        var world = p.getWorld();
        var item = registry.createWeaponItem(w); // visual; could use Item.Material from YAML (omitted MVP)
        var drop = world.dropItem(p.getEyeLocation().add(dir.multiply(0.2)), item);
        double speed = 1.1;
        drop.setVelocity(dir.multiply(speed));
        drop.setCanPlayerPickup(false);
        drop.setPickupDelay(Integer.MAX_VALUE);
        visualService.playShoot(p.getLocation(), w);
        // TODO: Explosion fuse + cluster + airstrike payloads
        return true;
    }
}
