package com.gunsmith;

import com.gunsmith.commands.GmExecutor;
import com.gunsmith.service.*;
import org.bukkit.command.PluginCommand;
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
        // config.yml を同梱している場合のみ保存
        if (getResource("config.yml") != null) saveDefaultConfig();
        // サンプルを展開（同梱されていれば）
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
        pm.registerEvents(this.listGui, this); // GUIクリックのキャンセル等

        // ---- /gm コマンド登録（Bukkit流。plugin.yml と対で動く）----
        GmExecutor gm = new GmExecutor(this, weaponRegistry, ammoService, listGui);
        PluginCommand pc = getCommand("gm");
        if (pc != null) {
            pc.setExecutor(gm);
            pc.setTabCompleter(gm);
        } else {
            getLogger().severe("Command 'gm' missing. Check plugin.yml!");
        }

        getLogger().info("GunSmith initialized.");
    }

    @Override
    public void onDisable() {
        // 必要なら片付け
    }
}
