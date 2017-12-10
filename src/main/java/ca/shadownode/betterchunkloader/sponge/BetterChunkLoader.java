package ca.shadownode.betterchunkloader.sponge;

import com.google.inject.Inject;
import ca.shadownode.betterchunkloader.sponge.commands.CommandManager;
import ca.shadownode.betterchunkloader.sponge.config.Configuration;
import ca.shadownode.betterchunkloader.sponge.datastore.DataStoreManager;
import ca.shadownode.betterchunkloader.sponge.datastore.IDataStore;
import ca.shadownode.betterchunkloader.sponge.events.PlayerListener;
import ca.shadownode.betterchunkloader.sponge.events.WorldListener;
import ca.shadownode.betterchunkloader.sponge.menu.MenuListener;
import java.io.File;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
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
    public PluginContainer pluginContainer;

    @Inject
    @ConfigDir(sharedRoot = false)
    public File configDir;

    @Inject
    public GuiceObjectMapperFactory factory;

    @Listener
    public void onServerAboutStart(GameAboutToStartServerEvent event) {
        plugin = this;

        config = new Configuration(this);

        if (config.loadCore() && config.loadMessages()) {
            dataStoreManager = new DataStoreManager(this);

            if (dataStoreManager.load()) {
                getLogger().info("Connecting to datastore.");

                getLogger().info("Loaded " + getDataStore().getChunkLoaders().size() + " chunkloaders.");
                getLogger().info("Loaded " + getDataStore().getPlayersData().size() + " players.");

                getLogger().info("Registering Listeners...");

                chunkManager = new ChunkManager(this);

                new PlayerListener(this).register();
                new WorldListener(this).register();
                new MenuListener(this).register();
                new CommandManager(this).register();

                getLogger().info("Load complete.");
            } else {
                getLogger().error("Unable to load a datastore please check your Console/Config!");
            }
        }
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        int count = 0;
        count = getDataStore().getChunkLoaders().stream().filter((chunkLoader) -> (chunkLoader.isLoadable())).map((chunkLoader) -> {
            getChunkManager().loadChunkLoader(chunkLoader);
            return chunkLoader;
        }).map((_item) -> 1).reduce(count, Integer::sum);
        getLogger().info("Activated " + count + " chunk loaders.");
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
