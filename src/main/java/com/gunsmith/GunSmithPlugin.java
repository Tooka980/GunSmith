package com.gunsmith;

import com.gunsmith.commands.GmCommands;
import com.gunsmith.service.*;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class GunSmithPlugin extends JavaPlugin {

    private ProjectileService projectileService;
    private AmmoService ammoService;
    private WeaponRegistry weaponRegistry;

    public ProjectileService getProjectileService() { return this.projectileService; }
    public AmmoService getAmmoService() { return this.ammoService; }

    @Override
    public void onEnable() {
        if (getResource("config.yml") != null) saveDefaultConfig();
        try { saveResource("weapons/assault_rifle/AK_12.yml", false); } catch (Exception ignored) {}
        try { saveResource("ammos.yml", false); } catch (Throwable ignored) {}

        // ---- Services ----
        this.weaponRegistry = new WeaponRegistry(this);
        this.ammoService = new AmmoService(this);
        VisualService visualService = new VisualService(this);
        ExplosionService explosionService = new ExplosionService(this, visualService);
        HomingService homingService = new HomingService(this, visualService);
        this.projectileService = new ProjectileService(this, weaponRegistry, explosionService);
        MeleeService meleeService = new MeleeService(this, weaponRegistry);

        // ---- Event listeners ----
        var pm = getServer().getPluginManager();
        pm.registerEvents(this.projectileService, this);
        pm.registerEvents(meleeService, this);

        // ---- /gm コマンド登録（Brigadier, LifecycleEvents）----
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            var registrar = event.registrar();
            new GmCommands(this, weaponRegistry, ammoService).registerBrigadier(registrar);
        });

        getLogger().info("GunSmith initialized.");
    }

    @Override
    public void onDisable() {
        // 片付けは必要に応じて
    }
}
