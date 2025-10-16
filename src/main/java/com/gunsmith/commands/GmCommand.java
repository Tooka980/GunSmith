package com.gunsmith.commands;

import com.gunsmith.GunSmithPlugin;
import com.gunsmith.service.AmmoService;
import com.gunsmith.service.ListGui;
import com.gunsmith.service.WeaponRegistry;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.resolver.PlayerResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class GmCommands {

    private final GunSmithPlugin plugin;
    private final WeaponRegistry registry;
    private final AmmoService ammo;

    public GmCommands(GunSmithPlugin plugin, WeaponRegistry registry, AmmoService ammo) {
        this.plugin = plugin;
        this.registry = registry;
        this.ammo = ammo;
    }

    public void registerBrigadier(io.papermc.paper.command.brigadier.Commands registrar) {
        // /gm
        LiteralArgumentBuilder<CommandSourceStack> root = literal("gm")
            .requires(src -> true) // ルートは誰でも見える

            // /gm list
            .then(literal("list")
                .executes(ctx -> openListForSelf(ctx))
            )

            // /gm reload
            .then(literal("reload")
                .requires(src -> src.getSender().hasPermission("gunsmith.admin"))
                .executes(ctx -> {
                    plugin.reloadConfig();
                    registry.reload();        // Weapon YAML を再読込（WeaponRegistry に reload() がある前提）
                    ammo.reload();            // Ammo の再読込（無ければ空メソッドを用意）
                    ctx.getSource().getSender().sendMessage(Component.text("[GunSmith] Reloaded."));
                    return 1;
                })
            )

            // /gm give <weaponId> <player>
            .then(literal("give")
                .requires(src -> src.getSender().hasPermission("gunsmith.give") || src.getSender().hasPermission("gunsmith.admin"))
                .then(argument("weapon", ArgumentTypes.string())
                    .suggests((c, b) -> {
                        for (String id : registry.getAllIds()) b.suggest(id);
                        return b.buildFuture();
                    })
                    .then(argument("player", ArgumentTypes.player()) // 1人選択
                        .executes(ctx -> giveWeapon(ctx, /*toSelf*/false))
                    )
                    // /gm give <weapon> だけで自分に
                    .executes(ctx -> giveWeapon(ctx, /*toSelf*/true))
                )
            );

        registrar.register(root, "GunSmith management command");
        // エイリアス /gunsmith も欲しければ下行を有効化
        // registrar.register(literal("gunsmith").redirect(root.build()), "alias for /gm");
    }

    private int openListForSelf(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player p)) {
            ctx.getSource().getSender().sendMessage(Component.text("Players only."));
            return 0;
        }
        ListGui.open(p, 0); // 先頭ページ。GUI内部でページング実装済み前提
        return 1;
    }

    private int giveWeapon(CommandContext<CommandSourceStack> ctx, boolean toSelf) {
        String weaponId = Commands.getArgumentOrDefault(ctx, "weapon", String.class, null);
        if (weaponId == null) {
            ctx.getSource().getSender().sendMessage(Component.text("Specify weapon id."));
            return 0;
        }
        weaponId = weaponId.trim();

        Player target;
        if (toSelf) {
            if (!(ctx.getSource().getExecutor() instanceof Player self)) {
                ctx.getSource().getSender().sendMessage(Component.text("Players only."));
                return 0;
            }
            target = self;
        } else {
            PlayerResolver resolver = Commands.getArgument(ctx, "player", PlayerResolver.class);
            target = resolver.resolve(ctx.getSource()).getFirst();
            if (target == null) {
                ctx.getSource().getSender().sendMessage(Component.text("Player not found."));
                return 0;
            }
        }

        var w = registry.get(weaponId);
        if (w == null) {
            // 一致しない場合は大文字小文字を緩く
            String lower = weaponId.toLowerCase(Locale.ROOT);
            for (String id : registry.getAllIds()) {
                if (id.toLowerCase(Locale.ROOT).equals(lower)) { w = registry.get(id); break; }
            }
        }
        if (w == null) {
            ctx.getSource().getSender().sendMessage(Component.text("Unknown weapon: " + weaponId));
            return 0;
        }

        ItemStack item = registry.createItem(w);
        var invLeft = target.getInventory().addItem(item);
        if (!invLeft.isEmpty()) {
            // インベントリが満杯ならドロップ
            target.getWorld().dropItemNaturally(target.getLocation(), item);
        }

        ctx.getSource().getSender().sendMessage(Component.text("[GunSmith] Gave " + weaponId + " to " + target.getName()));
        if (!toSelf && target.isOnline()) {
            target.sendMessage(Component.text("[GunSmith] You received: " + weaponId));
        }
        return 1;
    }
}
