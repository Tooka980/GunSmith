package com.gunsmith.commands;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.config.WeaponConfig;
import com.gunsmith.service.ListGui;
import com.gunsmith.service.WeaponRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class GmCommand implements CommandExecutor {
    private final GunSmithPlugin plugin; private final WeaponRegistry registry; private final ListGui listGui;
    public GmCommand(GunSmithPlugin plugin, WeaponRegistry registry, ListGui listGui){
        this.plugin=plugin; this.registry=registry; this.listGui=listGui;
    }
    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (args.length==0){
            sender.sendMessage("§e/gm list §7- open weapon list GUI");
            sender.sendMessage("§e/gm reload §7- reload configs");
            sender.sendMessage("§e/gm give <weaponId> <player> §7- give weapon");
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)){
            case "list" -> {
                if (!(sender instanceof Player p)){ sender.sendMessage("Player only."); return true; }
                listGui.open(p, 0); return true;
            }
            case "reload" -> { registry.reload(); sender.sendMessage("§aReloaded. Weapons: "+registry.getWeaponsCount()); return true; }
            case "give" -> {
                if (args.length<3){ sender.sendMessage("Usage: /gm give <weaponId> <player>"); return true; }
                var cfg = registry.getById(args[1]); if (cfg==null){ sender.sendMessage("Weapon not found: "+args[1]); return true; }
                Player target = Bukkit.getPlayerExact(args[2]); if (target==null){ sender.sendMessage("Player not found."); return true; }
                ItemStack is = registry.createWeaponItem(cfg);
                target.getInventory().addItem(is);
                sender.sendMessage("§aGave §e"+cfg.id+" §ato §e"+target.getName());
                return true;
            }
        }
        return false;
    }
}
