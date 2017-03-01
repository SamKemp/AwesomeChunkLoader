 package ca.shadownode.betterchunkloader.sponge.commands;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.util.ArrayList;
import java.util.List;

public class CmdPurge implements CommandExecutor {

    private final BetterChunkLoader plugin;
    
    public CmdPurge(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {

        if (!sender.hasPermission("betterchunkloader.purge")) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdNoPermission));
            return CommandResult.empty();
        }

        List<ChunkLoader> chunkLoaders = new ArrayList<>(plugin.getDataStore().getChunkLoaders());
        chunkLoaders.stream().filter((cl) -> !cl.blockCheck()).forEachOrdered((cl) -> {
            plugin.getDataStore().removeChunkLoader(cl);
        });

        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdPurgeSuccess));
        return CommandResult.success();
    }
}
