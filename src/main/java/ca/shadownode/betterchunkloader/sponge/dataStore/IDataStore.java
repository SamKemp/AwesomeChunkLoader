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
     * Get chunk loaders owned by someone with the specified UUID
     *
     * @param ownerUUID
     * @return
     */
    public abstract List<ChunkLoader> getChunkLoaders(UUID ownerUUID);

    /**
     * Get chunk loaders at specified chunk
     *
     * @param worldUUID
     * @param chunk
     * @return
     */
    public abstract List<ChunkLoader> getChunkLoadersAt(UUID worldUUID, Vector3i chunk);

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
     * @param ownerUUID
     */
    public abstract void removeChunkLoaders(UUID ownerUUID);

    /**
     * Change chunk loader range
     *
     * @param chunkLoader
     * @param range
     */
    public abstract void changeChunkLoaderRadius(ChunkLoader chunkLoader, Integer range);

    /**
     * Get the amount of free always on chunks that this player can still load
     * until he reaches his chunks limit
     *
     * @param playerUUID
     * @return
     */
    /**public abstract int getOfflineChunksAmount(UUID playerUUID);**/

    /**
     * Get the amount of free online only chunks that this player can still load
     * until he reaches his chunks limit
     *
     * @param playerUUID
     * @return
     */
    /**public abstract int getOnlineChunksAmount(UUID playerUUID);**/

    /**
     * Get the player data
     *
     * @param playerUUID
     * @return
     */
    public abstract PlayerData getPlayerData(UUID playerUUID);

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
}
