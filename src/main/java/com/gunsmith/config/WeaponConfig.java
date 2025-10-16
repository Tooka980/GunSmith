package com.gunsmith.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class WeaponConfig {
    public String id;
    public String material;
    public String name;
    public boolean unbreakable;
    public List<String> selectFireOrder;
    public double ratePerSec;
    public int burstCount;
    public int burstInterval;
    public boolean sneakAsADS;
    public double adsSpeedScale;
    public int magCapacity;
    public boolean startLoaded;
    public boolean consumeAmmo;
    public boolean requireAmmo;
    public String mode;
    public String entityType;
    public boolean gravityEnabled;
    public Double gravityValue;
    public double dragBase;
    public Double dragWater;
    public Double dragLava;
    public boolean homingEnabled;
    public int homingMaxTicks;
    public double homingMaxDistance;
    public double homingTurnRateDeg;
    public boolean penetrationEnabled;
    public int penetrationMaxEntities;
    public int penetrationMaxBlocks;
    public double damageBase;
    public double hsMultiplier;
    public ConfigurationSection visualSection;
    public ConfigurationSection spreadSection;
    public ConfigurationSection recoilSection;

    public static WeaponConfig from(String id, ConfigurationSection sec){
        WeaponConfig w = new WeaponConfig();
        w.id = id;
        var info = sec.getConfigurationSection("Info.Weapon_Item");
        if (info != null){
            w.material = info.getString("Material", "minecraft:iron_horse_armor");
            w.name = info.getString("Name", id);
            w.unbreakable = info.getBoolean("Unbreakable", true);
        }
        w.sneakAsADS = sec.getBoolean("Controls.Sneak_As_ADS", true);
        w.adsSpeedScale = sec.getDouble("Movement.ADS_Speed_Scale", 0.85);
        w.selectFireOrder = sec.getStringList("Trigger.Select_Fire");
        w.ratePerSec = sec.getDouble("Trigger.Rate_Per_Sec", 8.0);
        w.burstCount = sec.getInt("Trigger.Burst.Count", 3);
        w.burstInterval = sec.getInt("Trigger.Burst.Interval_Ticks", 2);
        w.magCapacity = sec.getInt("Magazine.Capacity", 30);
        w.startLoaded = sec.getBoolean("Magazine.Start_Loaded", true);
        w.consumeAmmo = sec.getBoolean("Magazine.Consume_Ammo", true);
        w.requireAmmo = sec.getBoolean("Magazine.Require_Ammo_In_Inventory", true);
        w.mode = sec.getString("Ballistics.Mode", "PHYSICAL");
        w.entityType = sec.getString("Ballistics.Entity.Type", "minecraft:arrow");
        w.gravityEnabled = sec.getBoolean("Ballistics.Gravity.Enabled", true);
        if (sec.isSet("Ballistics.Gravity.Value")) w.gravityValue = sec.getDouble("Ballistics.Gravity.Value");
        w.dragBase = sec.getDouble("Ballistics.Drag.Base", 0.99);
        w.dragWater = sec.isSet("Ballistics.Drag.In_Water") ? sec.getDouble("Ballistics.Drag.In_Water") : null;
        w.dragLava = sec.isSet("Ballistics.Drag.In_Lava") ? sec.getDouble("Ballistics.Drag.In_Lava") : null;
        w.homingEnabled = sec.getBoolean("Ballistics.Homing.Enabled", false);
        w.homingMaxTicks = sec.getInt("Ballistics.Homing.Max_Time_Ticks", 120);
        w.homingMaxDistance = sec.getDouble("Ballistics.Homing.Max_Distance", 60.0);
        w.homingTurnRateDeg = sec.getDouble("Ballistics.Homing.Turn_Rate_Deg_Per_Tick", 2.5);
        w.penetrationEnabled = sec.getBoolean("Ballistics.Penetration.Enabled", true);
        w.penetrationMaxEntities = sec.getInt("Ballistics.Penetration.Max_Entities", 1);
        w.penetrationMaxBlocks = sec.getInt("Ballistics.Penetration.Max_Blocks", 1);
        w.damageBase = sec.getDouble("Ballistics.Damage.Base", 6.0);
        w.hsMultiplier = sec.getDouble("HitDetection.Headshot_Multiplier", 1.5);
        w.visualSection = sec.getConfigurationSection("Ballistics.Visual");
        w.spreadSection = sec.getConfigurationSection("Spread");
        w.recoilSection = sec.getConfigurationSection("Recoil");
        return w;
    }
}
