package com.gunsmith;

import com.gunsmith.service.*;
import com.gunsmith.commands.GmCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class GunSmithPlugin extends JavaPlugin {
    private WeaponRegistry weaponRegistry;
    private FireService fireService;
    private HomingService homingService;
    private VisualService visualService;
    private AmmoService ammoService;
    private ListGui listGui;

    @Override public void onEnable() {
        saveResource("weapons/assault_rifle/AK_12.yml", false);
        saveResource("ammos.yml", false);
        saveResource("attachments.yml", false);
        saveResource("GunSmith_full_schema.yml", false);

        weaponRegistry = new WeaponRegistry(this); weaponRegistry.reload();
        visualService = new VisualService(this);
        homingService = new HomingService(this, visualService);
        ammoService = new AmmoService(this);
        fireService = new FireService(this, weaponRegistry, homingService, visualService, ammoService);
        listGui = new ListGui(this, weaponRegistry);

        getServer().getPluginManager().registerEvents(new GunSmithListener(this, weaponRegistry, fireService), this);
        getServer().getPluginManager().registerEvents(listGui, this);

        getCommand("gm").setExecutor(new GmCommand(this, weaponRegistry, listGui));

        getLogger().info("GunSmith enabled: " + weaponRegistry.getWeaponsCount() + " weapons");
    }
    @Override public void onDisable() { if (homingService != null) homingService.shutdown(); }
    public WeaponRegistry getWeaponRegistry(){return weaponRegistry;}
    public FireService getFireService(){return fireService;}
    public HomingService getHomingService(){return homingService;}
    public VisualService getVisualService(){return visualService;}
    public AmmoService getAmmoService(){return ammoService;}
    public ListGui getListGui(){return listGui;}
}
