
package com.gunsmith.service;

import com.gunsmith.config.WeaponConfig;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ExplosionService {

    private final com.gunsmith.GunSmithPlugin plugin;
    private final VisualService visual;

    public ExplosionService(com.gunsmith.GunSmithPlugin plugin, VisualService visual){
        this.plugin = plugin;
        this.visual = visual;
    }

    public void detonate(Location loc, WeaponConfig w){
        detonate(loc, w, null);
    }

    public void detonate(Location loc, WeaponConfig w, Player owner){
        World world = loc.getWorld();
        if (world == null) return;

        double radius = w.expRadius > 0 ? w.expRadius : (w.explosionSection != null ? w.explosionSection.getDouble("Radius", 3.0) : 3.0);
        double dmgCenter = w.explosionSection != null ? w.explosionSection.getDouble("Damage_At_Center", 8.0) : 8.0;
        double dmgEdge   = w.explosionSection != null ? w.explosionSection.getDouble("Damage_At_Edge", 2.0) : 2.0;

        for (Entity e : world.getNearbyEntities(loc, radius, radius, radius)){
            if (e instanceof LivingEntity le){
                // Owner immunity (only if owner provided)
                if (owner != null && w.damageOwnerImmunity && le.getUniqueId().equals(owner.getUniqueId())){
                    continue;
                }
                double dist = le.getLocation().distance(loc);
                double exposureFactor;
                String mode = w.explosionExposure == null ? "DEFAULT" : w.explosionExposure.toUpperCase();
                switch (mode){
                    case "NONE" -> exposureFactor = 1.0;
                    case "DISTANCE" -> exposureFactor = Math.max(0.0, 1.0 - (dist / Math.max(0.001, radius)));
                    default -> exposureFactor = Math.max(0.0, 1.0 - (dist / Math.max(0.001, radius)));
                }
                double dmg = (dmgCenter * exposureFactor) + (dmgEdge * (1.0 - exposureFactor));
                if (dmg < 0) dmg = 0;
                le.damage(dmg, owner != null ? owner : null);

                // Fire ticks from Damage section (if configured globally)
                if (w.damageFireTicks > 0){
                    le.setFireTicks(Math.max(le.getFireTicks(), w.damageFireTicks));
                }

                double kbMul = w.explosionKnockbackMultiplier <= 0 ? 1.0 : w.explosionKnockbackMultiplier;
                Vector knock = le.getLocation().toVector().subtract(loc.toVector()).normalize()
                        .multiply(0.6 * exposureFactor * kbMul);
                le.setVelocity(le.getVelocity().add(knock));
            }
        }

        // Visuals: simple fallback (Particles/OnExplode can be added later without compile errors)
        world.spawnParticle(Particle.EXPLOSION, loc, 1);
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    }
}
