package ca.shadownode.betterchunkloader.sponge.commands;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.PlayerData;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import java.util.HashMap;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

public class CmdBalance implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public CmdBalance(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext context) throws CommandException {
        Optional<String> playerName = context.getOne("player");
        if (playerName.isPresent()) {
            if (sender.hasPermission("betterchunkloader.commands.balance.others")) {
                if (chunksInfo(sender, playerName.get())) {
                    return CommandResult.success();
                } else {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.noPlayerExists));
                    return CommandResult.empty();
                }
            }else{
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.balance.noPermission));
                return CommandResult.empty();
            }
        } else {
            if (sender instanceof Player) {
                chunksInfo(sender, sender.getName());
                return CommandResult.success();
            }
        }
        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.balance.failure));
        return CommandResult.empty();
    }

    private boolean chunksInfo(CommandSource sender, String playerName) {
        Optional<PlayerData> playerData = plugin.getDataStore().getPlayerData(playerName);
        HashMap<String, String> args = new HashMap();
        if (playerData.isPresent()) {
            args.put("username", playerData.get().getName());
            args.put("uuid", playerData.get().getUnqiueId().toString());
            args.put("online", String.valueOf(playerData.get().getOnlineChunksAmount()));
            args.put("alwayson", String.valueOf(playerData.get().getAlwaysOnChunksAmount()));
            plugin.getPaginationService().builder()
                    .contents(Utilities.parseMessageList(plugin.getConfig().getMessages().commands.balance.success.items, args))
                    .title(Utilities.parseMessage(plugin.getConfig().getMessages().commands.balance.success.title))
                    .padding(Utilities.parseMessage(plugin.getConfig().getMessages().commands.balance.success.padding))
                    .sendTo(sender);
            return true;
        }
        return false;
    }
}
