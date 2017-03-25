package ca.shadownode.betterchunkloader.sponge;

import com.google.inject.Inject;
import ca.shadownode.betterchunkloader.sponge.commands.CommandManager;
import ca.shadownode.betterchunkloader.sponge.config.Configuration;
import ca.shadownode.betterchunkloader.sponge.dataStore.DataStoreManager;
import ca.shadownode.betterchunkloader.sponge.dataStore.IDataStore;
import ca.shadownode.betterchunkloader.sponge.events.PlayerListener;
import ca.shadownode.betterchunkloader.sponge.events.WorldListener;
import java.io.File;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.pagination.PaginationService;

@Plugin(id = "betterchunkloader")
public class BetterChunkLoader {

    private static BetterChunkLoader plugin;

    private Configuration config;
    private DataStoreManager dataStoreManager;
    private ChunkManager chunkManager;

    @Inject
    private Logger logger;

    @Inject
    private Game game;
    
    @Inject
    public PluginContainer plugincontainer;

    @Inject
    @ConfigDir(sharedRoot = false)
    public File configDir;

    @Inject
    public GuiceObjectMapperFactory factory;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        plugin = this;

        config = new Configuration(this);
        if (config.loadCore() && config.loadMessages()) {
            dataStoreManager = new DataStoreManager(this);

            if (dataStoreManager.load()) {

                getLogger().info("Loaded " + getDataStore().getChunkLoaders().size() + " chunkloaders.");
                getLogger().info("Loaded " + getDataStore().getPlayersData().size() + " players.");

                chunkManager = new ChunkManager(this);

                int count = 0;
                count = getDataStore().getChunkLoaders().stream().filter((chunkLoader) -> (chunkLoader.isLoadable())).map((chunkLoader) -> {
                    getChunkManager().loadChunkLoader(chunkLoader);
                    return chunkLoader;
                }).map((_item) -> 1).reduce(count, Integer::sum);
                getLogger().info("Activated " + count + " chunk loaders.");

                getLogger().info("Registering Listeners...");
                new PlayerListener(this).register();
                new WorldListener(this).register();
                new CommandManager(this).register();

                getLogger().info("Load complete.");
            } else {
                getLogger().error("Unable to load a datastore please check your Console/Config!");
            }
        }
    }

    @Listener
    public void onDisable(GameStoppedServerEvent event) {
        if (getDataStore() != null) {
            getDataStore().getChunkLoaders().stream().forEachOrdered((cl) -> {
                getChunkManager().unloadChunkLoader(cl);
            });
        }
    }

    public static BetterChunkLoader getInstance() {
        return plugin;
    }

    public DataStoreManager getDataStoreManager() {
        return dataStoreManager;
    }

    public IDataStore getDataStore() {
        return dataStoreManager.getDataStore();
    }

    public Configuration getConfig() {
        return config;
    }

    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    public Logger getLogger() {
        return logger;
    }

    public Game getGame() {
        return game;
    }

    public PaginationService getPaginationService() {
        return game.getServiceManager().provide(PaginationService.class).get();
    }
}
