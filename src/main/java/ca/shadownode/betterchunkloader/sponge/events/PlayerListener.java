package ca.shadownode.betterchunkloader.sponge.events;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class PlayerListener {

    private final BetterChunkLoader plugin;
    
    public PlayerListener(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }
    
    public void register() {
        Sponge.getEventManager().registerListeners(plugin, this);
    }
    
    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        /*When a player joins load all their online-only chunk loaders.*/
    }    
    
    @Listener
    public void onPlayerLeave(ClientConnectionEvent.Disconnect event) {
        /*When a player leaves unload all their online-only chunk loaders.*/
    }    
    
}
