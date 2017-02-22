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
import java.util.Optional;
import org.spongepowered.api.entity.living.player.User;

public class CmdDelete implements CommandExecutor {
    
    private final BetterChunkLoader plugin;
    
    public CmdDelete(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {

        if (!sender.hasPermission("betterchunkloader.delete")) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdNoPermission));
            return CommandResult.empty();
        }

        if (!commandContext.getOne("player").isPresent()) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdDeleteUsage));
            return CommandResult.empty();
        }
        String playerName = (String) commandContext.getOne("player").get();
        Optional<User> user = Utilities.getOfflinePlayer(playerName);
        if (user.isPresent()) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdPlayerNotExists));
            return CommandResult.empty();
        }

        List<ChunkLoader> clList = plugin.getDataStore().getChunkLoaders(user.get().getUniqueId());
        if (clList == null) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdDeleteFailure));
            return CommandResult.empty();
        }
        plugin.getDataStore().removeChunkLoaders(user.get().getUniqueId());
        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().cmdDeleteSuccess));
        plugin.getLogger().info(sender.getName() + " deleted all chunk loaders placed by " + playerName);
        return CommandResult.success();
    }
}
