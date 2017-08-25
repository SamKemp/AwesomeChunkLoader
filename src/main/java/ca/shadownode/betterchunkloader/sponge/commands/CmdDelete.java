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
import org.spongepowered.api.entity.living.player.Player;

public class CmdDelete implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public CmdDelete(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
        Optional<String> playerName = commandContext.<String>getOne("player");
        Optional<String> loaderType = commandContext.<String>getOne("type");

        HashMap<String, String> args = new HashMap<>();

        if (!loaderType.isPresent()) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.usage, args));
            return CommandResult.empty();
        }

        args.put("type", loaderType.get());

        if (playerName.isPresent()) {
            args.put("player", playerName.get());
            if (!sender.hasPermission("betterchunkloader.commands.delete.others")) {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.others.noPermission, args));
                return CommandResult.empty();
            }
            Optional<UUID> playerUUID = Utilities.getPlayerUUID(playerName.get());
            if (!playerUUID.isPresent()) {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.noPlayerExists, args));
                return CommandResult.empty();
            }
            List<ChunkLoader> clList = plugin.getDataStore().getChunkLoadersByOwner(playerUUID.get());
            switch (loaderType.get()) {
                case "online": {
                    int success = 0;
                    for (ChunkLoader chunkLoader : clList) {
                        if (!chunkLoader.isAlwaysOn()) {
                            plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                            plugin.getDataStore().removeChunkLoader(chunkLoader);
                            success++;
                        }
                    }
                    if (success > 0) {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.others.success, args));
                        return CommandResult.success();
                    } else {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.others.failure, args));
                        return CommandResult.success();
                    }
                }
                case "alwaysOn": {
                    int success = 0;
                    for (ChunkLoader chunkLoader : clList) {
                        if (chunkLoader.isAlwaysOn()) {
                            plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                            plugin.getDataStore().removeChunkLoader(chunkLoader);
                            success++;
                        }
                    }
                    if (success > 0) {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.others.success, args));
                        return CommandResult.success();
                    } else {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.others.failure, args));
                        return CommandResult.success();
                    }
                }
                default: {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.invalidType, args));
                    return CommandResult.empty();
                }
            }
        } else {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                List<ChunkLoader> clList = plugin.getDataStore().getChunkLoadersByOwner(player.getUniqueId());
                switch (loaderType.get()) {
                    case "online": {
                        int success = 0;
                        for (ChunkLoader chunkLoader : clList) {
                            if (!chunkLoader.isAlwaysOn()) {
                                plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                                plugin.getDataStore().removeChunkLoader(chunkLoader);
                                success++;
                            }
                        }
                        if (success > 0) {
                            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.own.success, args));
                            return CommandResult.success();
                        } else {
                            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.own.failure, args));
                            return CommandResult.success();
                        }
                    }
                    case "alwayson": {
                        int success = 0;
                        for (ChunkLoader chunkLoader : clList) {
                            if (chunkLoader.isAlwaysOn()) {
                                plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                                plugin.getDataStore().removeChunkLoader(chunkLoader);
                                success++;
                            }
                        }
                        if (success > 0) {
                            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.own.success, args));
                            return CommandResult.success();
                        } else {
                            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.own.failure, args));
                            return CommandResult.success();
                        }
                    }
                    default: {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.invalidType, args));
                        return CommandResult.empty();
                    }
                }
            } else {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.delete.consoleError, args));
                return CommandResult.empty();
            }
        }
    }

    public List<ChunkLoader> getChunkLoadersByType(UUID owner, Boolean type) {
        List<ChunkLoader> chunkLoaders = new ArrayList<>();
        plugin.getDataStore().getChunkLoadersByOwner(owner).stream().filter((cl) -> (cl.isAlwaysOn().equals(type))).forEachOrdered((cl) -> {
            chunkLoaders.add(cl);
        });
        return chunkLoaders;
    }
}
