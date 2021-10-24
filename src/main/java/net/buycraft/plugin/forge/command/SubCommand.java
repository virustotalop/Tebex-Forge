package net.buycraft.plugin.forge.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public abstract class SubCommand extends CommandBase {

    private final String name;
    private final String usage;

    public SubCommand(String name, String usage) {
        this.name = name;
        this.usage = usage;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return this.usage;
    }
}
