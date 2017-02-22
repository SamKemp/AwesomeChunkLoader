package ca.shadownode.betterchunkloader.sponge.events;


import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.api.world.ChunkTicketManager.Callback;
import org.spongepowered.api.world.World;

public class ChunkLoadingCallback implements Callback {

    private final BetterChunkLoader plugin;
    
    public ChunkLoadingCallback(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoaded(ImmutableList<ChunkTicketManager.LoadingTicket> il, World world) {
        
    }

}