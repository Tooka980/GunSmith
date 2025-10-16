package com.gunsmith;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import com.gunsmith.service.*;

public class GunSmithPlugin extends JavaPlugin {

    private ProjectileService projectileService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // リソース展開（存在しなくても無視）
        try { saveResource("weapons/assault_rifle/AK_12.yml", false); } catch (Exception ignored) {}
        try { saveResource("ammos.yml", false); } catch (Throwable ignored) {}

        // コアサービス初期化（依存順）
        WeaponRegistry weaponRegistry = new WeaponRegistry(this);
        AmmoService ammoService = new AmmoService(this);
        VisualService visualService = new VisualService(this);
        ExplosionService explosionService = new ExplosionService(this, visualService);
        HomingService homingService = new HomingService(this, weaponRegistry);
        this.projectileService = new ProjectileService(this, weaponRegistry, explosionService);
        MeleeService meleeService = new MeleeService(this, weaponRegistry, ammoService);
        FireService fireService = new FireService(this, weaponRegistry, homingService, visualService, ammoService);

        // Listener 登録（null安全）※ FireService は Listener ではない
        PluginManager pm = getServer().getPluginManager();
        if (this.projectileService != null) pm.registerEvents(this.projectileService, this);
        if (meleeService != null) pm.registerEvents(meleeService, this);

        getLogger().info("[GunSmith] Enabled successfully!");
    }
}
