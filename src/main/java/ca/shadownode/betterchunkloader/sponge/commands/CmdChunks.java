package ca.shadownode.betterchunkloader.sponge.commands;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.PlayerData;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class CmdChunks implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public CmdChunks(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
        String chunksChangeOperatorElement = commandContext.<String>getOne("change").get();
        String loaderTypeElement = commandContext.<String>getOne("type").get();
        String playerName = commandContext.<String>getOne("player").get();
        Integer changeValue = commandContext.<Integer>getOne("value").get();

        Optional<UUID> playerUUID = Utilities.getPlayerUUID(playerName);
        if (!playerUUID.isPresent()) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.noPlayerExists));
            return CommandResult.empty();
        }
        Optional<PlayerData> playerData = plugin.getDataStore().getPlayerData(playerUUID.get());
        if (!playerData.isPresent()) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.noPlayerExists));
            return CommandResult.empty();
        }

        HashMap<String, String> args = new HashMap<String, String>() {{
            put("target", playerData.get().getName());
            put("targetUUID", playerData.get().getUnqiueId().toString());
            put("online", String.valueOf(playerData.get().getOnlineChunksAmount()));
            put("alwayson", String.valueOf(playerData.get().getAlwaysOnChunksAmount()));
            put("maxOnline", String.valueOf(plugin.getConfig().getCore().chunkLoader.online.maxOnline));
            put("maxAlwaysOn", String.valueOf(plugin.getConfig().getCore().chunkLoader.alwaysOn.maxAlwaysOn));
            put("change", String.valueOf(changeValue));
        }};
        switch (chunksChangeOperatorElement) {
            case "add": {
                switch (loaderTypeElement) {
                    case "alwayson": {
                        args.put("type", "Always On");
                        args.put("limit", String.valueOf(plugin.getConfig().getCore().chunkLoader.alwaysOn.maxAlwaysOn));
                        if (playerData.get().getAlwaysOnChunksAmount() + changeValue > plugin.getConfig().getCore().chunkLoader.alwaysOn.maxAlwaysOn) {
                            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.chunks.add.failure, args));
                            return CommandResult.empty();
                        }
                        playerData.get().addAlwaysOnChunksAmount(changeValue);
                        plugin.getDataStore().updatePlayerData(playerData.get());
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.chunks.add.success, args));
                        return CommandResult.success();
                    }
                    case "online": {
                        args.put("type", "Online Only");
                        args.put("limit", String.valueOf(plugin.getConfig().getCore().chunkLoader.online.maxOnline));
                        if (playerData.get().getOnlineChunksAmount() + changeValue > plugin.getConfig().getCore().chunkLoader.online.maxOnline) {
                            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.chunks.add.failure, args));
                            return CommandResult.empty();
                        }
                        playerData.get().addOnlineChunksAmount(changeValue);
                        plugin.getDataStore().updatePlayerData(playerData.get());
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.chunks.add.success, args));
                        return CommandResult.success();
                    }
                    default: {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.chunks.usage, args));
                        return CommandResult.empty();
                    }
                }
            }
            case "set": {
                if (changeValue < 0) {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.chunks.set.failure, args));
                    return CommandResult.empty();
                }
                switch (loaderTypeElement) {
                    case "alwayson": {
                        args.put("type", "Always On");
                        args.put("limit", String.valueOf(plugin.getConfig().getCore().chunkLoader.alwaysOn.maxAlwaysOn));
                        playerData.get().setAlwaysOnChunksAmount(changeValue);
                        plugin.getDataStore().updatePlayerData(playerData.get());
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.chunks.set.success, args));
                        return CommandResult.success();
                    }
                    case "online": {
                        args.put("type", "Online Only");
                        args.put("limit", String.valueOf(plugin.getConfig().getCore().chunkLoader.online.maxOnline));
                        playerData.get().setOnlineChunksAmount(changeValue);
                        plugin.getDataStore().updatePlayerData(playerData.get());
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.chunks.set.success, args));
                        return CommandResult.success();
                    }
                    default: {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.chunks.usage, args));
                        return CommandResult.empty();
                    }
                }
            }
            default: {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.chunks.usage, args));
                return CommandResult.empty();
            }
        }
    }
}
