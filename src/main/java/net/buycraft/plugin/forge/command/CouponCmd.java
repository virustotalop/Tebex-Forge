package net.buycraft.plugin.forge.command;

import net.buycraft.plugin.BuyCraftAPIException;
import net.buycraft.plugin.data.Coupon;
import net.buycraft.plugin.forge.BuycraftPlugin;
import net.buycraft.plugin.shared.util.CouponUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.Arrays;

public class CouponCmd extends Subcommand {
    private final BuycraftPlugin plugin;

    public CouponCmd(final BuycraftPlugin plugin) {
        super("coupon", "coupon <create/delete>");
        this.plugin = plugin;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new TextComponentString(plugin.getI18n().get("usage_coupon_subcommands"))
                    .setStyle(new Style().setColor(TextFormatting.RED)));
            return;
        }
        switch (args[0]) {
            case "create":
                createCoupon(sender, args);
                break;
            case "delete":
                deleteCoupon(sender, args);
                break;
            default:
                sender.sendMessage(new TextComponentString(this.plugin.getI18n().get("usage_coupon_subcommands"))
                .setStyle(new Style().setColor(TextFormatting.RED)));
                break;
        }
    }

    private void createCoupon(final ICommandSender sender, String[] args) {
        String[] stripped = Arrays.copyOfRange(args, 1, args.length);
        final Coupon coupon;
        try {
            coupon = CouponUtil.parseArguments(stripped);
        } catch (Exception e) {
            sender.sendMessage(new TextComponentString(e.getMessage()));
            sender.sendMessage(new TextComponentString(plugin.getI18n().get("coupon_creation_arg_parse_failure",
                    e.getMessage())).setStyle(new Style().setColor(TextFormatting.RED)));
            return;
        }

        plugin.getPlatform().executeAsync(() -> {
            try {
                plugin.getApiClient().createCoupon(coupon).execute();
                sender.sendMessage(new TextComponentString(plugin.getI18n().get("coupon_creation_success",
                        coupon.getCode())).setStyle(new Style().setColor(TextFormatting.GREEN)));
            } catch (IOException e) {
                sender.sendMessage(new TextComponentString(e.getMessage())
                        .setStyle(new Style().setColor(TextFormatting.RED)));

            }
        });
    }

    private void deleteCoupon(final ICommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(new TextComponentString(
                    plugin.getI18n().get("no_coupon_specified"))
                    .setStyle(new Style().setColor(TextFormatting.RED)));
            return;
        }

        final String code = args[1];

        plugin.getPlatform().executeAsync(() -> {
            try {
                plugin.getApiClient().deleteCoupon(code).execute();
                sender.sendMessage(new TextComponentString(plugin.getI18n().get("coupon_deleted"))
                .setStyle(new Style().setColor(TextFormatting.GREEN)));
            } catch (IOException | BuyCraftAPIException e) {
                sender.sendMessage(new TextComponentString(e.getMessage())
                .setStyle(new Style().setColor(TextFormatting.RED)));
            }
        });
    }

    @Override
    public String getI18n() {
        return "usage_coupon";
    }
}
