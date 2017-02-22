package ca.shadownode.betterchunkloader.sponge.events;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.PlayerData;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import java.util.Optional;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
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
        plugin.getDataStore().getChunkLoaders(event.getTargetWorld().getUniqueId()).stream().filter((cl) -> (cl.isLoadable())).forEachOrdered((cl) -> {
             plugin.getChunkManager().loadChunkLoader(cl);
        });
    }

    @Listener
    public void onWorldUnLoad(UnloadWorldEvent event) {
        plugin.getDataStore().getChunkLoaders(event.getTargetWorld().getUniqueId()).stream().filter((cl) -> (cl.isLoadable())).forEachOrdered((cl) -> {
            plugin.getChunkManager().unloadChunkLoader(cl);
        });
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        BlockSnapshot block = event.getTransactions().get(0).getOriginal();
        if (block == null || (!block.getState().getType().equals(BlockTypes.DIAMOND_BLOCK) && !block.getState().getType().equals(BlockTypes.IRON_BLOCK))) {
            return;
        }

        Optional<ChunkLoader> chunkLoader = plugin.getDataStore().getChunkLoaderAt(block.getLocation().get());
        if (chunkLoader.isPresent()) {

            Player player = event.getCause().last(Player.class).get();
            PlayerData playerData = plugin.getDataStore().getPlayerData(chunkLoader.get().getOwner());
            if(chunkLoader.get().isAlwaysOn()) {
                playerData.addOfflineChunksAmount(chunkLoader.get().getChunks());
            }else{
                playerData.addOnlineChunksAmount(chunkLoader.get().getChunks());
            }
            plugin.getDataStore().updatePlayerData(playerData);
            plugin.getDataStore().removeChunkLoader(chunkLoader.get());
            player.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().clBreakMessage));

            Optional<Player> owner = Sponge.getServer().getPlayer(chunkLoader.get().getOwner());
            if (owner.isPresent() && player != owner.get()) {
                owner.get().sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().clBreakOwnerNotify, new String[]{
                    Utilities.getReadableLocation(chunkLoader.get().getLocation()),
                    player.getName()
                }));
            }
            plugin.getLogger().info(player.getName() + " broke " + owner.get().getName() + "'s chunk loader at " + Utilities.getReadableLocation(chunkLoader.get().getLocation()));
        }
    }

}
