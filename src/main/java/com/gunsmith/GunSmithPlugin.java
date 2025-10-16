package com.gunsmith.config;

import org.bukkit.configuration.ConfigurationSection;
import java.util.ArrayList;
import java.util.List;

/**
 * WeaponConfig: fields referenced across services/UI.
 * Safe defaults so code runs even if some keys are absent.
 */
public class WeaponConfig {

    // --- Identity / display ---
    public String id;
    public String name = null;                        // GUI 表示名
    public String material = "IRON_HORSE_ARMOR";     // アイテムMCID
    public boolean unbreakable = true;

    // --- Raw sections for services that read them directly ---
    public ConfigurationSection explosionSection; // Ballistics.Explosion
    public ConfigurationSection visualSection;    // Ballistics.Visual

    // --- Core damage ---
    public double damageBase = 6.0;
    public double damageBaseExplosion = 0.0;
    public int    damageFireTicks = 0;
    public boolean damageOwnerImmunity = false;
    public boolean damageIgnoreTeams = false;
    public int    damageArmorDamage = 0;

    // --- Hit multipliers ---
    public double headMultiplier = 1.5;
    public double bodyMultiplier = 1.0;
    public double limbMultiplier = 0.85;
    // GUI 用
    public double hsMultiplier = 1.5;

    // --- Range / lifetime / impact explode ---
    public int  rangeTicks = 200;                 // 射程tick（削除まで）
    public int  projectileLifespanTicks = 200;    // 物理発射体寿命
    public boolean impactExplodeEnabled = false;  // 着弾爆発
    public int  impactExplodeDelay = 0;           // 着弾爆発遅延

    // --- Melee (★今回のエラー原因：必須フィールド) ---
    public boolean meleeEnable = false;
    public String  meleeAttachmentId = null;
    public double  meleeRange = 3.0;
    public int     meleeHitDelay = 10;
    public int     meleeMissDelay = 10;
    public boolean meleeConsumeOnMiss = false;
    public Double  meleeDamageBase = null; // nullなら projectile の damageBase を流用

    // --- Fire modes / rate ---
    public List<String> selectFireOrder = new ArrayList<>(); // ["SEMI","BURST","AUTO","SINGLE_ACTION"]
    public double ratePerSec = 10.0;
    public int    burstCount = 3;
    public int    burstInterval = 2;
    public boolean singleAction = false;

    // --- ADS / movement ---
    public boolean sneakAsADS = true;

    // --- Projectile physics / entity ---
    public String mode = "ARROW";          // "ARROW"/"SNOWBALL"/"ITEM"/"RAY"
    public String entityType = "ARROW";
    public double projectileSpeed = 1.1;
    public boolean gravityEnabled = true;

    // --- Penetration ---
    public int penetrationMaxEntities = 0;
    public int penetrationMaxBlocks = 0;
    public List<String> penetrationAllowBlocks = new ArrayList<>();

    // --- Magazine / UI ---
    public int magCapacity = 30;

    // --- Ammo wiring ---
    public String       ammoId = null;     // 単一
    public List<String> ammoList = null;   // 複数
    public boolean consumeAmmo = true;
    public boolean requireAmmo = true;

    // --- Homing ---
    public boolean homingEnabled = false;
    public int     homingMaxTicks = 100;
    public double  homingMaxDistance = 64.0;
    public boolean homingLoseOnObstruction = true;
    public boolean homingRetarget = false;
    public int     homingRetargetInterval = 10;
    public double  homingRetargetRadius = 6.0;
    public double  homingTurnRateDeg = 6.0;

    // --- Explosion (advanced) ---
    public double explosionKnockbackMultiplier = 1.0;
    public String explosionExposure = "DEFAULT";   // DEFAULT/DISTANCE/NONE
    public String explosionShape = "DEFAULT";      // DEFAULT/SPHERE/CUBE/PARABOLA
    public double expYield = 4.0, expRadius = 3.0, expAngle = 0, expDepth = 0, expWidth = 0, expHeight = 0;
    public int    expRays = 16;
    public int    detonationDelayAfterImpact = 0;
    public boolean detonationRemoveProjectile = true;
    public boolean detonationOnSpawn = false, detonationOnEntity = true, detonationOnBlock = true;

    // Cluster
    public String  clusterSplitProjectile = null;
    public double  clusterProjectileSpeed = 1.0;
    public int     clusterNumberOfBombs = 0;
    public int     clusterNumberOfSplits = 0;
    public int     clusterDetonationDelayAfterImpact = 0;
    public boolean clusterRemoveProjectileOnDetonation = true;
    public boolean clusterImpactSpawn = false, clusterImpactEntity = true, clusterImpactBlock = true;

    // Airstrike
    public String  airstrikeDroppedProjectile = null;
    public int     airstrikeMinBombs = 0, airstrikeMaxBombs = 0;
    public double  airstrikeHeight = 60.0, airstrikeVerticalRandomness = 5.0,
                   airstrikeDistanceBetween = 3.0, airstrikeMaxDistanceFromCenter = 25.0;
    public int     airstrikeLayers = 1, airstrikeDelayBetweenLayers = 40;
    public int     airstrikeDetonationDelayAfterImpact = 0;
    public boolean airstrikeRemoveProjectileOnDetonation = true;
    public boolean airstrikeImpactSpawn = false, airstrikeImpactEntity = true, airstrikeImpactBlock = true;

    // ===== Parser =====
    public static WeaponConfig from(final String id, final ConfigurationSection sec) {
        WeaponConfig w = new WeaponConfig();
        w.id = id;
        if (sec == null) return w;

        // Identity
        w.name = sec.getString("Item.Name", w.name);
        w.material = sec.getString("Item.Material", w.material);
        w.unbreakable = sec.getBoolean("Item.Unbreakable", w.unbreakable);

        // Damage
        w.damageBase = sec.getDouble("Ballistics.Damage.Base", w.damageBase);
        var dmgSec = sec.getConfigurationSection("Ballistics.Damage");
        if (dmgSec != null) {
            w.damageBaseExplosion = dmgSec.getDouble("Base_Explosion_Damage", w.damageBaseExplosion);
            w.damageFireTicks = dmgSec.getInt("Fire_Ticks", w.damageFireTicks);
            w.damageOwnerImmunity = dmgSec.getBoolean("Enable_Owner_Immunity", w.damageOwnerImmunity);
            w.damageIgnoreTeams = dmgSec.getBoolean("Ignore_Teams", w.damageIgnoreTeams);
            w.damageArmorDamage = dmgSec.getInt("Armor_Damage", w.damageArmorDamage);
        }

        // Multipliers
        w.headMultiplier = sec.getDouble("HitDetection.Multipliers.Head", w.headMultiplier);
        w.bodyMultiplier = sec.getDouble("HitDetection.Multipliers.Body", w.bodyMultiplier);
        w.limbMultiplier = sec.getDouble("HitDetection.Multipliers.Limb", w.limbMultiplier);
        w.hsMultiplier = w.headMultiplier;

        // Sections
        w.visualSection = sec.getConfigurationSection("Ballistics.Visual");
        w.explosionSection = sec.getConfigurationSection("Ballistics.Explosion");

        // Range / lifetime
        w.rangeTicks = sec.getInt("Ballistics.Range_Ticks", w.rangeTicks);
        w.projectileLifespanTicks = sec.getInt("Ballistics.Entity.Lifespan_Ticks", w.projectileLifespanTicks);

        // Impact explode
        w.impactExplodeEnabled = sec.getBoolean("Ballistics.Impact_Explode.Enabled", w.impactExplodeEnabled);
        w.impactExplodeDelay = sec.getInt("Ballistics.Impact_Explode.Delay_Ticks", w.impactExplodeDelay);

        // Melee
        var melee = sec.getConfigurationSection("Melee");
        if (melee != null) {
            w.meleeEnable = melee.getBoolean("Enable_Melee", w.meleeEnable);
            w.meleeAttachmentId = melee.getString("Melee_Attachment", w.meleeAttachmentId);
            w.meleeRange = melee.getDouble("Melee_Range", w.meleeRange);
            w.meleeHitDelay = melee.getInt("Melee_Hit_Delay", w.meleeHitDelay);
            w.meleeMissDelay = melee.getInt("Melee_Miss_Delay", w.meleeMissDelay);
            w.meleeConsumeOnMiss = melee.getBoolean("Consume_On_Miss", w.meleeConsumeOnMiss);
            if (melee.isSet("Damage.Base")) w.meleeDamageBase = melee.getDouble("Damage.Base");
        }

        // Fire / burst / order
        w.ratePerSec = sec.getDouble("Trigger.Rate_Per_Second", w.ratePerSec);
        w.burstCount = sec.getInt("Trigger.Burst.Count", w.burstCount);
        w.burstInterval = sec.getInt("Trigger.Burst.Interval_Ticks", w.burstInterval);
        w.singleAction = sec.getBoolean("Trigger.Single_Action", w.singleAction);
        var order = sec.getStringList("Trigger.Select_Fire_Order");
        if (order != null && !order.isEmpty()) w.selectFireOrder = order;

        // ADS
        w.sneakAsADS = sec.getBoolean("ADS.Sneak_As_ADS", w.sneakAsADS);

        // Projectile physics
        w.mode = sec.getString("Projectile.Mode", w.mode);
        w.entityType = sec.getString("Projectile.Entity_Type", w.entityType);
        w.projectileSpeed = sec.getDouble("Projectile.Speed", w.projectileSpeed);
        w.gravityEnabled = sec.getBoolean("Projectile.Gravity", w.gravityEnabled);

        // Penetration
        w.penetrationMaxEntities = sec.getInt("Penetration.Max_Entities", w.penetrationMaxEntities);
        w.penetrationMaxBlocks = sec.getInt("Penetration.Max_Blocks", w.penetrationMaxBlocks);
        var allow = sec.getStringList("Penetration.Allow_Blocks");
        if (allow != null && !allow.isEmpty()) w.penetrationAllowBlocks = allow;

        // Magazine
        w.magCapacity = sec.getInt("Magazine.Capacity", w.magCapacity);

        // Ammo
        w.ammoId = sec.getString("Ammo.Id", w.ammoId);
        var al = sec.getStringList("Ammo.List");
        if (al != null && !al.isEmpty()) w.ammoList = al;
        w.consumeAmmo = sec.getBoolean("Ammo.Consume", w.consumeAmmo);
        w.requireAmmo = sec.getBoolean("Ammo.Require", w.requireAmmo);

        // Homing
        var hom = sec.getConfigurationSection("Homing");
        if (hom != null) {
            w.homingEnabled = hom.getBoolean("Enabled", w.homingEnabled);
            w.homingMaxTicks = hom.getInt("Max_Ticks", w.homingMaxTicks);
            w.homingMaxDistance = hom.getDouble("Max_Distance", w.homingMaxDistance);
            w.homingLoseOnObstruction = hom.getBoolean("Lose_On_Obstruction", w.homingLoseOnObstruction);
            w.homingRetarget = hom.getBoolean("Retarget.Enabled", w.homingRetarget);
            w.homingRetargetInterval = hom.getInt("Retarget.Interval_Ticks", w.homingRetargetInterval);
            w.homingRetargetRadius = hom.getDouble("Retarget.Radius", w.homingRetargetRadius);
            w.homingTurnRateDeg = hom.getDouble("Turn_Rate_Deg", w.homingTurnRateDeg);
        }

        // Explosion advanced
        if (w.explosionSection != null) {
            w.explosionKnockbackMultiplier = w.explosionSection.getDouble("Knockback_Multiplier", w.explosionKnockbackMultiplier);
            w.explosionExposure = w.explosionSection.getString("Explosion_Exposure", w.explosionExposure);
            w.explosionShape = w.explosionSection.getString("Explosion_Shape", w.explosionShape);

            var etd = w.explosionSection.getConfigurationSection("Explosion_Type_Data");
            if (etd != null) {
                w.expYield = etd.getDouble("Yield", w.expYield);
                double r = etd.getDouble("Radius", -1.0);
                if (r <= 0.0) r = w.explosionSection.getDouble("Radius", w.expRadius);
                w.expRadius = r;
                w.expAngle = etd.getDouble("Angle", w.expAngle);
                w.expDepth = etd.getDouble("Depth", w.expDepth);
                w.expWidth = etd.getDouble("Width", w.expWidth);
                w.expHeight = etd.getDouble("Height", w.expHeight);
                w.expRays = etd.getInt("Rays", w.expRays);
            } else {
                w.expRadius = w.explosionSection.getDouble("Radius", w.expRadius);
            }

            var det = w.explosionSection.getConfigurationSection("Detonation");
            if (det != null) {
                w.detonationDelayAfterImpact = det.getInt("Delay_After_Impact", w.detonationDelayAfterImpact);
                w.detonationRemoveProjectile = det.getBoolean("Remove_Projectile_On_Detonation", w.detonationRemoveProjectile);
                var iw = det.getConfigurationSection("Impact_When");
                if (iw != null) {
                    w.detonationOnSpawn = iw.getBoolean("Spawn", w.detonationOnSpawn);
                    w.detonationOnEntity = iw.getBoolean("Entity", w.detonationOnEntity);
                    w.detonationOnBlock = iw.getBoolean("Block", w.detonationOnBlock);
                }
            }

            var cl = w.explosionSection.getConfigurationSection("Cluster_Bomb");
            if (cl != null) {
                w.clusterSplitProjectile = cl.getString("Split_Projectile", w.clusterSplitProjectile);
                w.clusterProjectileSpeed = cl.getDouble("Projectile_Speed", w.clusterProjectileSpeed);
                w.clusterNumberOfBombs = cl.getInt("Number_Of_Bombs", w.clusterNumberOfBombs);
                w.clusterNumberOfSplits = cl.getInt("Number_Of_Splits", w.clusterNumberOfSplits);

                var cdet = cl.getConfigurationSection("Detonation");
                if (cdet != null) {
                    w.clusterDetonationDelayAfterImpact = cdet.getInt("Delay_After_Impact", w.clusterDetonationDelayAfterImpact);
                    w.clusterRemoveProjectileOnDetonation = cdet.getBoolean("Remove_Projectile_On_Detonation", w.clusterRemoveProjectileOnDetonation);
                    var ciw = cdet.getConfigurationSection("Impact_When");
                    if (ciw != null) {
                        w.clusterImpactSpawn = ciw.getBoolean("Spawn", w.clusterImpactSpawn);
                        w.clusterImpactEntity = ciw.getBoolean("Entity", w.clusterImpactEntity);
                        w.clusterImpactBlock = ciw.getBoolean("Block", w.clusterImpactBlock);
                    }
                }
            }

            var air = w.explosionSection.getConfigurationSection("Airstrike");
            if (air != null) {
                w.airstrikeDroppedProjectile = air.getString("Dropped_Projectile", w.airstrikeDroppedProjectile);
                w.airstrikeMinBombs = air.getInt("Minimum_Bombs", w.airstrikeMinBombs);
                w.airstrikeMaxBombs = air.getInt("Maximum_Bombs", w.airstrikeMaxBombs);
                w.airstrikeHeight = air.getDouble("Height", w.airstrikeHeight);
                w.airstrikeVerticalRandomness = air.getDouble("Vertical_Randomness", w.airstrikeVerticalRandomness);
                w.airstrikeDistanceBetween = air.getDouble("Distance_Between_Bombs", w.airstrikeDistanceBetween);
                w.airstrikeMaxDistanceFromCenter = air.getDouble("Maximum_Distance_From_Center", w.airstrikeMaxDistanceFromCenter);
                w.airstrikeLayers = air.getInt("Layers", w.airstrikeLayers);
                w.airstrikeDelayBetweenLayers = air.getInt("Delay_Between_Layers", w.airstrikeDelayBetweenLayers);

                var adet = air.getConfigurationSection("Detonation");
                if (adet != null) {
                    w.airstrikeDetonationDelayAfterImpact = adet.getInt("Delay_After_Impact", w.airstrikeDetonationDelayAfterImpact);
                    w.airstrikeRemoveProjectileOnDetonation = adet.getBoolean("Remove_Projectile_On_Detonation", w.airstrikeRemoveProjectileOnDetonation);
                    var aiw = adet.getConfigurationSection("Impact_When");
                    if (aiw != null) {
                        w.airstrikeImpactSpawn = aiw.getBoolean("Spawn", w.airstrikeImpactSpawn);
                        w.airstrikeImpactEntity = aiw.getBoolean("Entity", w.airstrikeImpactEntity);
                        w.airstrikeImpactBlock = aiw.getBoolean("Block", w.airstrikeImpactBlock);
                    }
                }
            }
        }

        return w;
    }
}
