package ca.shadownode.betterchunkloader.sponge.data;

import com.flowpowered.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChunkLoader {

    private UUID owner;
    private UUID world;
    private Vector3i location;
    private List<Vector3i> chunks = new ArrayList<>();
    private Integer range;

    public ChunkLoader(UUID owner, UUID world, Vector3i location, List<Vector3i> chunks, Integer range) {
        this.owner = owner;
        this.world = world;
        this.location = location;
        this.chunks = chunks;
        this.range = range;
    }
    
    /**
     * Returns the owner of this chunk loader.
     * @return UUID of owner.
     */
    public UUID getOwner() {
        return owner;
    }
    
    /**
     * Returns the uuid of the world this chunk loader is in.
     * @return UUID of world.
     */
    public UUID getWorld() {
        return world;
    }
      
    /**
     * Returns the location of this chunk loader.
     * @return Vector3i location of chunkloader.
     */
    public Vector3i getLocation() {
        return location;
    }
    
    /**
     * This method gets a list of all chunks currently controlled by this chunkloader.
     * @return List of all chunk locations as Vector3i.
     */
    public List<Vector3i> getChunks() {
        return chunks;
    }
    
    /**
     * This method returns the range of this chunkloader.
     * @return Integer range of chunkloader.
     */
    public Integer getRange() {
        return range;
    }
    
    /**
     * Sets the owner of this chunk loader.
     * @param owner UUID of owner.
     */
    public void setOwner(UUID owner) {
        this.owner = owner;
    }
    
    /**
     * Sets the uuid of the world this chunk loader is in.
     * @param world UUID of world.
     */
    public void setWorld(UUID world) {
        this.world = world;
    }
   
    /**
     * Sets the stored location for this chunk loader.
     * @param location Vector3i chunkloader location.
     */
    public void setLocation(Vector3i location) {
        this.location = location;
    }
    
    /**
     * Sets the stored chunks controlled by this chunk loader.
     * @param chunks List of chunk locations as Vector3i.
     */
    public void setChunks(List<Vector3i> chunks) {
        this.chunks = chunks;
    }
    
    /**
     *  Sets the range of this chunk loader.
     * @param range Integer range of chunkloader 
     */
    public void setRange(Integer range) {
        this.range = range;
    }
    
}
