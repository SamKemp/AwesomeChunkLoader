package ca.shadownode.betterchunkloader.sponge.commands;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.util.List;

public class CmdPurge implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public CmdPurge(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
        List<ChunkLoader> chunkLoaders = plugin.getDataStore().getChunkLoaders();
        int count = 0;
        count = chunkLoaders.stream().filter((chunkLoader) -> (!chunkLoader.blockCheck())).map((chunkLoader) -> {

            plugin.getDataStore().getPlayerData(chunkLoader.getOwner()).ifPresent((playerData) -> {
                if(chunkLoader.isAlwaysOn()) {
                    playerData.addAlwaysOnChunksAmount(chunkLoader.getChunks());
                }else{
                    playerData.addOnlineChunksAmount(chunkLoader.getChunks());
                }
            });

            plugin.getDataStore().removeChunkLoader(chunkLoader);
            return chunkLoader;
        }).map((_item) -> 1).reduce(count, Integer::sum);
        if (count > 0) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.purge.success));
            return CommandResult.success();
        } else {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.purge.failure));
            return CommandResult.empty();
        }
    }
}
