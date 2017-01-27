package ca.shadownode.betterchunkloader.sponge;

import ca.shadownode.betterchunkloader.sponge.commands.TestCommand;
import ca.shadownode.betterchunkloader.sponge.events.WorldListener;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.user.UserStorageService;

@Plugin(
        id = "betterchunkloader",
        name = "BetterChunkLoader",
        version = "1.0",
        description = "No idea on what to put here yet.",
        authors = {"ShadowKitten"}
)
public class BetterChunkLoader {

    private static BetterChunkLoader plugin;
    
    @Inject
    private Logger logger;
    
    @Inject
    @DefaultConfig(sharedRoot = false)
    public File defaultConfig;

    private UserStorageService userStorage;
    private ChunkManager chunkManager;
    
    private BetterChunkLoaderConfig config;
    
    @Listener(order = Order.POST)
    public void serverStarted(GameStartedServerEvent event) throws IOException {
        plugin = this;
        userStorage = Sponge.getServiceManager().provide(UserStorageService.class).get();
        config = new BetterChunkLoaderConfig(this);
        new WorldListener(this).register();
        new TestCommand(this).register();
    }
    
    @Listener(order = Order.POST)
    public void serverStopping(GameStoppingServerEvent event) throws IOException {
    }   
    
    public static BetterChunkLoader getInstance() {
        return plugin;
    }
    
    public BetterChunkLoaderConfig getConfig() {
        return config;
    }
    
    public UserStorageService getUserStorage() {
        return userStorage;
    }
    
    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    public Logger getLogger() {
        return this.logger;
    }
}
