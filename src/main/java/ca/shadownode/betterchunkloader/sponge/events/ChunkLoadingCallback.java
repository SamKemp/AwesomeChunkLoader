package ca.shadownode.betterchunkloader.sponge.events;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import java.util.List;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class ChunkLoadingCallback implements LoadingCallback {

    private final BetterChunkLoader plugin;

    public ChunkLoadingCallback(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public void ticketsLoaded(List<Ticket> tickets, World world) {
        // discard all tickets
    }

}
