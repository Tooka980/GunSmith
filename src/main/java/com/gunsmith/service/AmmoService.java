package com.gunsmith.service;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.config.WeaponConfig;
import org.bukkit.entity.Player;

public class AmmoService {
    private final GunSmithPlugin plugin;
    public AmmoService(GunSmithPlugin plugin){ this.plugin = plugin; }
    public boolean consumeIfAllowed(Player p, WeaponConfig w){
        if (!w.consumeAmmo) return true;
        if (!w.requireAmmo) return true;
        return true; // MVP: assume ammo exists
    }
}
