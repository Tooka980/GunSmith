package com.gunsmith;

import com.gunsmith.commands.GmCommand;
import com.gunsmith.service.*;
import org.bukkit.plugin.java.JavaPlugin;

public class GunSmithPlugin extends JavaPlugin {

    private ProjectileService projectileService;
    private AmmoService ammoService;
    private WeaponRegistry weaponRegistry;
    private ListGui listGui;

    public ProjectileService getProjectileService() { return this.projectileService; }
    public AmmoService getAmmoService() { return this.ammoService; }
    public WeaponRegistry getWeaponRegistry() { return this.weaponRegistry; }
    public ListGui getListGui() { return this.listGui; }

    @Override
    public void onEnable() {
        // 同梱されている場合のみ初期展開（未同梱ならスキップ）
        if (getResource("config.yml") != null) saveDefaultConfig();
        try { saveResource("weapons/assault_rifle/AK_12.yml", false); } catch (Throwable ignored) {}
        try { saveResource("ammos.yml", false); } catch (Throwable ignored) {}

        // ---- services ----
        this.weaponRegistry = new WeaponRegistry(this);
        this.ammoService = new AmmoService(this);
        VisualService visualService = new VisualService(this);
        ExplosionService explosionService = new ExplosionService(this, visualService);
        HomingService homingService = new HomingService(this, visualService);
        this.projectileService = new ProjectileService(this, weaponRegistry, explosionService);
        MeleeService meleeService = new MeleeService(this, weaponRegistry);
        this.listGui = new ListGui(this, weaponRegistry);

        // ---- listeners ----
        var pm = getServer().getPluginManager();
        pm.registerEvents(this.projectileService, this);
        pm.registerEvents(meleeService, this);
        pm.registerEvents(this.listGui, this);

        // ---- commands: Paper流（YAML不要） ----
        registerCommand("gm", new GmCommand(this, weaponRegistry, ammoService, listGui));

        getLogger().info("GunSmith initialized.");
    }

    @Override
    public void onDisable() {
        // 必要ならクリーンアップ
    }
}
