package ca.shadownode.betterchunkloader.sponge.data;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class LocationSerializer {

    private final BetterChunkLoader plugin;
    
    public LocationSerializer(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    public Optional<String> serializeLocation(Location<World> location) {
        try {
            StringWriter sink = new StringWriter();
            GsonConfigurationLoader loader = GsonConfigurationLoader.builder().setSink(() -> new BufferedWriter(sink)).build();
            ConfigurationNode node = loader.createEmptyNode();
            node.setValue(TypeToken.of(Location.class), location);
            loader.save(node);
            return Optional.of(sink.toString());
        } catch (IOException | ObjectMappingException ex) {
            plugin.getLogger().error("Error serializing Location", ex);
            return Optional.empty();
        }
    }

    public Optional<Location<World>> deserializeLocation(String locationStr) {
        try {
            StringReader source = new StringReader(locationStr);
            GsonConfigurationLoader loader = GsonConfigurationLoader.builder().setSource(() -> new BufferedReader(source)).build();
            ConfigurationNode node = loader.load();
            return Optional.of(node.getValue(TypeToken.of(Location.class)));
        } catch (IOException | ObjectMappingException ex) {
            plugin.getLogger().error("Error deserializing Location", ex);
            return Optional.empty();
        }
    }
    
    public Optional<String> serializeVector(Vector3i vector) {
        try {
            StringWriter sink = new StringWriter();
            GsonConfigurationLoader loader = GsonConfigurationLoader.builder().setSink(() -> new BufferedWriter(sink)).build();
            ConfigurationNode node = loader.createEmptyNode();
            node.setValue(TypeToken.of(Vector3i.class), vector);
            loader.save(node);
            return Optional.of(sink.toString());
        } catch (IOException | ObjectMappingException ex) {
            plugin.getLogger().error("Error serializing Vector3i", ex);
            return Optional.empty();
        }
    }

    public Optional<Vector3i> deserializeVector(String vectorStr) {
        try {
            StringReader source = new StringReader(vectorStr);
            GsonConfigurationLoader loader = GsonConfigurationLoader.builder().setSource(() -> new BufferedReader(source)).build();
            ConfigurationNode node = loader.load();
            return Optional.of(node.getValue(TypeToken.of(Vector3i.class)));
        } catch (IOException | ObjectMappingException ex) {
            plugin.getLogger().error("Error deserializing Location", ex);
            return Optional.empty();
        }
    }
}
