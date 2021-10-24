package net.buycraft.plugin.forge.command;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;

public class TebexRootCmd extends CommandBase {

    private final MinecraftServer server;
    private final String alias;
    private final Map<String, SubCommand> subCommand = new HashMap<>();

    public TebexRootCmd(MinecraftServer server, String alias) {
        this.server = server;
        this.alias = alias;
    }

    public TebexRootCmd addChild(SubCommand command) {
        this.subCommand.put(command.getName(), command);
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
