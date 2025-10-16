package com.gunsmith.service;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.config.WeaponConfig;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class VisualService {
    private final GunSmithPlugin plugin;
    public VisualService(GunSmithPlugin plugin){ this.plugin = plugin; }

    public void playShoot(Location loc, WeaponConfig w){
        if (w.visualSection == null) return;
        playSounds(loc, w.visualSection.getConfigurationSection("Sounds.OnShoot"));
        playParticles(loc, w.visualSection.getConfigurationSection("Particles.OnShoot"));
    }
    public void playEmpty(Player p){ p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.0f, 1.8f); }

    private void playSounds(Location loc, ConfigurationSection s){
        if (s == null) return;
        for (String key : s.getKeys(false)){
            ConfigurationSection i = s.getConfigurationSection(key);
            if (i == null) continue;
            try {
                String id = i.getString("Key","minecraft:block.piston.extend");
                float vol = (float)i.getDouble("Volume",0.8);
                float pit = (float)i.getDouble("Pitch",1.0);
                Sound sound = Sound.valueOf(id.replace("minecraft:","").toUpperCase().replace('.','_'));
                loc.getWorld().playSound(loc, sound, vol, pit);
            } catch (Exception ignored){}
        }
    }
    private void playParticles(Location loc, ConfigurationSection s){
        if (s == null) return;
        for (String key : s.getKeys(false)){
            ConfigurationSection i = s.getConfigurationSection(key);
            if (i == null) continue;
            try {
                String id = i.getString("Particle","minecraft:crit");
                int count = i.getInt("Count",1);
                double ox = i.getDouble("Offset.X",0), oy = i.getDouble("Offset.Y",0), oz = i.getDouble("Offset.Z",0);
                Particle particle = Particle.valueOf(id.replace("minecraft:","").toUpperCase().replace('.','_'));
                loc.getWorld().spawnParticle(particle, loc, count, ox, oy, oz, 0.0);
            } catch (Exception ignored){}
        }
    }
}
