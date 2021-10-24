package net.buycraft.plugin.forge.command;

import net.buycraft.plugin.forge.BuycraftPlugin;
import net.buycraft.plugin.forge.util.ForgeMessageUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public class BuyCommand extends CommandBase {

    private static final String BREAK = "                                            ";

    private final BuycraftPlugin plugin;
    private final String alias;

    public BuyCommand(BuycraftPlugin plugin, String alias) {
        this.plugin = plugin;
        this.alias = alias;
    }

    @Override
    public String getName() {
        return this.alias;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/" + this.alias;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if(plugin.getServerInformation() == null) {
            ForgeMessageUtil.sendMessage(sender,
                    new TextComponentString(ForgeMessageUtil.format("information_no_server"))
                    .setStyle(BuycraftPlugin.ERROR_STYLE));
            return;
        }

        this.sendBreak(sender);
        ForgeMessageUtil.sendMessage(sender, new TextComponentString(ForgeMessageUtil.format("To view the webstore, click this link: "))
                .setStyle(new Style().setColor(TextFormatting.GREEN).setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plugin.getServerInformation().getAccount().getDomain()))));
        ForgeMessageUtil.sendMessage(sender, new TextComponentString(plugin.getServerInformation().getAccount().getDomain()).setStyle(new Style().setColor(TextFormatting.BLUE)
        .setUnderlined(true)
        .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plugin.getServerInformation().getAccount().getDomain()))));
        this.sendBreak(sender);
    }

    private void sendBreak(ICommandSender sender) {
        ForgeMessageUtil.sendMessage(sender, new TextComponentString(BREAK).setStyle(new Style().setStrikethrough(true)));
    }
}
