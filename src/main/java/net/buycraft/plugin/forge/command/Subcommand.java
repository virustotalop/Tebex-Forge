package net.buycraft.plugin.forge.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public abstract class Subcommand  {

    private final String name;
    private final String usage;

    public Subcommand(String name, String usage) {
        this.name = name;
        this.usage = usage;
    }

    public String getName() {
        return this.name;
    }

    public String getUsage() {
        return this.usage;
    }

    public abstract String getI18n();
    public abstract void execute(MinecraftServer server, ICommandSender sender, String[] args);
}
