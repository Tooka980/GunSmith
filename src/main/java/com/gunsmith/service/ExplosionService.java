package com.gunsmith.service;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.config.WeaponConfig;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class ExplosionService {
    private final GunSmithPlugin plugin;
    private final VisualService visuals;
    public ExplosionService(GunSmithPlugin plugin, VisualService visuals){ this.plugin = plugin; this.visuals = visuals; }

    public void detonate(Location loc, WeaponConfig w){
        ConfigurationSection exp = w.explosionSection;
        double radius = exp != null ? exp.getDouble("Radius", 3.0) : 3.0;
        double dmgCenter = exp != null ? exp.getDouble("Damage_At_Center", 8.0) : 8.0;
        double dmgEdge   = exp != null ? exp.getDouble("Damage_At_Edge", 2.0) : 2.0;

        World world = loc.getWorld();
        for (Entity e : world.getNearbyEntities(loc, radius, radius, radius)){
            if (e instanceof LivingEntity le){
                double dist = le.getLocation().distance(loc);
                double exposureFactor = 1.0;
                String mode = w.explosionExposure==null? "DEFAULT" : w.explosionExposure.toUpperCase();
                switch (mode){
                    case "NONE" -> exposureFactor = 1.0;
                    case "DISTANCE" -> exposureFactor = Math.max(0.0, 1.0 - (dist / radius));
                    default -> exposureFactor = Math.max(0.0, 1.0 - (dist / radius));
                }
                double dmg = (dmgCenter * exposureFactor) + (dmgEdge * (1.0 - exposureFactor));
                le.damage(Math.max(0.0, dmg));
                double kbMul = Math.max(0.0, w.explosionKnockbackMultiplier<=0?1.0:w.explosionKnockbackMultiplier);
                Vector knock = le.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(0.6*exposureFactor*kbMul);
                le.setVelocity(le.getVelocity().add(knock));
            }
        }
        world.spawnParticle(Particle.EXPLOSION, loc, 1);
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    }
        // OnExplode visuals (if defined), else default
        if (w.visualSection != null && w.visualSection.getConfigurationSection("Particles.OnExplode") != null){
            // simple 1 particle fallback
            world.spawnParticle(Particle.EXPLOSION, loc, 1);
        } else {
            world.spawnParticle(Particle.EXPLOSION, loc, 1);
        }
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    }
}
