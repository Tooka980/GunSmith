package com.gunsmith;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Clean onEnable wiring for 0.3.x
 * - Matches current constructor signatures in services
 * - Exposes getters used by other components
 * - Guards saveDefaultConfig() if config.yml is not embedded
 */
public class GunSmithPlugin extends JavaPlugin {

    private com.gunsmith.service.ProjectileService projectileService;
    private com.gunsmith.service.AmmoService ammoService;

    public com.gunsmith.service.ProjectileService getProjectileService() {
        return this.projectileService;
    }
    public com.gunsmith.service.AmmoService getAmmoService() {
        return this.ammoService;
    }

    @Override
    public void onEnable() {
        // Only call saveDefaultConfig() when config.yml is embedded in the jar
        if (getResource("config.yml") != null) {
            saveDefaultConfig();
        } else {
            getLogger().warning("config.yml not embedded, skipping saveDefaultConfig()");
        }

        // Export sample resources (ignore if missing)
        try { saveResource("weapons/assault_rifle/AK_12.yml", false); } catch (Exception ignored) {}
        try { saveResource("ammos.yml", false); } catch (Throwable ignored) {}

        // Core services (respect actual constructor signatures)
        com.gunsmith.service.WeaponRegistry weaponRegistry = new com.gunsmith.service.WeaponRegistry(this);
        this.ammoService = new com.gunsmith.service.AmmoService(this);
        com.gunsmith.service.VisualService visualService = new com.gunsmith.service.VisualService(this);
        com.gunsmith.service.ExplosionService explosionService = new com.gunsmith.service.ExplosionService(this, visualService);
        // HomingService expects (Plugin, VisualService)
        com.gunsmith.service.HomingService homingService = new com.gunsmith.service.HomingService(this, visualService);
        this.projectileService = new com.gunsmith.service.ProjectileService(this, weaponRegistry, explosionService);

        // ❗ FIX: MeleeService は (GunSmithPlugin, WeaponRegistry) の 2 引数コンストラクタ
        com.gunsmith.service.MeleeService meleeService = new com.gunsmith.service.MeleeService(this, weaponRegistry);

        // FireService は Listener ではない
        com.gunsmith.service.FireService fireService = new com.gunsmith.service.FireService(
                this, weaponRegistry, homingService, visualService, this.ammoService
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
