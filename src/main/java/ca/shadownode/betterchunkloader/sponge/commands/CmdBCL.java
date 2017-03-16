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
        plugin.getPaginationService().builder()
                .contents(Utilities.parseMessageList(plugin.getConfig().getMessages().commands.usage.items))
                .title(Utilities.parseMessage(plugin.getConfig().getMessages().commands.usage.title))
                .padding(Utilities.parseMessage(plugin.getConfig().getMessages().commands.usage.padding))
                .sendTo(sender);
        return CommandResult.success();
    }
}
