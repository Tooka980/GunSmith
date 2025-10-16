package com.gunsmith.config;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Minimal but complete WeaponConfig to satisfy current services.
 * - Contains every field referenced by FireService / ProjectileService / MeleeService / ExplosionService
 * - Provides a static parser: from(String id, ConfigurationSection sec)
 */
public class WeaponConfig {

    // --- Identity ---
    public String id;

    // --- Common sections (kept for downstream services) ---
    public ConfigurationSection explosionSection; // Ballistics.Explosion
    public ConfigurationSection visualSection;    // Ballistics.Visual

    // --- Damage (projectile base) ---
    public double damageBase = 6.0;

    // Damage extras (explosion and flags)
    public double damageBaseExplosion = 0.0;
    public int damageFireTicks = 0;
    public boolean damageOwnerImmunity = false;
    public boolean damageIgnoreTeams = false;
    public int damageArmorDamage = 0;

    // --- Hit multipliers (Head/Body/Limb) ---
    public double headMultiplier = 1.5;
    public double bodyMultiplier = 1.0;
    public double limbMultiplier = 0.85;

    // --- Ranging / lifetimes / impact explode (used by ProjectileService) ---
    /** “射程”の内部 tick 管理（Ballistics.Range_Ticks） */
    public int rangeTicks = 200;
    /** 物理発射体の寿命（Ballistics.Entity.Lifespan_Ticks） */
    public int projectileLifespanTicks = 200;
    /** 着弾爆発の有効/無効（Ballistics.Impact_Explode.Enabled） */
    public boolean impactExplodeEnabled = false;
    /** 着弾爆発の遅延 tick（Ballistics.Impact_Explode.Delay_Ticks） */
    public int impactExplodeDelay = 0;

    // --- Melee (MeleeService) ---
    public boolean meleeEnable = false;
    public String meleeAttachmentId = null;
    public double meleeRange = 3.0;
    public int meleeHitDelay = 10;
    public int meleeMissDelay = 10;
    public boolean meleeConsumeOnMiss = false;
    public Double meleeDamageBase = null; // if null, fall back to damageBase

    // --- Fire modes ---
    /** SINGLE_ACTION（単動）フラグ */
    public boolean singleAction = false;

    // --- Explosion (advanced) ---
    public double explosionKnockbackMultiplier = 1.0;
    /** DEFAULT / DISTANCE / NONE */
    public String explosionExposure = "DEFAULT";
    /** DEFAULT / SPHERE / CUBE / PARABOLA(予約) */
    public String explosionShape = "DEFAULT";

    // Explosion type data
    public double expYield = 4.0;
    public double expRadius = 3.0;
    public double expAngle = 0.0;
    public double expDepth = 0.0;
    public double expWidth = 0.0;
    public double expHeight = 0.0;
    public int expRays = 16;

    // Detonation behavior
    public int detonationDelayAfterImpact = 0;
    public boolean detonationRemoveProjectile = true;
    public boolean detonationOnSpawn = false;
    public boolean detonationOnEntity = true;
    public boolean detonationOnBlock = true;

    // Cluster (ITEM only)
    public String clusterSplitProjectile = null;
    public double clusterProjectileSpeed = 1.0;
    public int clusterNumberOfBombs = 0;
    public int clusterNumberOfSplits = 0;
    public int clusterDetonationDelayAfterImpact = 0;
    public boolean clusterRemoveProjectileOnDetonation = true;
    public boolean clusterImpactSpawn = false;
    public boolean clusterImpactEntity = true;
    public boolean clusterImpactBlock = true;

    // Airstrike
    public String airstrikeDroppedProjectile = null;
    public int airstrikeMinBombs = 0, airstrikeMaxBombs = 0;
    public double airstrikeHeight = 60.0;
    public double airstrikeVerticalRandomness = 5.0;
    public double airstrikeDistanceBetween = 3.0;
    public double airstrikeMaxDistanceFromCenter = 25.0;
    public int airstrikeLayers = 1;
    public int airstrikeDelayBetweenLayers = 40;
    public int airstrikeDetonationDelayAfterImpact = 0;
    public boolean airstrikeRemoveProjectileOnDetonation = true;
    public boolean airstrikeImpactSpawn = false, airstrikeImpactEntity = true, airstrikeImpactBlock = true;

    // ===== Parser =====

    public static WeaponConfig from(final String id, final ConfigurationSection sec) {
        WeaponConfig w = new WeaponConfig();
        w.id = id;

        if (sec == null) return w;

        // Base damage
        w.damageBase = sec.getDouble("Ballistics.Damage.Base", w.damageBase);

        // Damage extras
        ConfigurationSection dmgSec = sec.getConfigurationSection("Ballistics.Damage");
        if (dmgSec != null) {
            w.damageBaseExplosion = dmgSec.getDouble("Base_Explosion_Damage", w.damageBaseExplosion);
            w.damageFireTicks = dmgSec.getInt("Fire_Ticks", w.damageFireTicks);
            w.damageOwnerImmunity = dmgSec.getBoolean("Enable_Owner_Immunity", w.damageOwnerImmunity);
            w.damageIgnoreTeams = dmgSec.getBoolean("Ignore_Teams", w.damageIgnoreTeams);
            w.damageArmorDamage = dmgSec.getInt("Armor_Damage", w.damageArmorDamage);
        }

        // Visual / Explosion sections (keep original section for services)
        w.visualSection = sec.getConfigurationSection("Ballistics.Visual");
        w.explosionSection = sec.getConfigurationSection("Ballistics.Explosion");

        // Ranging / lifetimes
        w.rangeTicks = sec.getInt("Ballistics.Range_Ticks", w.rangeTicks);
        int lifeTicks = sec.getInt("Ballistics.Entity.Lifespan_Ticks", -1);
        w.projectileLifespanTicks = (lifeTicks > 0) ? lifeTicks : Math.max(w.projectileLifespanTicks, w.rangeTicks);

        // Impact explode
        w.impactExplodeEnabled = sec.getBoolean("Ballistics.Impact_Explode.Enabled", w.impactExplodeEnabled);
        w.impactExplodeDelay = sec.getInt("Ballistics.Impact_Explode.Delay_Ticks", w.impactExplodeDelay);

        // Melee
        ConfigurationSection melee = sec.getConfigurationSection("Melee");
        if (melee != null) {
            w.meleeEnable = melee.getBoolean("Enable_Melee", w.meleeEnable);
            w.meleeAttachmentId = melee.getString("Melee_Attachment", w.meleeAttachmentId);
            w.meleeRange = melee.getDouble("Melee_Range", w.meleeRange);
            w.meleeHitDelay = melee.getInt("Melee_Hit_Delay", w.meleeHitDelay);
            w.meleeMissDelay = melee.getInt("Melee_Miss_Delay", w.meleeMissDelay);
            w.meleeConsumeOnMiss = melee.getBoolean("Consume_On_Miss", w.meleeConsumeOnMiss);
            if (melee.isSet("Damage.Base")) {
                w.meleeDamageBase = melee.getDouble("Damage.Base");
            }
        }

        // Fire modes
        w.singleAction = sec.getBoolean("Trigger.Single_Action", w.singleAction);

        // Multipliers
        double defHead = sec.getDouble("HitDetection.Headshot_Multiplier", w.headMultiplier);
        w.headMultiplier = sec.getDouble("HitDetection.Multipliers.Head", defHead);
        w.bodyMultiplier = sec.getDouble("HitDetection.Multipliers.Body", w.bodyMultiplier);
        w.limbMultiplier = sec.getDouble("HitDetection.Multipliers.Limb", w.limbMultiplier);

        // Explosion advanced
        if (w.explosionSection != null) {
            w.explosionKnockbackMultiplier = w.explosionSection.getDouble("Knockback_Multiplier", w.explosionKnockbackMultiplier);
            w.explosionExposure = w.explosionSection.getString("Explosion_Exposure", w.explosionExposure);
            w.explosionShape = w.explosionSection.getString("Explosion_Shape", w.explosionShape);

            ConfigurationSection etd = w.explosionSection.getConfigurationSection("Explosion_Type_Data");
            if (etd != null) {
                w.expYield = etd.getDouble("Yield", w.expYield);
                // Radius fallback
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
                w.expRays = w.expRays;
            }

            ConfigurationSection det = w.explosionSection.getConfigurationSection("Detonation");
            if (det != null) {
                w.detonationDelayAfterImpact = det.getInt("Delay_After_Impact", w.detonationDelayAfterImpact);
                w.detonationRemoveProjectile = det.getBoolean("Remove_Projectile_On_Detonation", w.detonationRemoveProjectile);
                ConfigurationSection iw = det.getConfigurationSection("Impact_When");
                if (iw != null) {
                    w.detonationOnSpawn = iw.getBoolean("Spawn", w.detonationOnSpawn);
                    w.detonationOnEntity = iw.getBoolean("Entity", w.detonationOnEntity);
                    w.detonationOnBlock = iw.getBoolean("Block", w.detonationOnBlock);
                }
            }

            ConfigurationSection cl = w.explosionSection.getConfigurationSection("Cluster_Bomb");
            if (cl != null) {
                w.clusterSplitProjectile = cl.getString("Split_Projectile", w.clusterSplitProjectile);
                w.clusterProjectileSpeed = cl.getDouble("Projectile_Speed", w.clusterProjectileSpeed);
                w.clusterNumberOfBombs = cl.getInt("Number_Of_Bombs", w.clusterNumberOfBombs);
                w.clusterNumberOfSplits = cl.getInt("Number_Of_Splits", w.clusterNumberOfSplits);

                ConfigurationSection cdet = cl.getConfigurationSection("Detonation");
                if (cdet != null) {
                    w.clusterDetonationDelayAfterImpact = cdet.getInt("Delay_After_Impact", w.clusterDetonationDelayAfterImpact);
                    w.clusterRemoveProjectileOnDetonation = cdet.getBoolean("Remove_Projectile_On_Detonation", w.clusterRemoveProjectileOnDetonation);
                    ConfigurationSection ciw = cdet.getConfigurationSection("Impact_When");
                    if (ciw != null) {
                        w.clusterImpactSpawn = ciw.getBoolean("Spawn", w.clusterImpactSpawn);
                        w.clusterImpactEntity = ciw.getBoolean("Entity", w.clusterImpactEntity);
                        w.clusterImpactBlock = ciw.getBoolean("Block", w.clusterImpactBlock);
                    }
                }
            }

            ConfigurationSection air = w.explosionSection.getConfigurationSection("Airstrike");
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

                ConfigurationSection adet = air.getConfigurationSection("Detonation");
                if (adet != null) {
                    w.airstrikeDetonationDelayAfterImpact = adet.getInt("Delay_After_Impact", w.airstrikeDetonationDelayAfterImpact);
                    w.airstrikeRemoveProjectileOnDetonation = adet.getBoolean("Remove_Projectile_On_Detonation", w.airstrikeRemoveProjectileOnDetonation);
                    ConfigurationSection aiw = adet.getConfigurationSection("Impact_When");
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
