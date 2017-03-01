package ca.shadownode.betterchunkloader.sponge.commands;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class CmdBCL implements CommandExecutor {

    private final BetterChunkLoader plugin;
    
    public CmdBCL(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext context) throws CommandException {
        plugin.getConfig().cmdRootUsage.forEach((message) -> {
            sender.sendMessage(Utilities.parseMessage(message));
        });
        return CommandResult.success();
    }
}
