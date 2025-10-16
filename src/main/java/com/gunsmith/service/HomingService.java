package com.gunsmith.service;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.config.WeaponConfig;
import org.bukkit.entity.AbstractArrow;
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
            int age=0;
            @Override public void run(){
                if (proj.isDead() || !proj.isValid()) { cancel(); return; }
                if (++age > w.homingMaxTicks) { cancel(); return; }
                var eye = shooter.getEyeLocation();
                if (eye.getWorld()!=proj.getWorld()) { cancel(); return; }
                if (eye.distanceSquared(proj.getLocation()) > w.homingMaxDistance*w.homingMaxDistance) { cancel(); return; }
                var to = eye.getDirection().normalize();
                var v = proj.getVelocity().normalize();
                double turn = Math.toRadians(w.homingTurnRateDeg);
                Vector blended = v.multiply(Math.cos(turn)).add(to.multiply(Math.sin(turn))).normalize();
                proj.setVelocity(blended.multiply(proj.getVelocity().length()));
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
