package com.gunsmith;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Clean onEnable with correct service wiring for 0.3.x line.
 * - Avoids registering null listeners
 * - Matches current constructor signatures observed in build logs
 */
public class GunSmithPlugin extends JavaPlugin {

    private com.gunsmith.service.ProjectileService projectileService;

    public com.gunsmith.service.ProjectileService getProjectileService() {
        return this.projectileService;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Export sample resources (ignore if missing)
        try { saveResource("weapons/assault_rifle/AK_12.yml", false); } catch (Exception ignored) {}
        try { saveResource("ammos.yml", false); } catch (Throwable ignored) {}

        // Core services (respect constructor signatures)
        com.gunsmith.service.WeaponRegistry weaponRegistry = new com.gunsmith.service.WeaponRegistry(this);
        com.gunsmith.service.AmmoService ammoService = new com.gunsmith.service.AmmoService(this);
        com.gunsmith.service.VisualService visualService = new com.gunsmith.service.VisualService(this);
        com.gunsmith.service.ExplosionService explosionService = new com.gunsmith.service.ExplosionService(this, visualService);
        com.gunsmith.service.HomingService homingService = new com.gunsmith.service.HomingService(this, weaponRegistry);
        this.projectileService = new com.gunsmith.service.ProjectileService(this, weaponRegistry, explosionService);
        com.gunsmith.service.MeleeService meleeService = new com.gunsmith.service.MeleeService(this, weaponRegistry, ammoService);
        // FireService is NOT a Listener; do not register as events
        com.gunsmith.service.FireService fireService = new com.gunsmith.service.FireService(
                this, weaponRegistry, homingService, visualService, ammoService
        );

        // Register only real listeners
        org.bukkit.plugin.PluginManager pm = getServer().getPluginManager();
        if (this.projectileService != null) pm.registerEvents(this.projectileService, this);
        if (meleeService != null) pm.registerEvents(meleeService, this);

        getLogger().info("GunSmith initialized.");
    }

    @Override
    public void onDisable() {
        // graceful shutdown if needed
    }
}
