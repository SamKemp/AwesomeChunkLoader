package ca.shadownode.betterchunkloader.sponge.dataStore;

import ca.shadownode.betterchunkloader.sponge.data.PlayerData;
import java.util.List;
import java.util.UUID;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import com.flowpowered.math.vector.Vector3i;
import java.util.Optional;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * Interface for BetterChunkLoader's data store
 */
public interface IDataStore {

    /**
     * Returns the data store name
     *
     * @return
     */
    public abstract String getName();

    /**
     * Loads data from the datastore. This is called while BCL is loading
     *
     * @return
     */
    public abstract boolean load();

    /**
     * Get chunk loaders
     *
     * @return
     */
    public abstract List<ChunkLoader> getChunkLoaders();

    /**
     * Get chunk loaders for dimension
     *
     * @param world
     * @return
     */
    public abstract List<ChunkLoader> getChunkLoaders(World world);
    
    /**
     * Get chunk loaders by type.
     *
     * @param isAlwaysOn
     * @return
     */
    public abstract List<ChunkLoader> getChunkLoadersByType(Boolean isAlwaysOn);

    
    /**
     * Get chunk loaders owned by someone with the specified UUID
     *
     * @param ownerUUID
     * @return
     */
    public abstract List<ChunkLoader> getChunkLoadersByOwner(UUID ownerUUID);


    /**
     * Get chunk loaders at specified chunk of a world
     *
     * @param world
     * @param chunk
     * @return
     */
    public abstract List<ChunkLoader> getChunkLoadersAt(World world, Vector3i chunk);

    /**
     * Get chunk loader at specified location
     *
     * @param blockLocation
     * @return
     */
    public abstract Optional<ChunkLoader> getChunkLoaderAt(Location<World> blockLocation);

    /**
     * Add a new chunk loader
     *
     * @param chunkLoader
     */
    public abstract void addChunkLoader(ChunkLoader chunkLoader);

    /**
     * Remove chunk loader
     *
     * @param chunkLoader
     */
    public abstract void removeChunkLoader(ChunkLoader chunkLoader);


    /**
     * Remove chunk loaders owned by someone with the specified UUID
     *
     * @param owner
     */
    public abstract void removeAllChunkLoaders(UUID owner);    
    
    /**
     * Remove chunk loaders in a specific world.
     *
     * @param world
     */
    public abstract void removeAllChunkLoaders(World world);

    /**
     * Set ChunkLoader radius.
     *
     * @param chunkLoader
     * @param radius
     */
    public abstract void setChunkLoaderRadius(ChunkLoader chunkLoader, Integer radius);

    /**
     * Get or create new player data by uuid.
     *
     * @param playerUUID
     * @return
     */
    public abstract Optional<PlayerData> getOrCreatePlayerData(UUID playerUUID);
    
    /**
     * Get or create new player data by name.
     *
     * @param playerName
     * @return
     */
    public abstract Optional<PlayerData> getOrCreatePlayerData(String playerName);
    
    /**
     * Get player data by uuid.
     *
     * @param playerUUID
     * @return
     */
    public abstract Optional<PlayerData> getPlayerData(UUID playerUUID);
    
    /**
     * Get player data by name.
     *
     * @param playerName
     * @return
     */
    public abstract Optional<PlayerData> getPlayerData(String playerName);

    /**
     * refresh player data on login.
     * @param uuid
     */
    public abstract void refreshPlayer(UUID uuid);

    /**
     * Updates the player data in all active data stores.
     *
     * @param playerData
     */
    public abstract void updatePlayerData(PlayerData playerData);

    /**
     * Get players data
     *
     * @return
     */
    public abstract List<PlayerData> getPlayersData();
    
    /**
     * Check if this user has player data.
     *
     * @param playerUUID
     * @return
     */
    public abstract Boolean playerDataExists(UUID playerUUID);
}
