package ca.shadownode.betterchunkloader.sponge.events;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.world.World;

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
        /*Load all always-on chunk loaders for the loading world and add them.*/
    }
    
    @Listener
    public void onWorldUnLoad(UnloadWorldEvent event) {
        /*Remove all always-on chunk loaders for the unloading world and their tickets.*/
        if(!event.isCancelled() && event.getTargetWorld() != null) {
            World world = event.getTargetWorld();
            /*Remove tickets for this world.*/
        }
    }
    
}
