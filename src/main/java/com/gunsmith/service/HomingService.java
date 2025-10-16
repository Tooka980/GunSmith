package com.gunsmith.service;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.config.WeaponConfig;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class HomingService {
    private final GunSmithPlugin plugin;
    private final VisualService visuals;
    public HomingService(GunSmithPlugin plugin, VisualService visuals){ this.plugin = plugin; this.visuals = visuals; }
    public void shutdown(){}

    public void trackHoming(AbstractArrow proj, Player shooter, WeaponConfig w){
        new BukkitRunnable(){
            int age=0, rtCounter=0;
            @Override public void run(){
                if (proj.isDead() || !proj.isValid()) { cancel(); return; }
                if (++age > w.homingMaxTicks) { cancel(); return; }
                Location eye = shooter.getEyeLocation();
                if (eye.getWorld()!=proj.getWorld()) { cancel(); return; }
                if (eye.distanceSquared(proj.getLocation()) > w.homingMaxDistance*w.homingMaxDistance) { cancel(); return; }

                // obstruction check
                if (w.homingLoseOnObstruction){
                    var ray = proj.getWorld().rayTrace(eye, eye.getDirection(), eye.distance(proj.getLocation()), FluidCollisionMode.NEVER, false, 0.1, e -> e==proj || e==shooter);
                    if (ray != null && ray.getHitBlock() != null) { cancel(); return; }
                }

                // Retarget (nearest living around projectile)
                Vector targetDir = eye.getDirection().normalize();
                if (w.homingRetarget && ++rtCounter >= Math.max(1,w.homingRetargetInterval)){
                    rtCounter = 0;
                    LivingEntity nearest = null;
                    double best = Double.MAX_VALUE;
                    for (Entity e : proj.getNearbyEntities(w.homingRetargetRadius, w.homingRetargetRadius, w.homingRetargetRadius)){
                        if (e instanceof LivingEntity le && e != shooter){
                            double d = e.getLocation().distanceSquared(proj.getLocation());
                            if (d < best){ best = d; nearest = le; }
                        }
                    }
                    if (nearest != null){
                        targetDir = nearest.getEyeLocation().toVector().subtract(proj.getLocation().toVector()).normalize();
                    }
                }

                Vector v = proj.getVelocity().normalize();
                double turn = Math.toRadians(w.homingTurnRateDeg);
                Vector blended = v.multiply(Math.cos(turn)).add(targetDir.multiply(Math.sin(turn))).normalize();
                proj.setVelocity(blended.multiply(proj.getVelocity().length()));
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
