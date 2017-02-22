package ca.shadownode.betterchunkloader.sponge.commands;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.PlayerData;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;
import org.spongepowered.api.entity.living.player.User;

public class CmdBalance implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public CmdBalance(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
        if (!sender.hasPermission("betterchunkloader.balance")) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().cmdNoPermission));
            return CommandResult.empty();
        }

        Optional<String> playerName = commandContext.getOne("player");
        if (playerName.isPresent()) {
            Optional<User> user = Utilities.getOfflinePlayer(playerName.get());
            if (user.isPresent()) {
                chunksInfo(sender, user.get());
                return CommandResult.success();
            } else {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdPlayerNotExists));
            }
        } else {
            if (sender instanceof Player) {
                chunksInfo(sender, (Player)sender);
                return CommandResult.success();
            }
        }

        return CommandResult.empty();
    }

    private void chunksInfo(CommandSource sender, User user) {
        PlayerData playerData = plugin.getDataStore().getPlayerData(user.getUniqueId());
        int online = playerData.getOnlineChunksAmount();
        int offline = playerData.getOfflineChunksAmount();

        plugin.getConfig().cmdBalanceSuccess.forEach((message) -> {
            sender.sendMessage(Utilities.parseMessage(message, new String[]{user.getName(), String.valueOf(online), String.valueOf(offline)}));
        });
    }
}
