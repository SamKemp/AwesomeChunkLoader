package ca.shadownode.betterchunkloader.sponge.data;

import org.spongepowered.api.data.Property;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;

public class ChunkLoaderMenu extends AbstractInventoryProperty<String, ChunkLoader> {

    public static final String PROPERTY_NAME = "chunkloadermenu";

    public ChunkLoaderMenu(ChunkLoader chunkLoader) {
        super(chunkLoader);
    }

    public static ChunkLoaderMenu of(ChunkLoader chunkLoader) {
        return new ChunkLoaderMenu(chunkLoader);
    }

    @Override
    public int compareTo(Property<?, ?> o) {
        return 0;
    }
}
