
package com.gunsmith.service;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.config.WeaponConfig;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataContainer;
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
        this.plugin = plugin;
        this.registry = registry;
        this.explosions = explosions;
        this.keyWeaponId = new NamespacedKey(plugin, "weapon_id");
        this.keyShooter = new NamespacedKey(plugin, "shooter_uuid");
        this.keyRangeTicks = new NamespacedKey(plugin, "range_ticks");
        this.keyImpactExplode = new NamespacedKey(plugin, "impact_explode");
        this.keyImpactDelay = new NamespacedKey(plugin, "impact_delay");
    }

    public void attach(final Projectile proj, final Player shooter, final WeaponConfig w){
        final PersistentDataContainer pdc = proj.getPersistentDataContainer();
        pdc.set(keyWeaponId, PersistentDataType.STRING, w.id);
        pdc.set(keyShooter, PersistentDataType.STRING, shooter.getUniqueId().toString());
        pdc.set(keyRangeTicks, PersistentDataType.INTEGER, Math.max(1, w.rangeTicks));
        pdc.set(keyImpactExplode, PersistentDataType.INTEGER, w.impactExplodeEnabled ? 1 : 0);
        pdc.set(keyImpactDelay, PersistentDataType.INTEGER, Math.max(0, w.impactExplodeDelay));

        // lifetime removal + timed explosion fallback
        new BukkitRunnable(){
            @Override public void run(){
                if (!proj.isValid() || proj.isDead()) return;
                proj.remove();
                if (w.explosionSection != null){
                    Player owner = resolveOwner(pdc);
                    explosions.detonate(proj.getLocation(), w, owner);
                }
            }
        }.runTaskLater(plugin, Math.max(1, w.projectileLifespanTicks));
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e){
        Projectile proj = e.getEntity();
        PersistentDataContainer pdc = proj.getPersistentDataContainer();
        if (!pdc.has(keyWeaponId, PersistentDataType.STRING)) return;

        String id = pdc.get(keyWeaponId, PersistentDataType.STRING);
        WeaponConfig w = registry.getById(id);
        if (w == null) return;

        boolean impactExplode = pdc.getOrDefault(keyImpactExplode, PersistentDataType.INTEGER, 0) == 1;
        int delay = pdc.getOrDefault(keyImpactDelay, PersistentDataType.INTEGER, 0);
        final Player owner = resolveOwner(pdc);

        if (e.getHitEntity() instanceof LivingEntity le){
            if (impactExplode && w.explosionSection != null){
                new BukkitRunnable(){
                    @Override public void run(){
                        if (proj.isValid()) proj.remove();
                        explosions.detonate(le.getLocation(), w, owner);
                    }
                }.runTaskLater(plugin, Math.max(0, delay));
            }
        } else if (e.getHitBlock() != null){
            // no penetration/ricochet logic -> despawn and maybe explode
            proj.remove();
            if (impactExplode && w.explosionSection != null){
                new BukkitRunnable(){
                    @Override public void run(){
                        explosions.detonate(e.getHitBlock().getLocation().add(0.5,0.5,0.5), w, owner);
                    }
                }.runTaskLater(plugin, Math.max(0, delay));
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e){
        if (!(e.getDamager() instanceof Projectile proj)) return;
        PersistentDataContainer pdc = proj.getPersistentDataContainer();
        if (!pdc.has(keyWeaponId, PersistentDataType.STRING)) return;
        String id = pdc.get(keyWeaponId, PersistentDataType.STRING);
        WeaponConfig w = registry.getById(id);
        if (w == null) return;

        if (e.getEntity() instanceof LivingEntity le){
            // Apply hit-location multiplier to existing damage
            double mult = resolveMultiplier(proj, le, w);
            e.setDamage(Math.max(0.0, e.getDamage() * mult));
        }
    }

    private Player resolveOwner(PersistentDataContainer pdc){
        try{
            String s = pdc.get(keyShooter, PersistentDataType.STRING);
            if (s == null) return null;
            UUID uid = UUID.fromString(s);
            return Bukkit.getPlayer(uid);
        }catch (Exception ignored){}
        return null;
    }

    private double resolveMultiplier(Projectile proj, LivingEntity target, WeaponConfig w){
        double baseY = target.getLocation().getY();
        double h = Math.max(0.001, target.getHeight());
        double hitY = proj.getLocation().getY();
        double t = (hitY - baseY) / h;
        if (t >= 0.8) return w.headMultiplier;
        if (t <= 0.2) return w.limbMultiplier;
        return w.bodyMultiplier;
    }
}
