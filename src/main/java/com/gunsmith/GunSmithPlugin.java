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
        this.listGui = new ListGui(this, weaponRegistry);

        // ---- Event listeners ----
        var pm = getServer().getPluginManager();
        pm.registerEvents(this.projectileService, this);
        pm.registerEvents(meleeService, this);
        pm.registerEvents(this.listGui, this); // GUIのクリックキャンセル等をここで受けるなら

        // ---- /gm コマンド登録（Paperの registerCommand を使用）----
        // Brigadier / Lifecycle を使わないので依存不整合が起きません
        this.registerCommand("gm", new GmCommand(this, weaponRegistry, ammoService, listGui))
            .setTabCompleter(new GmCommand(this, weaponRegistry, ammoService, listGui));

        getLogger().info("GunSmith initialized.");
    }

    @Override
    public void onDisable() {
        // 片付けは必要に応じて
    }
}
