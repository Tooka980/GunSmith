package com.gunsmith.service;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.config.WeaponConfig;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

public class ProjectileService implements Listener {
    private final GunSmithPlugin plugin;
    private final WeaponRegistry registry;
    private final ExplosionService explosions;
    private final NamespacedKey keyWeaponId;
    private final NamespacedKey keyShooter;
    private final NamespacedKey keyRangeTicks;
    private final NamespacedKey keyImpactExplode;
    private final NamespacedKey keyImpactDelay;

    public ProjectileService(GunSmithPlugin plugin, WeaponRegistry registry, ExplosionService explosions){
        this.plugin = plugin; this.registry = registry; this.explosions = explosions;
        this.keyWeaponId = new NamespacedKey(plugin, "weapon_id");
        this.keyShooter = new NamespacedKey(plugin, "shooter_uuid");
        this.keyRangeTicks = new NamespacedKey(plugin, "range_ticks");
        this.keyImpactExplode = new NamespacedKey(plugin, "impact_explode");
        this.keyImpactDelay = new NamespacedKey(plugin, "impact_delay");
    }

    public void attach(Projectile proj, Player shooter, WeaponConfig w){
        var pdc = proj.getPersistentDataContainer();
        pdc.set(keyWeaponId, PersistentDataType.STRING, w.id);
        pdc.set(keyShooter, PersistentDataType.STRING, shooter.getUniqueId().toString());
        pdc.set(keyRangeTicks, PersistentDataType.INTEGER, Math.max(1, w.rangeTicks));
        pdc.set(keyImpactExplode, PersistentDataType.INTEGER, w.impactExplodeEnabled ? 1 : 0);
        pdc.set(keyImpactDelay, PersistentDataType.INTEGER, w.impactExplodeDelay);

        // lifetime removal
        new BukkitRunnable(){ @Override public void run(){
            if (!proj.isValid() || proj.isDead()) return;
            proj.remove();
            if (w.explosionSection != null){
                org.bukkit.entity.Player owner = null; try{ java.util.UUID uid = java.util.UUID.fromString(pdc.get(keyShooter, org.bukkit.persistence.PersistentDataType.STRING)); owner = org.bukkit.Bukkit.getPlayer(uid);}catch(Exception ignored){}
            explosions.detonate(proj.getLocation(), w, owner);
            }
        }}.runTaskLater(plugin, Math.max(1, w.projectileLifespanTicks));
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e){
        Projectile proj = e.getEntity();
        var pdc = proj.getPersistentDataContainer();
        if (!pdc.has(keyWeaponId, PersistentDataType.STRING)) return;
        String id = pdc.get(keyWeaponId, PersistentDataType.STRING);
        WeaponConfig w = registry.getById(id);
        if (w == null) return;

        // If hit entity and it's not an arrow default damage (e.g., snowball), we handle damage here
        if (e.getHitEntity() instanceof LivingEntity le){
            // Do nothing here; EntityDamageByEntityEvent will adjust damage multiplier
            if (w.impactExplodeEnabled){
                int delay = pdc.getOrDefault(keyImpactDelay, PersistentDataType.INTEGER, 0);
                new BukkitRunnable(){ @Override public void run(){
                    if (proj.isValid()) proj.remove();
                    if (w.explosionSection != null) org.bukkit.entity.Player owner = null; try{ java.util.UUID uid = java.util.UUID.fromString(pdc.get(keyShooter, org.bukkit.persistence.PersistentDataType.STRING)); owner = org.bukkit.Bukkit.getPlayer(uid);}catch(Exception ignored){}
                    explosions.detonate(le.getLocation(), w, owner);
                }}.runTaskLater(plugin, Math.max(0, delay));
            }
        } else if (e.getHitBlock() != null){
            // no penetration/ricochet -> despawn and maybe explode
            proj.remove();
            if (w.impactExplodeEnabled && w.explosionSection != null){
                int delay = pdc.getOrDefault(keyImpactDelay, PersistentDataType.INTEGER, 0);
                new BukkitRunnable(){ @Override public void run(){
                    org.bukkit.entity.Player owner = null; try{ java.util.UUID uid = java.util.UUID.fromString(pdc.get(keyShooter, org.bukkit.persistence.PersistentDataType.STRING)); owner = org.bukkit.Bukkit.getPlayer(uid);}catch(Exception ignored){}
                    explosions.detonate(e.getHitBlock().getLocation().add(0.5,0.5,0.5), w, owner);
                }}.runTaskLater(plugin, Math.max(0, delay));
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e){
        if (!(e.getDamager() instanceof Projectile proj)) return;
        var pdc = proj.getPersistentDataContainer();
        if (!pdc.has(keyWeaponId, PersistentDataType.STRING)) return;
        String id = pdc.get(keyWeaponId, PersistentDataType.STRING);
        WeaponConfig w = registry.getById(id);
        if (w == null) return;

        if (e.getEntity() instanceof LivingEntity le){
            double base = w.damageBase;
            // weapon base + (arrow base already applied by Bukkit) -> we override with weapon+ammo total
            double current = base;
            // No direct access to last ammo damage here (per-player). We'll keep arrow.setDamage earlier.
            // Apply hit-location multiplier
            double mult = resolveMultiplier(proj, le, w);
            e.setDamage(Math.max(0.0, e.getDamage() * mult)); // scale Bukkit's computed damage
            // Optional: custom knockback etc could be added
        }
    }

    private double resolveMultiplier(Projectile proj, LivingEntity target, WeaponConfig w){
        // Rough heuristic: compare impact y to target height
        double baseY = target.getLocation().getY();
        double h = target.getHeight();
        double hitY = proj.getLocation().getY();
        double t = (hitY - baseY) / Math.max(0.001, h);
        if (t >= 0.8) return w.headMultiplier;      // top 20% = head
        if (t <= 0.2) return w.limbMultiplier;      // bottom 20% = limb
        return w.bodyMultiplier;                    // middle = body
    }
}
