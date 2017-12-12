package ca.shadownode.betterchunkloader.sponge.data;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.flowpowered.math.vector.Vector3i;
import java.util.Optional;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public final class ChunkLoader {

    private UUID uuid;
    private UUID world;
    private UUID owner;

    private Vector3i location;
    private Vector3i chunk;
    private Integer radius;

    private Long creation;
    private Boolean isAlwaysOn;

    public static final BlockType ONLINE_TYPE = Sponge.getRegistry().getType(BlockType.class, BetterChunkLoader.getInstance().getConfig().getCore().chunkLoader.online.blockType).orElse(BlockTypes.IRON_BLOCK);
    public static final BlockType ALWAYSON_TYPE = Sponge.getRegistry().getType(BlockType.class, BetterChunkLoader.getInstance().getConfig().getCore().chunkLoader.alwaysOn.blockType).orElse(BlockTypes.DIAMOND_BLOCK);

    public ChunkLoader(UUID uuid, UUID world, UUID owner, Vector3i location, Vector3i chunk, Integer radius, Long creation, Boolean isAlwaysOn) {
        this.uuid = uuid;
        this.world = world;
        this.owner = owner;
        this.location = location;
        this.chunk = chunk;
        this.radius = radius;
        this.creation = creation;
        this.isAlwaysOn = isAlwaysOn;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    @XmlAttribute(name = "uuid")
    public void setUniqueId(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getWorld() {
        return world;
    }

    @XmlAttribute(name = "world")
    public void setWorld(UUID world) {
        this.world = world;
    }

    public UUID getOwner() {
        return owner;
    }

    @XmlAttribute(name = "owner")
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Vector3i getLocation() {
        return location;
    }

    @XmlAttribute(name = "location")
    public void setLocation(Vector3i location) {
        this.location = location;
    }

    public Vector3i getChunk() {
        return chunk;
    }

    public void setChunk(Vector3i chunk) {
        this.chunk = chunk;
    }

    public Integer getRadius() {
        return radius;
    }

    @XmlAttribute(name = "r")
    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    public Long getCreation() {
        return creation;
    }

    @XmlAttribute(name = "creation")
    public void setCreation(Long creation) {
        this.creation = creation;
    }

    public Boolean isAlwaysOn() {
        return isAlwaysOn;
    }

    @XmlAttribute(name = "alwaysOn")
    public void setAlwaysOn(Boolean isAlwaysOn) {
        this.isAlwaysOn = isAlwaysOn;
    }

    public Boolean isExpired() {
        Optional<PlayerData> playerData = BetterChunkLoader.getInstance().getDataStore().getPlayerData(owner);
        if (playerData.isPresent()) {
            if (isAlwaysOn()) {
                return System.currentTimeMillis() - playerData.get().getLastOnline() > BetterChunkLoader.getInstance().getConfig().getCore().chunkLoader.alwaysOn.expiry * 3600000L;
            } else {
                return System.currentTimeMillis() - playerData.get().getLastOnline() > BetterChunkLoader.getInstance().getConfig().getCore().chunkLoader.online.expiry * 3600000L;
            }
        }
        return true;
    }

    public boolean isLoadable() {
        Optional<Player> player = Sponge.getServer().getPlayer(owner);
        if (isAlwaysOn) {
            return !this.isExpired() && this.blockCheck();
        } else {
            return !this.isExpired() && this.blockCheck() && player.isPresent();
        }
    }

    public Integer getChunks() {
        return Double.valueOf(Math.pow((2 * radius) + 1, 2)).intValue();
    }

    public Boolean canEdit(Player player) {
        if (player.getUniqueId().equals(owner)) {
            return true;
        } else {
            return player.hasPermission("betterchunkloader.chunkloader.edit");
        }
    }

    public Boolean canCreate(Player player) {
        return player.hasPermission("betterchunkloader.chunkloader.create") || player.hasPermission("betterchunkloader.chunkloader.create." + (isAlwaysOn() ? "alwayson" : "online"));
    }

    public Boolean blockCheck() {
        Optional<World> _world = Sponge.getServer().getWorld(this.world);
        if (_world.isPresent()) {
            if (location == null) {
                return false;
            }
            BlockState block = _world.get().getBlock(location);
            if (isAlwaysOn) {
                return block.getType().equals(ALWAYSON_TYPE);
            } else {
                return block.getType().equals(ONLINE_TYPE);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.world + ":" + this.location.getX() + "," + this.location.getZ() + (this.isAlwaysOn ? "y" : "n") + " - " + this.getChunks() + " - " + this.location.toString();
    }

    public Boolean contains(Vector3i vector) {
        return location.getX() - radius <= vector.getX() && vector.getX() <= location.getX() + radius && location.getZ() - radius <= vector.getZ() && vector.getZ() <= location.getZ() + radius;
    }

    public Boolean contains(int chunkX, int chunkZ) {
        return location.getX() - radius <= chunkX && chunkX <= location.getX() + radius && location.getZ() - radius <= chunkZ && chunkZ <= location.getZ() + radius;
    }

}
