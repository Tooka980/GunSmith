package com.gunsmith.service;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.config.WeaponConfig;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WeaponRegistry {

    private final GunSmithPlugin plugin;
    private final Map<String, WeaponConfig> weapons = new HashMap<>();
    private final NamespacedKey keyId;

    public WeaponRegistry(GunSmithPlugin plugin) {
        this.plugin = plugin;
        this.keyId = new NamespacedKey(plugin, "weapon_id");
    }

    public void reload() {
        weapons.clear();
        File dir = new File(plugin.getDataFolder(), "weapons");
        scan(dir);
        plugin.getLogger().info("Loaded " + weapons.size() + " weapons.");
    }

    private void scan(File folder) {
        if (!folder.exists()) return;
        File[] files = folder.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return;
        for (File f : files) {
            YamlConfiguration yml = new YamlConfiguration();
            try { yml.load(f); }
            catch (IOException | InvalidConfigurationException e) {
                plugin.getLogger().warning("Failed to load " + f.getName() + ": " + e.getMessage());
                continue;
            }
            for (String root : yml.getKeys(false)) {
                var sec = yml.getConfigurationSection(root);
                if (sec == null) continue;
                WeaponConfig cfg = WeaponConfig.from(root, sec);
                weapons.put(root.toLowerCase(Locale.ROOT), cfg);
            }
        }
        File[] sub = folder.listFiles(File::isDirectory);
        if (sub != null) for (File s : sub) scan(s);
    }

    public WeaponConfig matchWeapon(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return null;
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (pdc.has(keyId, PersistentDataType.STRING)) {
                String id = pdc.get(keyId, PersistentDataType.STRING);
                if (id != null) return weapons.get(id.toLowerCase(Locale.ROOT));
            }
            String dn = meta.getDisplayName();
            if (dn != null) for (WeaponConfig w : weapons.values()) if (dn.contains(w.name)) return w;
        }
        return null;
    }

    public int getWeaponsCount() { return weapons.size(); }
    public Collection<WeaponConfig> getAll() { return weapons.values(); }
    public WeaponConfig getById(String id){ return weapons.get(id.toLowerCase(Locale.ROOT)); }

    public ItemStack createWeaponItem(WeaponConfig w) {
        Material mat = resolveMaterial(w.material, Material.IRON_HORSE_ARMOR);
        ItemStack is = new ItemStack(mat);
        ItemMeta im = is.getItemMeta();
        if (w.name != null) im.setDisplayName(w.name);
        im.setUnbreakable(w.unbreakable);
        im.getPersistentDataContainer().set(keyId, PersistentDataType.STRING, w.id);
        is.setItemMeta(im);
        return is;
    }

    private Material resolveMaterial(String mcid, Material fallback) {
        if (mcid == null) return fallback;
        Material m = Material.matchMaterial(mcid, false);
        if (m == null) {
            String simple = mcid.contains(":") ? mcid.substring(mcid.indexOf(':')+1) : mcid;
            m = Material.matchMaterial(simple, false);
        }
        return m != null ? m : fallback;
    }

    public com.gunsmith.config.WeaponConfig getHeldWeapon(org.bukkit.entity.Player p){
        org.bukkit.inventory.ItemStack is = p.getInventory().getItemInMainHand();
        if (is == null || !is.hasItemMeta()) return null;
        var meta = is.getItemMeta();
        var pdc = meta.getPersistentDataContainer();
        var key = new org.bukkit.NamespacedKey(plugin, "weapon_id");
        String id = pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING);
        if (id == null) return null;
        return getById(id);
    }

}
