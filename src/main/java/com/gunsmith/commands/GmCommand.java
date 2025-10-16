package com.gunsmith.commands;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.service.AmmoService;
import com.gunsmith.service.ListGui;
import com.gunsmith.service.WeaponRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Paperの JavaPlugin#registerCommand を使う実装。
 * 依存を極力抑え、既存サービス(API差異)の違いはリフレクションで吸収します。
 */
public final class GmCommand extends Command implements TabCompleter {

    private final GunSmithPlugin plugin;
    private final WeaponRegistry registry;
    private final AmmoService ammo;
    private final ListGui listGui;

    public GmCommand(GunSmithPlugin plugin, WeaponRegistry registry, AmmoService ammo, ListGui listGui) {
        super("gm");
        this.plugin = plugin;
        this.registry = registry;
        this.ammo = ammo;
        this.listGui = listGui;

        setDescription("GunSmith management command");
        setPermissionMessage("You don't have permission.");
        // 権限はサブコマンド内で個別に判断
        setAliases(Arrays.asList("gunsmith"));
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§7[GunSmith] Usage: /gm <list|reload|give>");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage("§cPlayers only.");
                    return true;
                }
                // ListGui は非staticのはずなのでインスタンスから開く
                try {
                    listGui.open(p, 0);
                } catch (Throwable t) {
                    sender.sendMessage("§cList GUI not available: " + t.getClass().getSimpleName());
                }
                return true;
            }

            case "reload" -> {
                if (!sender.hasPermission("gunsmith.admin")) {
                    sender.sendMessage("§cNo permission (gunsmith.admin).");
                    return true;
                }
                try { plugin.reloadConfig(); } catch (Throwable ignored) {}
                // Weapon/Ammo のリロードはメソッドが実装されている場合だけ呼ぶ
                try {
                    Method m = registry.getClass().getMethod("reload");
                    m.invoke(registry);
                } catch (NoSuchMethodException ignored) {
                } catch (Throwable t) {
                    sender.sendMessage("§e[GunSmith] registry.reload() failed: " + t.getClass().getSimpleName());
                }
                try {
                    Method m = ammo.getClass().getMethod("reload");
                    m.invoke(ammo);
                } catch (NoSuchMethodException ignored) {
                } catch (Throwable t) {
                    sender.sendMessage("§e[GunSmith] ammo.reload() failed: " + t.getClass().getSimpleName());
                }
                sender.sendMessage("§a[GunSmith] Reloaded.");
                return true;
            }

            case "give" -> {
                if (!(sender.hasPermission("gunsmith.give") || sender.hasPermission("gunsmith.admin"))) {
                    sender.sendMessage("§cNo permission (gunsmith.give).");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§7Usage: /gm give <weaponId> [player]");
                    return true;
                }
                String weaponId = args[1];

                Player target;
                if (args.length >= 3) {
                    target = Bukkit.getPlayerExact(args[2]);
                    if (target == null) {
                        sender.sendMessage("§cPlayer not found: " + args[2]);
                        return true;
                    }
                } else {
                    if (!(sender instanceof Player p)) {
                        sender.sendMessage("§cSpecify a player when run from console.");
                        return true;
                    }
                    target = p;
                }

                // registry から WeaponConfig を引く（get(String) 等のAPI差異を吸収）
                Object weapon = null;
                try {
                    Method m = registry.getClass().getMethod("get", String.class);
                    weapon = m.invoke(registry, weaponId);
                } catch (NoSuchMethodException ignored) {
                    try {
                        Method m2 = registry.getClass().getMethod("byId", String.class);
                        weapon = m2.invoke(registry, weaponId);
                    } catch (Throwable ignored2) {}
                } catch (Throwable ignored) {}

                // 大文字小文字緩和（getAllIds() があれば使う）
                if (weapon == null) {
                    try {
                        Method all = registry.getClass().getMethod("getAllIds");
                        @SuppressWarnings("unchecked")
                        List<String> ids = (List<String>) all.invoke(registry);
                        String lower = weaponId.toLowerCase(Locale.ROOT);
                        for (String id : ids) {
                            if (id != null && id.toLowerCase(Locale.ROOT).equals(lower)) {
                                try {
                                    Method m = registry.getClass().getMethod("get", String.class);
                                    weapon = m.invoke(registry, id);
                                } catch (Throwable ignored3) {}
                                break;
                            }
                        }
                    } catch (Throwable ignored) {}
                }

                if (weapon == null) {
                    sender.sendMessage("§cUnknown weapon: " + weaponId);
                    return true;
                }

                // ItemStack を作る（createItem(WeaponConfig) or createItem(String) 等に対応）
                ItemStack item = null;
                try {
                    Method m = registry.getClass().getMethod("createItem", weapon.getClass());
                    item = (ItemStack) m.invoke(registry, weapon);
                } catch (NoSuchMethodException e) {
                    try {
                        Method m2 = registry.getClass().getMethod("createItemById", String.class);
                        item = (ItemStack) m2.invoke(registry, weaponId);
                    } catch (Throwable ignored2) {}
                } catch (Throwable ignored) {}

                if (item == null) {
                    sender.sendMessage("§cCannot create item for: " + weaponId);
                    return true;
                }

                var left = target.getInventory().addItem(item);
                if (!left.isEmpty()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), item);
                }
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

    // タブ補完は軽量実装（Brigadier非使用）
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> base = Arrays.asList("list", "reload", "give");
            String p = args[0].toLowerCase(Locale.ROOT);
            List<String> out = new ArrayList<>();
            for (String s : base) if (s.startsWith(p)) out.add(s);
            return out;
        }
        if (args.length == 2 && "give".equalsIgnoreCase(args[0])) {
            // Weapon IDs が取れるならサジェスト
            try {
                Method all = registry.getClass().getMethod("getAllIds");
                @SuppressWarnings("unchecked")
                List<String> ids = (List<String>) all.invoke(registry);
                String p = args[1].toLowerCase(Locale.ROOT);
                List<String> out = new ArrayList<>();
                for (String id : ids) if (id != null && id.toLowerCase(Locale.ROOT).startsWith(p)) out.add(id);
                return out;
            } catch (Throwable ignored) {
                return List.of();
            }
        }
        if (args.length == 3 && "give".equalsIgnoreCase(args[0])) {
            String p = args[2].toLowerCase(Locale.ROOT);
            List<String> out = new ArrayList<>();
            for (Player pl : Bukkit.getOnlinePlayers()) {
                if (pl.getName().toLowerCase(Locale.ROOT).startsWith(p)) out.add(pl.getName());
            }
            return out;
        }
        return List.of();
    }
}
