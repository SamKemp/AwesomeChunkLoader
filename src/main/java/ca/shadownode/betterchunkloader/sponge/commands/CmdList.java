package ca.shadownode.betterchunkloader.sponge.commands;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.PlayerData;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

public class CmdList implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public CmdList(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
        Optional<String> loaderType = commandContext.<String>getOne("type");
        Optional<String> playerName = commandContext.<String>getOne("player");

        List<ChunkLoader> chunkLoaders = new ArrayList<>();

        if (loaderType.isPresent()) {
            boolean alwaysOn = !loaderType.get().equals("online");
            if (playerName.isPresent()) {
                if (sender.hasPermission("betterchunkloader.commands.list.others")) {
                    Optional<UUID> playerUUID = Utilities.getPlayerUUID(playerName.get());
                    if (playerUUID.isPresent()) {
                        chunkLoaders = getChunkLoadersByType(playerUUID.get(), alwaysOn);
                    } else {
                        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.list.noPlayer));
                        return CommandResult.empty();
                    }
                } else {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.list.noPermission));
                }
            } else {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    chunkLoaders = getChunkLoadersByType(player.getUniqueId(), alwaysOn);
                } else {
                    chunkLoaders = plugin.getDataStore().getChunkLoadersByType(alwaysOn);
                }
            }
            List<Text> readableCLs = new ArrayList<>();
            for(ChunkLoader chunkLoader : chunkLoaders) {
                readableCLs.add(getReadableChunkLoader(chunkLoader));
            }
            plugin.getPaginationService().builder()
                    .contents(readableCLs)
                    .title(Utilities.parseMessage(plugin.getConfig().getMessages().commands.list.success.title))
                    .header(Utilities.parseMessage(plugin.getConfig().getMessages().commands.list.success.header))
                    .padding(Utilities.parseMessage(plugin.getConfig().getMessages().commands.list.success.padding))
                    .sendTo(sender);
            return CommandResult.success();
        }
        sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().commands.list.noLoaderType));
        return CommandResult.empty();
    }

    public Text getReadableChunkLoader(ChunkLoader chunkLoader) {
        HashMap<String, String> args = new HashMap<>();
        args.put("uuid", chunkLoader.getUniqueId().toString());
        args.put("radius", String.valueOf(chunkLoader.getRadius()));
        args.put("type", chunkLoader.isAlwaysOn() ? "Always On" : "Online Only");
        args.put("chunks", String.valueOf(chunkLoader.getChunks()));
        args.put("location", Utilities.getReadableLocation(chunkLoader.getLocation()));
        Optional<World> world = plugin.getGame().getServer().getWorld(chunkLoader.getWorld());
        if (world.isPresent()) {
            args.put("world", world.get().getName());
        }
        Optional<PlayerData> playerData = plugin.getDataStore().getPlayerData(chunkLoader.getOwner());
        if (playerData.isPresent()) {
            args.put("owner", playerData.get().getName());
            args.put("uuid", playerData.get().getUnqiueId().toString());
        }
        return Utilities.parseMessage(plugin.getConfig().getMessages().commands.list.success.format, args);
    }

    public List<ChunkLoader> getChunkLoadersByType(UUID owner, Boolean type) {
        List<ChunkLoader> chunkLoaders = new ArrayList<>();
        plugin.getDataStore().getChunkLoadersByOwner(owner).stream().filter((cl) -> (cl.isAlwaysOn().equals(type))).forEachOrdered((cl) -> {
            chunkLoaders.add(cl);
        });
        return chunkLoaders;
    }
}
