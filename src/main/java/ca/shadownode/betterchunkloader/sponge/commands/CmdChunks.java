package ca.shadownode.betterchunkloader.sponge.commands;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.PlayerData;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import java.util.Optional;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

public class CmdChunks implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public CmdChunks(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {

        if (!sender.hasPermission("betterchunkloader.chunks")) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdNoPermission));
            return CommandResult.empty();
        }
        String chunksChangeOperatorElement = commandContext.<String>getOne("change").get();
        String loaderTypeElement = commandContext.<String>getOne("type").get();
        String playerName = commandContext.<String>getOne("player").get();
        Integer changeValue = commandContext.<Integer>getOne("value").get();

        Optional<User> user = Utilities.getOfflinePlayer(playerName);
        if (!user.isPresent()) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().cmdPlayerNotExists));
            return CommandResult.empty();
        }
        PlayerData playerData = plugin.getDataStore().getPlayerData(user.get().getUniqueId());
        switch (chunksChangeOperatorElement) {
            case "add": {
                switch (loaderTypeElement) {
                    case "offline": {
                        if (playerData.getOfflineChunksAmount() + changeValue > plugin.getConfig().maxOfflineChunks) {
                            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdChunksAddFailure, new String[]{String.valueOf(changeValue), "offline", user.get().getName(), String.valueOf(plugin.getConfig().maxOfflineChunks)}));
                            return CommandResult.empty();
                        }
                        playerData.addOfflineChunksAmount(changeValue);
                        plugin.getDataStore().updatePlayerData(playerData);
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdChunksAddSuccess, new String[]{String.valueOf(changeValue), "offline", user.get().getName()}));
                        return CommandResult.success();
                    }
                    case "online": {
                        if (playerData.getOnlineChunksAmount() + changeValue > plugin.getConfig().maxOnlineChunks) {
                            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdChunksAddFailure, new String[]{String.valueOf(changeValue), "online", user.get().getName(), String.valueOf(plugin.getConfig().maxOnlineChunks)}));
                            return CommandResult.empty();
                        }
                        playerData.addOfflineChunksAmount(changeValue);
                        plugin.getDataStore().updatePlayerData(playerData);
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdChunksAddSuccess, new String[]{String.valueOf(changeValue), "online", user.get().getName()}));
                        return CommandResult.success();
                    }
                    default: {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdChunksUsage));
                        return CommandResult.empty();
                    }
                }
            }
            case "set": {
                if (changeValue < 0) {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdChunksSetFailure));
                    return CommandResult.empty();
                }
                switch (loaderTypeElement) {
                    case "offline": {
                        playerData.setOfflineChunksAmount(changeValue);
                        plugin.getDataStore().updatePlayerData(playerData);
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdChunksSetSuccess, new String[]{user.get().getName(), "offline", String.valueOf(changeValue)}));
                        return CommandResult.success();
                    }
                    case "online": {
                        playerData.setOnlineChunksAmount(changeValue);
                        plugin.getDataStore().updatePlayerData(playerData);
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdChunksSetSuccess, new String[]{user.get().getName(), "online", String.valueOf(changeValue)}));
                        return CommandResult.success();
                    }
                    default: {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdChunksUsage));
                        return CommandResult.empty();
                    }
                }
            }
            default: {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdChunksUsage));
                return CommandResult.empty();
            }
        }
    }
}
