package com.gunsmith.commands;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.service.AmmoService;
import com.gunsmith.service.ListGui;
import com.gunsmith.service.WeaponRegistry;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public final class GmCommand implements BasicCommand {

    private final GunSmithPlugin plugin;
    private final WeaponRegistry registry;
    private final AmmoService ammo;
    private final ListGui listGui;

    public GmCommand(GunSmithPlugin plugin, WeaponRegistry registry, AmmoService ammo, ListGui listGui) {
        this.plugin = plugin;
        this.registry = registry;
        this.ammo = ammo;
        this.listGui = listGui;
    }

    @Override
    public String permission() {
        return "gunsmith.use";
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        final CommandSender sender = source.getSender();

        if (args.length == 0) {
            sender.sendMessage("§7[GunSmith] /gm <list|reload|give>");
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list" -> {
                Player p = (sender instanceof Player sp) ? sp : null;
                if (p == null) { sender.sendMessage("§cPlayers only."); return; }
                try {
                    listGui.open(p, 0);
                } catch (Throwable t) {
                    sender.sendMessage("§cList GUI not available: " + t.getClass().getSimpleName());
                }
            }

            case "reload" -> {
                if (!sender.hasPermission("gunsmith.admin")) { sender.sendMessage("§cNo permission (gunsmith.admin)."); return; }
                try { plugin.reloadConfig(); } catch (Throwable ignored) {}
                callIfExists(registry, "reload");
                callIfExists(ammo, "reload");
                sender.sendMessage("§a[GunSmith] Reloaded.");
            }

            case "give" -> {
                if (!(sender.hasPermission("gunsmith.give") || sender.hasPermission("gunsmith.admin"))) {
                    sender.sendMessage("§cNo permission (gunsmith.give)."); return;
                }
                if (args.length < 2) { sender.sendMessage("§7Usage: /gm give <weaponId> [player]"); return; }

                String weaponId = args[1];

                Player target;
                if (args.length >= 3) {
                    target = Bukkit.getPlayerExact(args[2]);
                    if (target == null) { sender.sendMessage("§cPlayer not found: " + args[2]); return; }
                } else {
                    // 送信者がプレイヤーのときは自分に付与／コンソールなら第3引数必須
                    if (!(sender instanceof Player sp)) { sender.sendMessage("§cSpecify a player when run from console."); return; }
                    target = sp;
                }

                Object w = getWeaponByIdLoose(weaponId);
                if (w == null) { sender.sendMessage("§cUnknown weapon: " + weaponId); return; }

                ItemStack item = createItemFor(w, weaponId);
                if (item == null) { sender.sendMessage("§cCannot create item for: " + weaponId); return; }

                var remained = target.getInventory().addItem(item);
                if (!remained.isEmpty()) target.getWorld().dropItemNaturally(target.getLocation(), item);

                sender.sendMessage("§a[GunSmith] Gave " + weaponId + " to " + target.getName());
                if (!target.equals(sender)) target.sendMessage("§a[GunSmith] You received: " + weaponId);
            }

            default -> sender.sendMessage("§7[GunSmith] /gm <list|reload|give>");
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length == 0) return List.of("list", "reload", "give");

        if (args.length == 1) {
            return filterPrefix(List.of("list","reload","give"), args[0]);
        }

        if (args.length == 2 && "give".equalsIgnoreCase(args[0])) {
            return filterPrefix(getAllIdsSafe(), args[1]);
        }

        if (args.length == 3 && "give".equalsIgnoreCase(args[0])) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[2].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    // ---------- helpers ----------

    private void callIfExists(Object obj, String method) {
        try { obj.getClass().getMethod(method).invoke(obj); } catch (NoSuchMethodException ignored) { } catch (Throwable t) {
            plugin.getLogger().warning(method+"() failed: "+t.getClass().getSimpleName());
        }
    }

    private Object getWeaponByIdLoose(String id) {
        Object w = null;
        try { w = registry.getClass().getMethod("get", String.class).invoke(registry, id); } catch (Throwable ignored) {}
        if (w != null) return w;

        String lower = id.toLowerCase(Locale.ROOT);
        for (String cand : getAllIdsSafe()) {
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

    private List<String> filterPrefix(Collection<String> src, String prefix) {
        String p = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String s : src) if (s != null && s.toLowerCase(Locale.ROOT).startsWith(p)) out.add(s);
        return out;
    }
}
