package net.buycraft.plugin.forge.command;

import com.mojang.authlib.GameProfile;
import net.buycraft.plugin.forge.BuycraftPlugin;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TebexRootCmd extends CommandBase {

    private final BuycraftPlugin plugin;
    private final MinecraftServer server;
    private final String alias;
    private final Map<String, Subcommand> subcommands = new HashMap<>();

    public TebexRootCmd(BuycraftPlugin plugin, MinecraftServer server, String alias) {
        this.plugin = plugin;
        this.server = server;
        this.alias = alias;
    }

    public TebexRootCmd addChild(Subcommand command) {
        this.subcommands.put(command.getName(), command);
        return this;
    }

    @Override
    public String getName() {
        return this.alias;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/" + this.alias + " <args>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(isOp(sender) || isConsole(sender)) {
            if(args.length > 0) {
                for(Map.Entry<String, Subcommand> entry : this.subcommands.entrySet()) {
                    if(entry.getKey().equalsIgnoreCase(args[0])) {
                        String[] withoutSubcommand = Arrays.copyOfRange(args, 1, args.length);
                        entry.getValue().execute(this.server, sender, withoutSubcommand);
                        return;
                    }
                }
            }
            showHelp(sender);
        }
    }

    private void showHelp(ICommandSender sender) {
        sender.sendMessage(new TextComponentString(plugin.getI18n().get("usage"))
                .setStyle(new Style().setColor(TextFormatting.DARK_AQUA).setBold(true)));
        for (Map.Entry<String, Subcommand> entry : this.subcommands.entrySet()) {
            sender.sendMessage(new TextComponentString("/" + this.alias + " " + entry.getValue().getUsage())
                    .setStyle(new Style().setColor(TextFormatting.GREEN))
                    .appendSibling(new TextComponentString(": " +
                            this.plugin.getI18n().get(entry.getValue().getI18n()))
                            .setStyle(new Style().setColor(TextFormatting.GRAY))));
        }
    }

    private boolean isOp(ICommandSender sender) {
        Entity entity = sender.getCommandSenderEntity();
        if(entity == null || !(entity instanceof EntityPlayer)) {
            return false;
        }
        EntityPlayer player = (EntityPlayer) entity;
        GameProfile profile = player.getGameProfile();
        if(profile == null) {
            return false;
        }
        return server.getPlayerList().canSendCommands(profile);
    }

    private boolean isConsole(ICommandSender sender) {
        return sender == this.server;
    }
}
