package com.gunsmith.service;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.config.WeaponConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class FireService {

    private final GunSmithPlugin plugin;
    private final WeaponRegistry registry;
    private final HomingService homingService;
    private final VisualService visualService;
    private final AmmoService ammoService;
    private final ExplosionService explosionService;
    private final AirstrikeService airstrikeService;
    private final Map<UUID, Integer> currentModeIndex = new HashMap<>();
    private final Map<UUID, Boolean> singleActionCocked = new HashMap<>();
    private final Random rng = new Random();

    public FireService(GunSmithPlugin plugin, WeaponRegistry registry, HomingService homingService, VisualService visualService, AmmoService ammoService) {
        this.plugin = plugin;
        this.registry = registry;
        this.homingService = homingService;
        this.visualService = visualService;
        this.ammoService = ammoService;
        this.explosionService = new ExplosionService(plugin, visualService);
        this.airstrikeService = new AirstrikeService(plugin, visualService);
    }

    public void cycleFireMode(Player p, WeaponConfig w) {
        if (w.selectFireOrder == null || w.selectFireOrder.isEmpty()) return;
        if (w.singleAction){
            boolean cocked = singleActionCocked.getOrDefault(p.getUniqueId(), false);
            singleActionCocked.put(p.getUniqueId(), !cocked);
            p.sendActionBar(Component.text(!cocked ? "Cocked" : "Decocked", NamedTextColor.YELLOW));
            return;
        }

        int idx = currentModeIndex.getOrDefault(p.getUniqueId(), 0);
        idx = (idx + 1) % w.selectFireOrder.size();
        currentModeIndex.put(p.getUniqueId(), idx);
        String mode = w.selectFireOrder.get(idx);
        p.sendActionBar(Component.text("Mode: " + mode, NamedTextColor.GOLD));
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.6f);
    }

    public String getActiveMode(Player p, WeaponConfig w) {
        if (w.selectFireOrder == null || w.selectFireOrder.isEmpty()) return "SEMI";
        int idx = currentModeIndex.getOrDefault(p.getUniqueId(), 0);
        idx = Math.min(idx, w.selectFireOrder.size()-1);
        return w.selectFireOrder.get(idx);
    }

    public void handleTrigger(Player p, WeaponConfig w) {
        String mode = getActiveMode(p, w);
        switch (mode) {
            case "AUTO" -> autoFire(p, w);
            case "BURST" -> burstFire(p, w);
            case "CHARGE" -> semiFire(p, w);
            default -> semiFire(p, w);
        }
    }

    private void autoFire(Player p, WeaponConfig w) {
        int interval = Math.max(1, (int)Math.round(20.0 / Math.max(0.1, w.ratePerSec)));
        new BukkitRunnable() {
            @Override public void run() {
                if (!p.isOnline() || !p.isHandRaised()) { cancel(); return; }
                if (!shootOnce(p, w)) { cancel(); }
            }
        }.runTaskTimer(plugin, 0L, interval);
    }

    private void burstFire(Player p, WeaponConfig w) {
        int interval = Math.max(1, w.burstInterval);
        new BukkitRunnable() {
            int left = Math.max(1, w.burstCount);
            @Override public void run() {
                if (!p.isOnline()) { cancel(); return; }
                if (!shootOnce(p, w)) { cancel(); return; }
                if (--left <= 0) cancel();
            }
        }.runTaskTimer(plugin, 0L, interval);
    }

    private void semiFire(Player p, WeaponConfig w) { shootOnce(p, w); }

    private boolean shootOnce(Player p, WeaponConfig w) {

        if (w.singleAction){
            if (!singleActionCocked.getOrDefault(p.getUniqueId(), false)){
                p.sendActionBar(Component.text("Not cocked", NamedTextColor.RED));
                return false;
            }
            singleActionCocked.put(p.getUniqueId(), false);
        }

        if (!ammoService.consumeIfAllowed(p, w)) { visualService.playEmpty(p); return false; }
        boolean ads = w.sneakAsADS && p.isSneaking();
        Vector dir = p.getEyeLocation().getDirection().normalize();
        double base = ads ? 0.16 : 0.30;
        double yaw = (Math.random()-0.5) * Math.toRadians(base*2);
        double pitch = (Math.random()-0.5) * Math.toRadians(base*2);
        dir.add(new Vector(yaw, pitch, 0)).normalize();

        if ("ITEM".equalsIgnoreCase(w.mode)) return fireItemProjectile(p, w, dir);
        if ("RAY".equalsIgnoreCase(w.mode)) return fireRay(p, w, dir);
        return firePhysicalProjectile(p, w, dir);
    }

    private boolean firePhysicalProjectile(Player p, WeaponConfig w, Vector dir) {
        
        if (w.entityType != null && w.entityType.toLowerCase().contains("snowball")){
            Snowball sb = p.getWorld().spawn(p.getEyeLocation().add(dir.multiply(0.2)), Snowball.class, s -> {
                s.setShooter(p);
                s.setVelocity(dir.multiply(Math.max(0.1, w.projectileSpeed)));
            });
            visualService.playShoot(p.getLocation(), w);
            plugin.getProjectileService().attach(sb, p, w);
            return true;
        }
    
        Arrow arrow = p.getWorld().spawn(p.getEyeLocation().add(dir.multiply(0.2)), Arrow.class, a -> {
            a.setShooter(p);
            a.setVelocity(dir.multiply(Math.max(0.1, w.projectileSpeed)));
            a.setGravity(w.gravityEnabled);
            a.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            a.setPierceLevel((byte)Math.max(0, w.penetrationMaxEntities));
            double dmg = w.damageBase + ammoService.getLastAmmoDamage(p.getUniqueId());
            a.setDamage(Math.max(0.0, dmg));
        });
        visualService.playShoot(p.getLocation(), w);
        if (w.homingEnabled) homingService.trackHoming(arrow, p, w);
        // attach metadata & timers
        plugin.getProjectileService().attach(arrow, p, w);
        return true;
    }

    private boolean fireRay(Player p, WeaponConfig w, Vector dir){
        Location start = p.getEyeLocation();
        double maxDist = 100.0;
        int blocksLeft = Math.max(0, w.penetrationMaxBlocks);
        int entitiesLeft = Math.max(1, w.penetrationMaxEntities);
        Location cur = start.clone();
        Vector v = dir.clone();
        Set<String> allow = Set.copyOf(w.penetrationAllowBlocks);

        while (maxDist > 0 && (blocksLeft >= 0 || entitiesLeft > 0)){
            RayTraceResult res = p.getWorld().rayTrace(cur, v, maxDist, FluidCollisionMode.NEVER, true, 0.25, e -> e!=p);
            if (res == null) break;
            maxDist -= res.getHitPosition().toLocation(p.getWorld()).distance(cur);
            cur = res.getHitPosition().toLocation(p.getWorld());
            if (res.getHitEntity() instanceof LivingEntity le && entitiesLeft > 0){
                double dmg = w.damageBase + ammoService.getLastAmmoDamage(p.getUniqueId());
                le.damage(Math.max(0.0, dmg), p);
                entitiesLeft--;
                continue;
            }
            Block b = res.getHitBlock();
            if (b != null){
                String mcid = "minecraft:" + b.getType().name().toLowerCase();
                if (allow.contains(mcid) && blocksLeft > 0){
                    blocksLeft--;
                    cur.add(v.multiply(0.2)); // nudge through
                    continue;
                } else {
                    // stop at solid
                    break;
                }
            }
        }
        p.getWorld().spawnParticle(Particle.CRIT, start, 5, 0.05,0.05,0.05, 0.0);
        p.getWorld().playSound(start, Sound.ENTITY_ARROW_SHOOT, 0.6f, 1.6f);
        return true;
    }

    private boolean fireItemProjectile(Player p, WeaponConfig w, Vector dir) {
        ItemStack vis = new ItemStack(Material.SLIME_BALL);
        Item drop = p.getWorld().dropItem(p.getEyeLocation().add(dir.multiply(0.2)), vis);
        double speed = Math.max(0.1, w.projectileSpeed>0 ? w.projectileSpeed : 1.1);
        drop.setVelocity(dir.multiply(speed));
        drop.setCanPlayerPickup(false);
        drop.setPickupDelay(Integer.MAX_VALUE);
        visualService.playShoot(p.getLocation(), w);

        int fuse = w.explosionSection != null ? w.explosionSection.getInt("Fuse_Ticks", 60) : 60;

        new BukkitRunnable(){
            int age = 0;
            @Override public void run(){
                if (!drop.isValid() || drop.isDead()) { cancel(); return; }
                age++;
                if (age >= fuse){ explode(drop.getLocation(), w); drop.remove(); cancel(); return; }
                if (drop.isOnGround()){ explode(drop.getLocation(), w); drop.remove(); cancel(); }
            }
        }.runTaskTimer(plugin, 1L, 1L);
        return true;
    }

    private void explode(Location loc, WeaponConfig w){
        // Custom explosion
        explosionService.detonate(loc, w);

        // Airstrike (from Explosion section)
        if (w.explosionSection != null && w.explosionSection.getBoolean("Airstrike.Enabled", false)){
            int waves = w.explosionSection.getInt("Airstrike.Waves", 1);
            int calls = w.explosionSection.getInt("Airstrike.Calls_Per_Wave", 4);
            int interval = w.explosionSection.getInt("Airstrike.Interval_Ticks", 20);
            int height = w.explosionSection.getInt("Airstrike.Height", 30);
            double spread = w.explosionSection.getDouble("Airstrike.Horizontal_Spread", 6.0);
            airstrikeService.call(loc, w, waves, calls, interval, height, spread);
        }

        // Cluster (ITEM only): split the thrown item into more thrown items, possibly multiple times
        if (w.explosionSection != null && w.explosionSection.getConfigurationSection("Cluster") != null){
            int times = w.explosionSection.getInt("Cluster.Times", 1);
            int count = w.explosionSection.getInt("Cluster.Count", 4);
            double speed = 0.8;
            for (int t=0;t<times;t++){
                for (int i=0;i<count;i++){
                    Vector dir = randomUnit();
                    ItemStack vis = new ItemStack(Material.SLIME_BALL);
                    Item child = loc.getWorld().dropItem(loc.clone().add(0,0.2,0), vis);
                    child.setVelocity(dir.multiply(speed));
                    child.setCanPlayerPickup(false);
                    child.setPickupDelay(Integer.MAX_VALUE);
                    // each child auto-detonates quickly
                    new BukkitRunnable(){ @Override public void run(){
                        if (!child.isValid()) return;
                        explosionService.detonate(child.getLocation(), w);
                        child.remove();
                    }}.runTaskLater(plugin, 20L);
                }
            }
        }
    }

    private Vector randomUnit(){
        double x = rng.nextDouble()*2-1;
        double y = rng.nextDouble()*2-1;
        double z = rng.nextDouble()*2-1;
        Vector v = new Vector(x,y,z);
        if (v.lengthSquared() < 1e-6) return new Vector(0,1,0);
        return v.normalize();
    }
}
