package ca.shadownode.betterchunkloader.sponge.menu;

import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;

public class ChunkLoaderMenuProp extends AbstractInventoryProperty<String, ChunkLoader> {

    public static final String PROPERTY_NAME = "chunkloadermenuprop";

    public ChunkLoaderMenuProp(ChunkLoader chunkLoader) {
        super(chunkLoader);
    }

    public static ChunkLoaderMenuProp of(ChunkLoader chunkLoader) {
        return new ChunkLoaderMenuProp(chunkLoader);
    }

    @Override
    public int compareTo(Property<?, ?> o) {
        return 0;
    }
}
