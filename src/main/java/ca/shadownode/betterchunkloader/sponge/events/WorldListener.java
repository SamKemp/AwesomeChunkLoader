package ca.shadownode.betterchunkloader.sponge.events;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import java.util.HashMap;
import java.util.Optional;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;

public class WorldListener {

    private final BetterChunkLoader plugin;

    public WorldListener(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    @Listener
    public void onWorldLoad(LoadWorldEvent event) {
        plugin.getDataStore().getChunkLoaders(event.getTargetWorld()).stream().filter((cl) -> (cl.isLoadable())).forEachOrdered((cl) -> {
            plugin.getChunkManager().loadChunkLoader(cl);
        });
    }

    @Listener
    public void onWorldUnLoad(UnloadWorldEvent event) {
        plugin.getDataStore().getChunkLoaders(event.getTargetWorld()).stream().filter((cl) -> (cl.isLoadable())).forEachOrdered((cl) -> {
            plugin.getChunkManager().unloadChunkLoader(cl);
        });
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        BlockSnapshot block = event.getTransactions().get(0).getOriginal();

        if (block == null) {
            return;
        }

        if (!block.getState().getType().equals(ChunkLoader.ONLINE_TYPE) && !block.getState().getType().equals(ChunkLoader.ALWAYSON_TYPE)) {
            return;
        }

        plugin.getDataStore().getChunkLoaderAt(block.getLocation().get()).ifPresent((chunkLoader) -> {

            Player player = event.getCause().last(Player.class).get();
            plugin.getDataStore().getPlayerData(chunkLoader.getOwner()).ifPresent((playerData) -> {

                HashMap<String, String> args = new HashMap<>();
                args.put("player", player.getName());
                args.put("playerUUID", player.getUniqueId().toString());
                args.put("ownerName", playerData.getName());
                args.put("owner", playerData.getUnqiueId().toString());
                args.put("type", chunkLoader.isAlwaysOn() ? "Always On" : "Online Only");
                args.put("location", Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()));
                args.put("chunks", String.valueOf(chunkLoader.getChunks()));

                if (plugin.getDataStore().removeChunkLoader(chunkLoader)) {
                    plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                    if (chunkLoader.isAlwaysOn()) {
                        playerData.addAlwaysOnChunksAmount(chunkLoader.getChunks());
                    } else {
                        playerData.addOnlineChunksAmount(chunkLoader.getChunks());
                    }
                    plugin.getDataStore().updatePlayerData(playerData);
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.removeSuccess, args));

                    Optional<Player> owner = Sponge.getServer().getPlayer(chunkLoader.getOwner());
                    if (owner.isPresent() && player != owner.get()) {
                        player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.ownerNotify, args));
                    }
                     plugin.getLogger().info(player.getName() + " broke " + owner.get().getName() + "'s chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()));

                }else{
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.removeFailure, args));
                }
            });
        });
    }

}
