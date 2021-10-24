package net.buycraft.plugin.forge.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;

public class TebexRootCommand extends CommandBase {

    private final String alias;
    private final Map<String, SubCommand> subCommand = new HashMap<>();

    public TebexRootCommand(String alias) {
        this.alias = alias;
    }

    public TebexRootCommand addChild(SubCommand command) {
        this.subCommand.put(command.getName(), command);
        return this;
    }

    @Override
    public String getName() {
        return this.alias;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

    }
}
