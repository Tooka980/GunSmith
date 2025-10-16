package com.gunsmith;

import com.gunsmith.service.*;
import com.gunsmith.commands.GmCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class GunSmithPlugin extends JavaPlugin {
    public com.gunsmith.service.ProjectileService getProjectileService(){ return this.projectileService; }

    private void exportResourceIfPresent(String path) {
        if (getResource(path) != null) {
            saveResource(path, false);
        } else {
            getLogger().warning("Embedded resource missing, skipped: " + path);
        }
    }

private WeaponRegistry weaponRegistry;
    private FireService fireService;
    private HomingService homingService;
    private VisualService visualService;
    private ProjectileService projectileService;
    private MeleeService meleeService;
    private AmmoService ammoService;
    private ListGui listGui;

    @Override public void onEnable(){ 
        saveDefaultConfig();
        // export default resources (ignore missing)
        try { saveResource("weapons/assault_rifle/AK_12.yml", false); } catch (Exception ignored) {}
        try { saveResource("ammos.yml", False); } catch (Throwable ignored) {}

        // core singletons
        com.gunsmith.service.WeaponRegistry weaponRegistry = new com.gunsmith.service.WeaponRegistry(this);
        com.gunsmith.service.AmmoService ammoService = new com.gunsmith.service.AmmoService(this, weaponRegistry);
        com.gunsmith.service.VisualService visualService = new com.gunsmith.service.VisualService(this);
        com.gunsmith.service.ExplosionService explosionService = new com.gunsmith.service.ExplosionService(this, visualService);
        this.projectileService = new com.gunsmith.service.ProjectileService(this, weaponRegistry, explosionService);
        com.gunsmith.service.FireService fireService = new com.gunsmith.service.FireService(this, weaponRegistry, ammoService, visualService, explosionService, projectileService);
        com.gunsmith.service.MeleeService meleeService = new com.gunsmith.service.MeleeService(this, weaponRegistry, ammoService);

        org.bukkit.plugin.PluginManager pm = getServer().getPluginManager();
        if (projectileService != null) pm.registerEvents(projectileService, this);
        if (meleeService != null) pm.registerEvents(meleeService, this);
        if (fireService != null) pm.registerEvents(fireService, this);

        // commands (Paper API: registerCommand)
        try {
            new com.gunsmith.command.GMCommand(this, weaponRegistry, ammoService).register();
        } catch (Throwable t){
            getLogger().warning("Command registration failed: " + t.getMessage());
        }
    }
    @Override public void onDisable() { if (homingService != null) homingService.shutdown(); }
    public WeaponRegistry getWeaponRegistry(){return weaponRegistry;}
    public FireService getFireService(){return fireService;}
    public HomingService getHomingService(){return homingService;}
    public VisualService getVisualService(){return visualService;}
    public AmmoService getAmmoService(){return ammoService;}
    public ListGui getListGui(){return listGui;}
}
