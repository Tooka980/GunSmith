package com.gunsmith.service;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.config.WeaponConfig;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AmmoService {
    private final GunSmithPlugin plugin;
    private final YamlConfiguration ammoYml;
    private final Map<java.util.UUID, Double> lastAmmoDamage = new HashMap<>();
    private final Map<java.util.UUID, String> lastAmmoId = new HashMap<>();

    public AmmoService(GunSmithPlugin plugin){
        this.plugin = plugin;
        File f = new File(plugin.getDataFolder(), "ammos.yml");
        this.ammoYml = YamlConfiguration.loadConfiguration(f);
    }

    public Set<String> getAmmoIds(){
        return ammoYml.getKeys(false);
    }

    public String resolveAmmoId(WeaponConfig w){
        if (w.ammoId != null && ammoYml.isConfigurationSection(w.ammoId)) return w.ammoId;
        if (w.ammoList != null){
            for (String id : w.ammoList) if (ammoYml.isConfigurationSection(id)) return id;
        }
        // fallback: first ammo
        return ammoYml.getKeys(false).stream().findFirst().orElse(null);
    }

    public ItemStack createAmmoItem(String ammoId, int amount){
        String matId = ammoYml.getString(ammoId + ".Item.Material", "minecraft:paper");
        Material mat = Material.matchMaterial(matId, false);
        if (mat == null) mat = Material.PAPER;
        ItemStack is = new ItemStack(mat, Math.max(1, amount));
        var meta = is.getItemMeta();
        String name = ammoYml.getString(ammoId + ".Item.Name", "Ammo: "+ammoId);
        meta.setDisplayName(name);
        is.setItemMeta(meta);
        return is;
    }

    public double getAmmoDamage(String ammoId){
        return ammoYml.getDouble(ammoId + ".Damage.Base", 0.0);
    }

    public boolean consumeIfAllowed(Player p, WeaponConfig w){
        if (!w.consumeAmmo) { lastAmmoDamage.put(p.getUniqueId(), 0.0); lastAmmoId.put(p.getUniqueId(), null); return true; }
        if (!w.requireAmmo) { lastAmmoDamage.put(p.getUniqueId(), 0.0); lastAmmoId.put(p.getUniqueId(), null); return true; }

        // Try allowed ammo(s) in order
        java.util.List<String> tryList = new java.util.ArrayList<>();
        if (w.ammoId != null) tryList.add(w.ammoId);
        if (w.ammoList != null) tryList.addAll(w.ammoList);
        if (tryList.isEmpty()) tryList.addAll(ammoYml.getKeys(false));

        for (String ammoId : tryList){
            if (!ammoYml.isConfigurationSection(ammoId)) continue;
            String matId = ammoYml.getString(ammoId + ".Item.Material", "minecraft:paper");
            Material mat = Material.matchMaterial(matId, false);
            if (mat == null) mat = Material.PAPER;
            for (int i=0;i<p.getInventory().getSize();i++){
                ItemStack it = p.getInventory().getItem(i);
                if (it != null && it.getType() == mat){
                    it.setAmount(it.getAmount()-1);
                    if (it.getAmount() <= 0) p.getInventory().setItem(i, null);
                    lastAmmoDamage.put(p.getUniqueId(), getAmmoDamage(ammoId));
                    lastAmmoId.put(p.getUniqueId(), ammoId);
                    return true;
                }
            }
        }
        return false;
    }

    public double getLastAmmoDamage(java.util.UUID uuid){
        return lastAmmoDamage.getOrDefault(uuid, 0.0);
    }
    public String getLastAmmoId(java.util.UUID uuid){
        return lastAmmoId.get(uuid);
    }
}
