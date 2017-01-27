package ca.shadownode.betterchunkloader.sponge;

import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public final class BetterChunkLoaderConfig {

    private final BetterChunkLoader plugin;   
    
    private ConfigurationLoader loader;
    private ConfigurationNode config;

    public boolean bool;
    public List<String> array;

    public BetterChunkLoaderConfig(BetterChunkLoader plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        try {
            loader = HoconConfigurationLoader.builder().setFile(plugin.defaultConfig).build();
            saveDefaultConfig();
            config = loader.load();
            bool = config.getNode("Location", "Boolean").getBoolean();
            array = config.getNode("Location, Array").getList(TypeToken.of(String.class));
        } catch (IOException | ObjectMappingException ex) {
            plugin.getLogger().error("The default configuration could not be loaded!", ex);
        }
    }
    
    public void saveDefaultConfig() {
        if (!plugin.defaultConfig.exists()) {
            try {
                plugin.defaultConfig.createNewFile();
                config = loader.load();
                config.getNode("Location", "Boolean").setValue(false);
                config.getNode("Location", "Array").setValue(new TypeToken<List<String>>() {
                }, Arrays.asList(
                        "test",
                        "test"
                ));
                loader.save(config);
            } catch (IOException | ObjectMappingException ex) {
                plugin.getLogger().error("The default configuration could not be created!", ex);
            }
        }
    }

}