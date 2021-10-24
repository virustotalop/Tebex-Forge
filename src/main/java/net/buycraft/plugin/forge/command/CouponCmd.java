package net.buycraft.plugin.forge.command;

import net.buycraft.plugin.forge.BuycraftPlugin;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CouponCmd extends SubCommand {
    private final BuycraftPlugin plugin;

    public CouponCmd(final BuycraftPlugin plugin) {
        super("coupon", "/tebex coupon <create/delete>");
        this.plugin = plugin;
    }

    /*public int create(CommandContext<CommandSource> context) {
        if (plugin.getApiClient() == null) {
            ForgeMessageUtil.sendMessage(context.getSource(), new TextComponentString(ForgeMessageUtil.format("generic_api_operation_error"))
                    .setStyle(BuycraftPlugin.ERROR_STYLE));
            return 0;
        }

        final Coupon coupon;
        try {
            coupon = CouponUtil.parseArguments(StringArgumentType.getString(context, "data").split(" "));
        } catch (Exception e) {
            ForgeMessageUtil.sendMessage(context.getSource(), new TextComponentString(ForgeMessageUtil.format("coupon_creation_arg_parse_failure", e.getMessage()))
                    .setStyle(BuycraftPlugin.ERROR_STYLE));
            return 0;
        }

        plugin.getPlatform().executeAsync(() -> {
            try {
                plugin.getApiClient().createCoupon(coupon).execute();
                ForgeMessageUtil.sendMessage(context.getSource(), new TextComponentString(ForgeMessageUtil.format("coupon_creation_success", coupon.getCode()))
                        .setStyle(BuycraftPlugin.SUCCESS_STYLE));
            } catch (IOException e) {
                ForgeMessageUtil.sendMessage(context.getSource(), new TextComponentString(ForgeMessageUtil.format("generic_api_operation_error"))
                        .setStyle(BuycraftPlugin.ERROR_STYLE));
            }
        });

        return 1;
    }

    public int delete(CommandContext<CommandSource> context) {
        if (plugin.getApiClient() == null) {
            ForgeMessageUtil.sendMessage(context.getSource(), new TextComponentString(ForgeMessageUtil.format("generic_api_operation_error"))
                    .setStyle(BuycraftPlugin.ERROR_STYLE));
            return 0;
        }

        String code = StringArgumentType.getString(context, "code");
        plugin.getPlatform().executeAsync(() -> {
            try {
                plugin.getApiClient().deleteCoupon(code).execute();
                ForgeMessageUtil.sendMessage(context.getSource(), new TextComponentString(ForgeMessageUtil.format("coupon_deleted")).setStyle(BuycraftPlugin.SUCCESS_STYLE));
            } catch (Exception e) {
                ForgeMessageUtil.sendMessage(context.getSource(), new TextComponentString(e.getMessage()).setStyle(BuycraftPlugin.ERROR_STYLE));
            }
        });

        return 1;
    }*/

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

    }
}
