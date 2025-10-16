package com.gunsmith.commands;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.service.AmmoService;
import com.gunsmith.service.ListGui;
import com.gunsmith.service.WeaponRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.*;

public final class GmExecutor implements CommandExecutor, TabCompleter {

    private final GunSmithPlugin plugin;
    private final WeaponRegistry registry;
    private final AmmoService ammo;
    private final ListGui listGui;

    public GmExecutor(GunSmithPlugin plugin, WeaponRegistry registry, AmmoService ammo, ListGui listGui) {
        this.plugin = plugin;
        this.registry = registry;
        this.ammo = ammo;
        this.listGui = listGui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§7[GunSmith] Usage: /gm <list|reload|give>");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("§cPlayers only."); return true; }
                try { listGui.open(p, 0); } catch (Throwable t) {
                    sender.sendMessage("§cList GUI not available: " + t.getClass().getSimpleName());
                }
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("gunsmith.admin")) { sender.sendMessage("§cNo permission (gunsmith.admin)."); return true; }
                try { plugin.reloadConfig(); } catch (Throwable ignored) {}
                // registry.reload() / ammo.reload() は存在する場合のみ呼ぶ
                callIfExists(registry, "reload");
                callIfExists(ammo, "reload");
                sender.sendMessage("§a[GunSmith] Reloaded.");
                return true;
            }
            case "give" -> {
                if (!(sender.hasPermission("gunsmith.give") || sender.hasPermission("gunsmith.admin"))) {
                    sender.sendMessage("§cNo permission (gunsmith.give)."); return true;
                }
                if (args.length < 2) { sender.sendMessage("§7Usage: /gm give <weaponId> [player]"); return true; }
                String weaponId = args[1];

                Player target;
                if (args.length >= 3) {
                    target = Bukkit.getPlayerExact(args[2]);
                    if (target == null) { sender.sendMessage("§cPlayer not found: " + args[2]); return true; }
                } else {
                    if (!(sender instanceof Player p)) { sender.sendMessage("§cSpecify a player when run from console."); return true; }
                    target = p;
                }

                Object w = getWeaponByIdLoose(weaponId);
                if (w == null) { sender.sendMessage("§cUnknown weapon: " + weaponId); return true; }

                ItemStack item = createItemFor(w, weaponId);
                if (item == null) { sender.sendMessage("§cCannot create item for: " + weaponId); return true; }

                var remained = target.getInventory().addItem(item);
                if (!remained.isEmpty()) target.getWorld().dropItemNaturally(target.getLocation(), item);

                sender.sendMessage("§a[GunSmith] Gave " + weaponId + " to " + target.getName());
                if (sender != target) target.sendMessage("§a[GunSmith] You received: " + weaponId);
                return true;
            }
            default -> {
                sender.sendMessage("§7[GunSmith] Usage: /gm <list|reload|give>");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return prefix(Arrays.asList("list","reload","give"), args[0]);
        }
        if (args.length == 2 && "give".equalsIgnoreCase(args[0])) {
            List<String> ids = getAllIdsSafe();
            return prefix(ids, args[1]);
        }
        if (args.length == 3 && "give".equalsIgnoreCase(args[0])) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
            return prefix(names, args[2]);
        }
        return Collections.emptyList();
    }

    // ---- helpers ----

    private void callIfExists(Object obj, String method) {
        try { obj.getClass().getMethod(method).invoke(obj); } catch (NoSuchMethodException ignored) { } catch (Throwable t) {
            plugin.getLogger().warning(method+"() failed: "+t.getClass().getSimpleName());
        }
    }

    private Object getWeaponByIdLoose(String id) {
        Object w = null;
        try { w = registry.getClass().getMethod("get", String.class).invoke(registry, id); } catch (Throwable ignored) {}
        if (w != null) return w;

        List<String> all = getAllIdsSafe();
        String lower = id.toLowerCase(Locale.ROOT);
        for (String cand : all) {
            if (cand != null && cand.toLowerCase(Locale.ROOT).equals(lower)) {
                try { return registry.getClass().getMethod("get", String.class).invoke(registry, cand); } catch (Throwable ignored) {}
            }
        }
        return null;
    }

    private ItemStack createItemFor(Object weapon, String id) {
        try {
            // createItem(WeaponConfig)
            Method m = registry.getClass().getMethod("createItem", weapon.getClass());
            return (ItemStack) m.invoke(registry, weapon);
        } catch (NoSuchMethodException e) {
            try {
                // createItemById(String)
                Method m2 = registry.getClass().getMethod("createItemById", String.class);
                return (ItemStack) m2.invoke(registry, id);
            } catch (Throwable ignored2) { return null; }
        } catch (Throwable t) { return null; }
    }

    @SuppressWarnings("unchecked")
    private List<String> getAllIdsSafe() {
        try { return (List<String>) registry.getClass().getMethod("getAllIds").invoke(registry); }
        catch (Throwable ignored) { return Collections.emptyList(); }
    }

    private List<String> prefix(List<String> src, String p) {
        String pref = p == null ? "" : p.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String s : src) if (s != null && s.toLowerCase(Locale.ROOT).startsWith(pref)) out.add(s);
        return out;
    }
}
