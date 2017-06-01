package ca.shadownode.betterchunkloader.sponge.commands;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import java.util.HashMap;
import java.util.Optional;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class CmdReload implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public CmdReload(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
        Optional<String> typeElement = commandContext.<String>getOne("type");
        String currentType;
        Boolean success;
        if (typeElement.isPresent()) {
            switch (typeElement.get()) {
                case "core": {
                    currentType = "core";
                    success = plugin.getConfig().loadCore();
                    break;
                }
                case "messages": {
                    currentType = "messages";
                    success = plugin.getConfig().loadMessages();
                    break;
                }
                case "datastore": {
                    currentType = "datastore";
                    plugin.getDataStoreManager().clearDataStores();
                    success = plugin.getDataStoreManager().load();
                    break;
                }
                default: {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.reload.usage));
                    return CommandResult.success();
                }
            }
        } else {
            currentType = "all";
            success = plugin.getConfig().loadCore() && plugin.getConfig().loadMessages() && plugin.getDataStoreManager().load();
        }
        HashMap<String, String> args = new HashMap<>();
        args.put("type", currentType);
        if (success) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.reload.success, args));
            return CommandResult.success();
        } else {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.reload.failure, args));
            return CommandResult.empty();
        }
    }
}
