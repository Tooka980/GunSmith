package com.gunsmith.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class WeaponConfig {
    public String id;
    public String material;
    public String name;
    public boolean unbreakable;

    // Controls / movement
    public List<String> selectFireOrder;
    public double ratePerSec;
    public double rateRPM;
    public int burstCount;
    public int burstInterval;
    public boolean sneakAsADS;
    public double adsSpeedScale;

    // Magazine & ammo mapping
    public int magCapacity;
    public boolean startLoaded;
    public boolean consumeAmmo;
    public boolean requireAmmo;
    public String ammoId; // single
    public List<String> ammoList; // multiple

    // Ballistics
    public String mode; // RAY, PHYSICAL, ITEM
    public String entityType;
    public boolean gravityEnabled;
    public Double gravityValue;
    public double dragBase;
    public double projectileSpeed;
    public Double dragWater;
    public Double dragLava;
    public int rangeTicks; // lifetime range
    public int projectileLifespanTicks; // for physical entity removal

    // Homing
    public boolean homingEnabled;
    public int homingMaxTicks;
    public double homingMaxDistance;
    public double homingTurnRateDeg;
    public boolean homingLoseOnObstruction;
    public boolean homingRetarget;
    public int homingRetargetInterval;
    public double homingRetargetRadius;

    // Penetration
    public boolean penetrationEnabled;
    public int penetrationMaxEntities;
    public int penetrationMaxBlocks;
    public java.util.List<String> penetrationAllowBlocks;

    // Damage
    public double damageBase;     // weapon base damage (adds to ammo damage)
    public double hsMultiplier;
    public double headMultiplier;
    public double bodyMultiplier;
    public double limbMultiplier;

    // Damage extras
    public double damageBaseExplosion;
    public int damageFireTicks;
    public boolean damageOwnerImmunity;
    public boolean damageIgnoreTeams;
    public int damageArmorDamage;

    // Explosion
    public ConfigurationSection explosionSection;
    public boolean impactExplodeEnabled;
    public int impactExplodeDelay;

    // Visual / Spread / Recoil
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
        w.rateRPM = sec.getDouble("Trigger.Rate_RPM", w.ratePerSec*60.0);
        w.burstCount = sec.getInt("Trigger.Burst.Count", 3);
        w.burstInterval = sec.getInt("Trigger.Burst.Interval_Ticks", 2);

        var mag = sec.getConfigurationSection("Magazine");
        w.magCapacity = mag != null ? mag.getInt("Capacity", 30) : 30;
        w.startLoaded = mag != null && mag.getBoolean("Start_Loaded", true);
        w.consumeAmmo = mag == null || mag.getBoolean("Consume_Ammo", true);
        w.requireAmmo = mag == null || mag.getBoolean("Require_Ammo_In_Inventory", true);
        if (mag != null) {
            w.ammoId = mag.getString("Ammo", null);
            w.ammoList = mag.getStringList("Ammo_List");
        }

        w.mode = sec.getString("Ballistics.Mode", "PHYSICAL");
        w.entityType = sec.getString("Ballistics.Entity.Type", "minecraft:arrow");
        w.gravityEnabled = sec.getBoolean("Ballistics.Gravity.Enabled", true);
        if (sec.isSet("Ballistics.Gravity.Value")) w.gravityValue = sec.getDouble("Ballistics.Gravity.Value");
        w.dragBase = sec.getDouble("Ballistics.Drag.Base", 0.99);
        w.dragWater = sec.isSet("Ballistics.Drag.In_Water") ? sec.getDouble("Ballistics.Drag.In_Water") : null;
        w.dragLava = sec.isSet("Ballistics.Drag.In_Lava") ? sec.getDouble("Ballistics.Drag.In_Lava") : null;
        w.projectileSpeed = sec.getDouble("Ballistics.Projectile_Speed", 3.0);
        w.rangeTicks = sec.getInt("Ballistics.Range_Ticks", 200);
        w.projectileLifespanTicks = sec.getInt("Ballistics.Entity.Lifespan_Ticks", 200);

        var hom = sec.getConfigurationSection("Ballistics.Homing");
        w.homingEnabled = hom != null && hom.getBoolean("Enabled", false);
        w.homingMaxTicks = hom != null ? hom.getInt("Max_Time_Ticks", 120) : 120;
        w.homingMaxDistance = hom != null ? hom.getDouble("Max_Distance", 60.0) : 60.0;
        w.homingTurnRateDeg = hom != null ? hom.getDouble("Turn_Rate_Deg_Per_Tick", 2.5) : 2.5;
        w.homingLoseOnObstruction = hom != null && hom.getBoolean("Lose_Lock_On_Obstruction", false);
        var rt = hom != null ? hom.getConfigurationSection("Retarget") : null;
        w.homingRetarget = rt != null && rt.getBoolean("Enabled", false);
        w.homingRetargetInterval = rt != null ? rt.getInt("Interval_Ticks", 10) : 10;
        w.homingRetargetRadius = rt != null ? rt.getDouble("Radius", 12.0) : 12.0;

        var pen = sec.getConfigurationSection("Ballistics.Penetration");
        w.penetrationEnabled = pen != null && pen.getBoolean("Enabled", true);
        w.penetrationMaxEntities = pen != null ? pen.getInt("Max_Entities", 1) : 1;
        w.penetrationMaxBlocks = pen != null ? pen.getInt("Max_Blocks", 1) : 1;
        w.penetrationAllowBlocks = pen != null ? pen.getStringList("Allow_Blocks") : java.util.List.of();

        w.damageBase = sec.getDouble("Ballistics.Damage.Base", 6.0);
        w.hsMultiplier = sec.getDouble("HitDetection.Headshot_Multiplier", 1.5);
        var mult = sec.getConfigurationSection("HitDetection.Multipliers");
        w.headMultiplier = mult != null ? mult.getDouble("Head", w.hsMultiplier) : w.hsMultiplier;
        w.bodyMultiplier = mult != null ? mult.getDouble("Body", 1.0) : 1.0;
        w.limbMultiplier = mult != null ? mult.getDouble("Limb", 0.9) : 0.9;

        w.explosionSection = sec.getConfigurationSection("Ballistics.Explosion");
        if (sec.getConfigurationSection("Ballistics.Impact_Explode") != null){
            var ie = sec.getConfigurationSection("Ballistics.Impact_Explode");
            w.impactExplodeEnabled = ie.getBoolean("Enabled", false);
            w.impactExplodeDelay = ie.getInt("Delay_Ticks", 0);
        } else {
            w.impactExplodeEnabled = false;
            w.impactExplodeDelay = 0;
        }

        w.visualSection = sec.getConfigurationSection("Ballistics.Visual");

        // Melee
        var melee = sec.getConfigurationSection("Melee");
        if (melee != null){
            w.meleeEnable = melee.getBoolean("Enable_Melee", false);
            w.meleeAttachmentId = melee.getString("Melee_Attachment", null);
            w.meleeRange = melee.getDouble("Melee_Range", 3.0);
            w.meleeHitDelay = melee.getInt("Melee_Hit_Delay", 10);
            w.meleeMissDelay = melee.getInt("Melee_Miss_Delay", 10);
            w.meleeConsumeOnMiss = melee.getBoolean("Consume_On_Miss", false);
            if (melee.isSet("Damage.Base")) w.meleeDamageBase = melee.getDouble("Damage.Base");
        }

        // Single Action fire mode
        w.singleAction = sec.getBoolean("Trigger.Single_Action", false);

        // Advanced explosion schema
        if (w.explosionSection != null){
            w.explosionKnockbackMultiplier = w.explosionSection.getDouble("Knockback_Multiplier", 1.0);
            w.explosionExposure = w.explosionSection.getString("Explosion_Exposure", "DEFAULT");
            w.explosionShape = w.explosionSection.getString("Explosion_Shape", "DEFAULT");
            var etd = w.explosionSection.getConfigurationSection("Explosion_Type_Data");
            if (etd != null){
                w.expYield = etd.getDouble("Yield", 4.0);
                w.expRadius = etd.getDouble("Radius", w.explosionSection.getDouble("Radius", 3.0));
                w.expAngle = etd.getDouble("Angle", 0.0);
                w.expDepth = etd.getDouble("Depth", 0.0);
                w.expWidth = etd.getDouble("Width", 0.0);
                w.expHeight = etd.getDouble("Height", 0.0);
                w.expRays = etd.getInt("Rays", 16);
            } else {
                w.expRadius = w.explosionSection.getDouble("Radius", 3.0);
                w.expRays = 16;
            }
            var det = w.explosionSection.getConfigurationSection("Detonation");
            if (det != null){
                w.detonationDelayAfterImpact = det.getInt("Delay_After_Impact", 0);
                w.detonationRemoveProjectile = det.getBoolean("Remove_Projectile_On_Detonation", true);
                var iw = det.getConfigurationSection("Impact_When");
                if (iw != null){
                    w.detonationOnSpawn  = iw.getBoolean("Spawn", false);
                    w.detonationOnEntity = iw.getBoolean("Entity", true);
                    w.detonationOnBlock  = iw.getBoolean("Block", true);
                } else {
                    w.detonationOnSpawn=false; w.detonationOnEntity=true; w.detonationOnBlock=true;
                }
            }
            var cl = w.explosionSection.getConfigurationSection("Cluster_Bomb");
            if (cl != null){
                w.clusterSplitProjectile = cl.getString("Split_Projectile", null);
                w.clusterProjectileSpeed = cl.getDouble("Projectile_Speed", 1.0);
                w.clusterNumberOfBombs = cl.getInt("Number_Of_Bombs", 4);
                w.clusterNumberOfSplits = cl.getInt("Number_Of_Splits", 1);
                var cdet = cl.getConfigurationSection("Detonation");
                if (cdet != null){
                    w.clusterDetonationDelayAfterImpact = cdet.getInt("Delay_After_Impact", 0);
                    w.clusterRemoveProjectileOnDetonation = cdet.getBoolean("Remove_Projectile_On_Detonation", true);
                    var ciw = cdet.getConfigurationSection("Impact_When");
                    if (ciw != null){
                        w.clusterImpactSpawn  = ciw.getBoolean("Spawn", false);
                        w.clusterImpactEntity = ciw.getBoolean("Entity", true);
                        w.clusterImpactBlock  = ciw.getBoolean("Block", true);
                    }
                }
            }
            var air = w.explosionSection.getConfigurationSection("Airstrike");
            if (air != null){
                w.airstrikeDroppedProjectile = air.getString("Dropped_Projectile", null);
                w.airstrikeMinBombs = air.getInt("Minimum_Bombs", 4);
                w.airstrikeMaxBombs = air.getInt("Maximum_Bombs", 8);
                w.airstrikeHeight = air.getDouble("Height", 60.0);
                w.airstrikeVerticalRandomness = air.getDouble("Vertical_Randomness", 5.0);
                w.airstrikeDistanceBetween = air.getDouble("Distance_Between_Bombs", 3.0);
                w.airstrikeMaxDistanceFromCenter = air.getDouble("Maximum_Distance_From_Center", 25.0);
                w.airstrikeLayers = air.getInt("Layers", 1);
                w.airstrikeDelayBetweenLayers = air.getInt("Delay_Between_Layers", 40);
                var adet = air.getConfigurationSection("Detonation");
                if (adet != null){
                    w.airstrikeDetonationDelayAfterImpact = adet.getInt("Delay_After_Impact", 0);
                    w.airstrikeRemoveProjectileOnDetonation = adet.getBoolean("Remove_Projectile_On_Detonation", true);
                    var aiw = adet.getConfigurationSection("Impact_When");
                    if (aiw != null){
                        w.airstrikeImpactSpawn  = aiw.getBoolean("Spawn", false);
                        w.airstrikeImpactEntity = aiw.getBoolean("Entity", true);
                        w.airstrikeImpactBlock  = aiw.getBoolean("Block", true);
                    }
                }
            }
        }

        w.spreadSection = sec.getConfigurationSection("Spread");
        w.recoilSection = sec.getConfigurationSection("Recoil");

        // CS remap: if provided as CrackShot-style under Weapon_Title, shift section
        if (sec.getConfigurationSection("Weapon_Title") != null){
            sec = sec.getConfigurationSection("Weapon_Title");
        }

        // CrackShot compatibility mapping (minimal useful subset)
        if (sec.getConfigurationSection("Shooting") != null){
            var csS = sec.getConfigurationSection("Shooting");
            // Fully_Automatic
            if (csS.getBoolean("Fully_Automatic.Enable", false)){
                w.selectFireOrder = java.util.List.of("SEMI","AUTO");
            }
            // Fire rate: RPM
            if (csS.isSet("Fully_Automatic.Fire_Rate")){
                w.rateRPM = csS.getDouble("Fully_Automatic.Fire_Rate", w.rateRPM);
                w.ratePerSec = Math.max(0.1, w.rateRPM / 60.0);
            }
            // Delay between shots (ticks)
            if (csS.isSet("Delay_Between_Shots")){
                double ticks = csS.getDouble("Delay_Between_Shots", 4.0);
                if (ticks > 0) w.ratePerSec = 20.0 / ticks;
                w.rateRPM = w.ratePerSec * 60.0;
            }
            // Projectile speed / damage / drop
            if (csS.isSet("Projectile_Speed")) w.projectileSpeed = csS.getDouble("Projectile_Speed", w.projectileSpeed);
            if (csS.isSet("Projectile_Damage")) w.damageBase = csS.getDouble("Projectile_Damage", w.damageBase);
            if (csS.getBoolean("Remove_Bullet_Drop", false)) { w.gravityEnabled = false; w.gravityValue = 0.0; }

            if (csS.isSet("Projectile_Type")){
                String t = csS.getString("Projectile_Type", "arrow").toLowerCase();
                if (t.contains("snow")) { w.mode = "PHYSICAL"; w.entityType = "minecraft:snowball"; }
                else { w.mode = "PHYSICAL"; w.entityType = "minecraft:arrow"; }
            }
        }
        if (sec.getConfigurationSection("Burstfire") != null){
            var csB = sec.getConfigurationSection("Burstfire");
            if (csB.getBoolean("Enable", false)){
                w.burstCount = csB.getInt("Shots_Per_Burst", w.burstCount);
                w.burstInterval = csB.getInt("Delay_Between_Shots_In_Burst", w.burstInterval);
                // ensure burst in cycle order
                if (w.selectFireOrder == null || w.selectFireOrder.isEmpty()) w.selectFireOrder = java.util.List.of("SEMI","BURST");
            }
        }
        if (sec.getConfigurationSection("Ammo") != null){
            var csA = sec.getConfigurationSection("Ammo");
            boolean ammoEnable = csA.getBoolean("Enable", true);
            w.consumeAmmo = ammoEnable && csA.getBoolean("Take_Ammo_Per_Shot", true);
            w.requireAmmo = ammoEnable;
            // map material id if provided
            String id = csA.getString("Ammo_Item_ID", null);
            if (id != null){
                // store in ammoId for our ammo mapping
                w.ammoId = id;
            }
        }
        if (sec.getConfigurationSection("Scope") != null){
            var csScope = sec.getConfigurationSection("Scope");
            if (csScope.getBoolean("Enable", false)){
                double zoomSpread = csScope.getDouble("Zoom_Bullet_Spread", 0.7);
                w.adsSpeedScale = 0.8;
            }
        }
    
        return w;
    }
}
