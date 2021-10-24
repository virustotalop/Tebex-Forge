package net.buycraft.plugin.forge.command;

import net.buycraft.plugin.forge.BuycraftPlugin;
import net.buycraft.plugin.forge.util.ForgeMessageUtil;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.stream.Stream;

public class InfoCmd extends SubCommand {

    private final BuycraftPlugin plugin;

    public InfoCmd(final BuycraftPlugin plugin) {
        super("info", "/tebex info");
        this.plugin = plugin;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (plugin.getApiClient() == null) {
            ForgeMessageUtil.sendMessage(sender, new TextComponentString(ForgeMessageUtil.format("generic_api_operation_error"))
                    .setStyle(BuycraftPlugin.ERROR_STYLE));
        } else if (plugin.getServerInformation() == null) {
            ForgeMessageUtil.sendMessage(sender, new TextComponentString(ForgeMessageUtil.format("information_no_server"))
                    .setStyle(BuycraftPlugin.ERROR_STYLE));
        } else {
            String webstoreURL = plugin.getServerInformation().getAccount().getDomain();
            ITextComponent webstore = new TextComponentString(webstoreURL)
                    .setStyle(new Style().setColor(TextFormatting.GREEN)
                    .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, webstoreURL))
                    .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(webstoreURL))));

            ITextComponent serverInfo = new TextComponentString(plugin.getServerInformation().getServer().getName())
                    .setStyle(new Style().setColor(TextFormatting.GREEN));
            Stream.of(
                    new TextComponentString(ForgeMessageUtil.format("information_title") + " ")
                            .setStyle(new Style().setColor(TextFormatting.GRAY)),
                    new TextComponentString(ForgeMessageUtil.format("information_sponge_server") + " ")
                            .setStyle(new Style().setColor(TextFormatting.GRAY)).appendSibling(serverInfo),
                    new TextComponentString(ForgeMessageUtil.format("information_currency",
                            plugin.getServerInformation().getAccount().getCurrency().getIso4217()))
                            .setStyle(new Style().setColor(TextFormatting.GRAY)),
                    new TextComponentString(ForgeMessageUtil.format("information_domain", ""))
                            .setStyle(new Style().setColor(TextFormatting.GRAY)).appendSibling(webstore)
            ).forEach(message -> ForgeMessageUtil.sendMessage(sender, message));
        }
    }
}
