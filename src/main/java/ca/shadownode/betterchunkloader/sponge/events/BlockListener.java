package ca.shadownode.betterchunkloader.sponge.events;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;

public class BlockListener {

    private final BetterChunkLoader plugin;
    
    public BlockListener(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }
    
    public void register() {
        Sponge.getEventManager().registerListeners(plugin, this);
    }
    
    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event) {
        /*When a user places a block check if it's a chunk loader if it is open the menu.*/
    }
    
    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        /*When a user breaks the block remove the chunk loader*/
    }
    
    @Listener
    public void onBlockInteract(InteractBlockEvent.Primary event) {
        /*Make sure user is not a fake player and they own the chunk loader*/
        /*When a user left clicks the block and they are holding a blaze rod break it*/
    }
    
    @Listener
    public void onBlockInteract(InteractBlockEvent.Secondary event) {
        /*Make sure user is not a fake player and they own the chunk loader*/
        /*When a user right clicks the block and they are holding a blaze rod open menu*/
    }
    
}
