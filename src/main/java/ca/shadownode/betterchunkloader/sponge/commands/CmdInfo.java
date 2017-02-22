package ca.shadownode.betterchunkloader.sponge.commands;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import java.util.*;

public class CmdInfo implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public CmdInfo(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        if (!commandSource.hasPermission("betterchunkloader.info")) {
            commandSource.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdNoPermission));
            return CommandResult.empty();
        }

        List<ChunkLoader> chunkLoaders = plugin.getDataStore().getChunkLoaders();

        if (chunkLoaders.isEmpty()) {
            commandSource.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdInfoFailure));
            return CommandResult.success();
        }

        Integer alwaysOnLoaders = 0, onlineOnlyLoaders = 0, alwaysOnChunks = 0, onlineOnlyChunks = 0, maxChunksCount = 0, players = 0;
        HashMap<UUID, Integer> loadedChunksForPlayer = new HashMap<>();

        for (ChunkLoader chunkLoader : chunkLoaders) {
            if (chunkLoader.isAlwaysOn()) {
                alwaysOnLoaders++;
                alwaysOnChunks += chunkLoader.getChunks();
            } else {
                onlineOnlyLoaders++;
                onlineOnlyChunks += chunkLoader.getChunks();
            }

            Integer count = loadedChunksForPlayer.get(chunkLoader.getOwner());
            if (count == null) {
                count = 0;
            }
            count += chunkLoader.getChunks();
            loadedChunksForPlayer.put(chunkLoader.getOwner(), count);
        }

        players = loadedChunksForPlayer.size();

        for (Map.Entry<UUID, Integer> entry : loadedChunksForPlayer.entrySet()) {
            if (maxChunksCount < entry.getValue()) {
                maxChunksCount = entry.getValue();
            }
        }
        
        for(String message : plugin.getConfig().cmdInfoSuccess) {
            commandSource.sendMessage(Utilities.parseMessage(message, new String[]{
                String.valueOf(onlineOnlyLoaders),
                String.valueOf(onlineOnlyChunks),
                String.valueOf(alwaysOnLoaders),
                String.valueOf(alwaysOnChunks),
                String.valueOf(players)
            }));
        }
        
        return CommandResult.success();
    }
}
