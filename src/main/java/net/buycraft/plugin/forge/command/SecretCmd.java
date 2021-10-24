package net.buycraft.plugin.forge.command;

import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.forge.BuycraftPlugin;
import net.buycraft.plugin.forge.util.ForgeMessageUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

public class SecretCmd extends Subcommand {

    private final BuycraftPlugin plugin;

    public SecretCmd(final BuycraftPlugin plugin) {
        super("secret", "secret <secret>");
        this.plugin = plugin;
    }

    @Override
    public String getI18n() {
        return "usage_secret";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (sender != server) {
            ForgeMessageUtil.sendMessage(sender, new TextComponentString(ForgeMessageUtil.format("secret_console_only"))
                    .setStyle(BuycraftPlugin.ERROR_STYLE));
            return;
        }

        String secret = findSecret(args);
        if(secret == null) {
            ForgeMessageUtil.sendMessage(sender, new TextComponentString(
                    ForgeMessageUtil.format("secret_does_not_work"))
                    .setStyle(BuycraftPlugin.ERROR_STYLE));
            return;
        }
        plugin.getPlatform().executeAsync(() -> {
            String currentKey = plugin.getConfiguration().getServerKey();
            BuyCraftAPI client = BuyCraftAPI.create(secret, plugin.getHttpClient());

            try {
                plugin.updateInformation(client);
            } catch (IOException e) {
                plugin.getLogger().error("Unable to verify secret", e);
                ForgeMessageUtil.sendMessage(sender, new TextComponentString(ForgeMessageUtil.format("secret_does_not_work"))
                        .setStyle(BuycraftPlugin.ERROR_STYLE));
                return;
            }

            ServerInformation information = plugin.getServerInformation();
            plugin.setApiClient(client);
            plugin.getConfiguration().setServerKey(secret);

            try {
                plugin.saveConfiguration();
            } catch (IOException e) {
                ForgeMessageUtil.sendMessage(sender, new TextComponentString(ForgeMessageUtil.format("secret_cant_be_saved"))
                        .setStyle(BuycraftPlugin.ERROR_STYLE));
            }

            ForgeMessageUtil.sendMessage(sender, new TextComponentString(ForgeMessageUtil.format("secret_success",
                    information.getServer().getName(), information.getAccount().getName())).setStyle(BuycraftPlugin.SUCCESS_STYLE));

            boolean repeatChecks = false;
            if (currentKey.equals("INVALID")) {
                repeatChecks = true;
            }

            plugin.getDuePlayerFetcher().run(repeatChecks);
        });
    }

    private String findSecret(String[] args) {
        if(args.length > 0) {
            return args[0];
        }
        return null;
    }
}
