package com.gunsmith.commands;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.service.AmmoService;
import com.gunsmith.service.ListGui;
import com.gunsmith.service.WeaponRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class GmRootCommand extends Command {
    private final GunSmithPlugin plugin;
    private final WeaponRegistry registry;
    private final ListGui listGui;
    private final AmmoService ammo;

    public GmRootCommand(GunSmithPlugin plugin, WeaponRegistry registry, ListGui listGui) {
        super("gm", "GunSmith admin/user command", "/gm <list|reload|give|ammo>", java.util.List.of("gunsmith"));
        this.plugin = plugin;
        this.registry = registry;
        this.listGui = listGui;
        this.ammo = plugin.getAmmoService();
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            help(sender); return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "list" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("Player only."); return true; }
                listGui.open(p, 0);
                return true;
            }
            case "reload" -> {
                registry.reload();
                sender.sendMessage("§aGunSmith reloaded. Weapons: " + registry.getWeaponsCount());
                return true;
            }
            case "give" -> {
                if (args.length < 3) { sender.sendMessage("Usage: /gm give <weaponId> <player>"); return true; }
                var cfg = registry.getById(args[1]);
                if (cfg == null) { sender.sendMessage("Weapon not found: " + args[1]); return true; }
                var target = Bukkit.getPlayerExact(args[2]);
                if (target == null) { sender.sendMessage("Player not found."); return true; }
                ItemStack is = registry.createWeaponItem(cfg);
                target.getInventory().addItem(is);
                sender.sendMessage("§aGave §e" + cfg.id + " §ato §e" + target.getName());
                return true;
            }
            case "ammo" -> {
                if (args.length == 1) { sender.sendMessage("§e/gm ammo list §7| §e/gm ammo give <ammoId> <player> [amount]"); return true; }
                if ("list".equalsIgnoreCase(args[1])){
                    sender.sendMessage("§eAmmos:");
                    for (String id : ammo.getAmmoIds()) sender.sendMessage(" - " + id);
                    return true;
                }
                if ("give".equalsIgnoreCase(args[1])){
                    if (args.length < 4){ sender.sendMessage("Usage: /gm ammo give <ammoId> <player> [amount]"); return true; }
                    String ammoId = args[2];
                    var target = Bukkit.getPlayerExact(args[3]);
                    if (target == null){ sender.sendMessage("Player not found."); return true; }
                    int amount = 64;
                    if (args.length >= 5) try { amount = Integer.parseInt(args[4]); } catch (Exception ignored){}
                    var stack = ammo.createAmmoItem(ammoId, amount);
                    target.getInventory().addItem(stack);
                    sender.sendMessage("§aGave ammo §e"+ammoId+" x"+amount+" §ato §e"+target.getName());
                    return true;
                }
                help(sender); return true;
            }
        }
        return false;
    }

    private void help(CommandSender sender){
        sender.sendMessage("§e/gm list §7- open weapon GUI");
        sender.sendMessage("§e/gm reload §7- reload configs");
        sender.sendMessage("§e/gm give <weaponId> <player> §7- give weapon");
        sender.sendMessage("§e/gm ammo list §7- list ammo IDs");
        sender.sendMessage("§e/gm ammo give <ammoId> <player> [amount] §7- give ammo");
    }

    @Override
    public java.util.List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (args.length == 1) {
            for (String s : new String[]{"list","reload","give","ammo"}) if (s.startsWith(args[0].toLowerCase())) out.add(s);
        } else if (args.length == 2 && "give".equalsIgnoreCase(args[0])) {
            for (var w : registry.getAll()) if (w.id.toLowerCase().startsWith(args[1].toLowerCase())) out.add(w.id);
        } else if (args.length == 3 && "give".equalsIgnoreCase(args[0])) {
            for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers())
                if (p.getName().toLowerCase().startsWith(args[2].toLowerCase())) out.add(p.getName());
        } else if (args.length == 2 && "ammo".equalsIgnoreCase(args[0])) {
            for (String s : new String[]{"list","give"}) if (s.startsWith(args[1].toLowerCase())) out.add(s);
        } else if (args.length == 3 && "ammo".equalsIgnoreCase(args[0]) && "give".equalsIgnoreCase(args[1])) {
            for (String id : ammo.getAmmoIds()) if (id.toLowerCase().startsWith(args[2].toLowerCase())) out.add(id);
        } else if (args.length == 4 && "ammo".equalsIgnoreCase(args[0]) && "give".equalsIgnoreCase(args[1])) {
            for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers())
                if (p.getName().toLowerCase().startsWith(args[3].toLowerCase())) out.add(p.getName());
        }
        return out;
    }
}
