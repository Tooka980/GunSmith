package com.gunsmith.service;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.config.WeaponConfig;
import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.SmallFireball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class AirstrikeService {
    private final GunSmithPlugin plugin;
    private final VisualService visuals;
    private final Random rng = new Random();

    public AirstrikeService(GunSmithPlugin plugin, VisualService visuals){ this.plugin = plugin; this.visuals = visuals; }

    public void call(Location target, WeaponConfig w, int waves, int callsPerWave, int interval, int height, double spread){
        new BukkitRunnable(){
            int wave = 0;
            @Override public void run(){
                if (++wave > Math.max(1, waves)) { cancel(); return; }
                for (int i=0;i<Math.max(1,callsPerWave);i++){
                    double dx = (rng.nextDouble()*2-1)*spread;
                    double dz = (rng.nextDouble()*2-1)*spread;
                    Location spawn = target.clone().add(dx, height, dz);
                    var fb = target.getWorld().spawn(spawn, SmallFireball.class, f -> {
                        Vector dir = target.toVector().subtract(spawn.toVector()).normalize();
                        f.setVelocity(dir.multiply(1.2));
                    });
                }
            }
        }.runTaskTimer(plugin, 0L, Math.max(1, interval));
    }
}
