package ca.shadownode.betterchunkloader.sponge.data;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    
    private UUID playerUUID;
    private Date lastActive;
    private Map<UUID, List<ChunkLoader>> chunkLoaders;
    private Integer allowedOfflineChunks;
    private Integer allowedOnlineChunks;

    public PlayerData(UUID playerUUID, Map<UUID, List<ChunkLoader>> chunkLoaders, Date lastActive) {
        this.playerUUID = playerUUID;
        this.lastActive = lastActive;
        this.chunkLoaders = chunkLoaders;
        /*Default max always on chunk count.*/
        this.allowedOfflineChunks = 10;
        /*Default max online only chunk count.*/
        this.allowedOnlineChunks = 10;
    }

    public PlayerData(UUID playerUUID, Map<UUID, List<ChunkLoader>> chunkLoaders, Date lastActive, int allowedOfflineChunks, int allowedOnlineChunks) {
        this.playerUUID = playerUUID;
        this.lastActive = lastActive;
        this.chunkLoaders = chunkLoaders;
        this.allowedOfflineChunks = allowedOfflineChunks;
        this.allowedOnlineChunks = allowedOnlineChunks;
    }

    /**
     * Returns the players UUID.
     * 
     * @return UUID of player.
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }
   
    /**
     * Returns the last time this player was active on the server.
     * 
     * @return Date last active.
     */
    public Date getLastActive() {
        return this.lastActive;
    }    
    
    /**
     * Returns a map of all chunk loaders assigned to this player by world UUID.
     *  
     * @return Map containing UUID and List of ChunkLoader(s).
     */
    public Map<UUID, List<ChunkLoader>> getChunkLoaders() {
        return chunkLoaders;
    }
    
    /**
     * Returns the total amount of chunks a player can load while offline.
     * 
     * @return Integer of allowed offline chunks.
     */
    public Integer getAllowedOfflineChunks() {
        return allowedOfflineChunks;
    }
    
    /**
     * Returns the total amount of chunks a player can load while online.
     * 
     * @return Integer of allowed online chunks.
     */
    public Integer getAllowedOnlineChunks() {
        return allowedOnlineChunks;
    }

    
    /**
     * Sets the players UUID.
     * 
     * @param playerUUID UUID of the players.
     */
    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    /**
     * Set the last time this player was active.
     * 
     * @param lastActive Date the player was last active.
     */
    public void setLastActive(Date lastActive) {
        this.lastActive = lastActive;
    }
    
    /**
     * Sets a map of all chunk loaders owned by world UUID.
     * 
     * @param chunkLoaders Map of UUID and List of ChunkLoader(s).
     */
    public void setChunkLoaders(Map<UUID, List<ChunkLoader>> chunkLoaders) {
        this.chunkLoaders = chunkLoaders;
    }

    /**
     * Set the total amount of always on chunks that a player can load.
     *
     * @param allowedOfflineChunks Integer of the amount of allowed offline chunks.
     */
    public void setAllowedOfflineChunks(int allowedOfflineChunks) {
        this.allowedOfflineChunks = allowedOfflineChunks;
    }

    /**
     * Set the total amount of online only chunks that this player can load.
     *
     * @param allowedOnlineChunks Integer of the amount of allowed online chunks.
     */
    public void setAllowedOnlineChunks(int allowedOnlineChunks) {
        this.allowedOnlineChunks = allowedOnlineChunks;
    }
}
