package ca.shadownode.betterchunkloader.sponge.commands;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import java.util.ArrayList;
import java.util.HashMap;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CmdDelete implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public CmdDelete(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
        if (!commandContext.getOne("player").isPresent()) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.usage));
            return CommandResult.empty();
        }
        String playerName = (String) commandContext.getOne("player").get();
        Optional<String> loaderType = commandContext.<String>getOne("type");

        Optional<UUID> playerUUID = Utilities.getPlayerUUID(playerName);
        if (!playerUUID.isPresent()) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.noPlayerExists));
            return CommandResult.empty();
        }

        HashMap<String, String> args = new HashMap<>();
        args.put("player", playerName);
        args.put("playerUUID", playerUUID.get().toString());
        
        List<ChunkLoader> clList = new ArrayList<>();
        if (loaderType.isPresent()) {
            args.put("type", loaderType.get());
            boolean alwaysOn = !loaderType.get().equals("online");
            clList = getChunkLoadersByType(playerUUID.get(), alwaysOn);
        } else {
            args.put("type", "all");
            plugin.getDataStore().getChunkLoadersByOwner(playerUUID.get());
        }
        if (clList.isEmpty()) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.failure, args));
            return CommandResult.empty();
        }
        for (ChunkLoader chunkLoader : clList) {
            plugin.getDataStore().removeChunkLoader(chunkLoader);
        }
        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.success, args));
        plugin.getLogger().info(sender.getName() + " deleted all chunk loaders placed by " + playerName);
        return CommandResult.success();
    }

    public List<ChunkLoader> getChunkLoadersByType(UUID owner, Boolean type) {
        List<ChunkLoader> chunkLoaders = new ArrayList<>();
        plugin.getDataStore().getChunkLoadersByOwner(owner).stream().filter((cl) -> (cl.isAlwaysOn().equals(type))).forEachOrdered((cl) -> {
            chunkLoaders.add(cl);
        });
        return chunkLoaders;
    }
}
