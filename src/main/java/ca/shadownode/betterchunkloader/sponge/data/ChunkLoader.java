package ca.shadownode.betterchunkloader.sponge.data;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import java.sql.Timestamp;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.flowpowered.math.vector.Vector3i;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public final class ChunkLoader {

    private UUID uuid;
    private UUID world;
    private UUID owner;

    private Location<World> location;
    private Vector3i chunk;
    private Integer radius;

    private Timestamp creation;
    private boolean isAlwaysOn;

    /*Radius,Side*/
    private final Map<Integer, Integer> side = new HashMap<Integer, Integer>() {
        {
            put(0, 1);
            put(1, 3);
            put(2, 5);
            put(3, 7);
            put(4, 9);
        }
    ;

    };

    public ChunkLoader(UUID uuid, UUID world, UUID owner, Location<World> location, Vector3i chunk, Integer radius, Timestamp creation, boolean isAlwaysOn) {
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

    public UUID getOwner() {
        return owner;
    }

    @XmlAttribute(name = "owner")
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Location<World> getLocation() {
        return location;
    }

    public Timestamp getCreation() {
        return creation;
    }

    @XmlAttribute(name = "creation")
    public void setCreation(Timestamp creation) {
        this.creation = creation;
    }

    public boolean isAlwaysOn() {
        return isAlwaysOn;
    }

    @XmlAttribute(name = "alwaysOn")
    void setAlwaysOn(boolean isAlwaysOn) {
        this.isAlwaysOn = isAlwaysOn;
    }

    public Integer getRadius() {
        return radius;
    }

    @XmlAttribute(name = "r")
    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    public Vector3i getChunk() {
        return chunk;
    }

    public boolean isExpired() {
        PlayerData playerData = BetterChunkLoader.getInstance().getDataStore().getPlayerData(getOwner());
        LocalDateTime limit = playerData.getLastOnline().toLocalDateTime().plusDays(3);
        LocalDateTime current = LocalDateTime.now();
        return current.isAfter(limit);
    }

    public boolean isLoadable() {
        return blockCheck() && !isExpired();
    }

    public int getSide(Integer index) {
        return side.get(index);
    }

    public int getChunks() {
        return side.get(getRadius()) * side.get(getRadius());
    }

    public boolean blockCheck() {
        if (this.location.getBlock() == null) {
            return false;
        }
        if (isAlwaysOn) {
            return this.location.getBlock().getType().equals(BlockTypes.DIAMOND_BLOCK);
        } else {
            return this.location.getBlock().getType().equals(BlockTypes.IRON_BLOCK);
        }
    }

    public void showUI(Player player) {
        PlayerData playerData = BetterChunkLoader.getInstance().getDataStore().getPlayerData(getOwner());
        String title = (radius != -1 ? "BCL: " + playerData.getName() + " Chunks: " + getChunks() + " " : this.isAlwaysOn() ? "Always On Chunk Loader" : "Online Only ChunkLoader");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        InventoryArchetype inventoryArchetype = InventoryArchetype.builder().from(InventoryArchetypes.MENU_ROW).property(ChunkLoaderMenu.of(this)).build("archid", "archname");
        Inventory inventory = Inventory.builder().of(inventoryArchetype).property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(title))).build(BetterChunkLoader.getInstance());
        if (getRadius() != -1) {
            addInventoryOption(inventory, new SlotPos(0, 0), ItemTypes.REDSTONE_TORCH, "Remove");
        }

        int pos = 2;
        for (int i = 0; i < 5;) {
            int chunks = side.get(i) * side.get(i);
            if (getRadius() == i) {
                addInventoryOption(inventory, new SlotPos(pos, 0), ItemTypes.POTION, "Radius: " + i + " Chunks: " + chunks + " [Active]");
            } else {
                addInventoryOption(inventory, new SlotPos(pos, 0), ItemTypes.GLASS_BOTTLE, "Radius: " + i + " Chunks: " + chunks);
            }
            pos++;
            i++;
        }
        player.openInventory(inventory, Cause.of(NamedCause.simulated(player)));
    }

    public void addInventoryOption(Inventory inventory, SlotPos slotPos, ItemType icon, String name) {
        ItemStack itemStack = ItemStack.builder().itemType(icon).quantity(1).build();
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(name));
        inventory.query(slotPos).set(itemStack);
    }

    @Override
    public String toString() {
        return this.world + ":" + this.location.getX() + "," + this.location.getZ() + (this.isAlwaysOn ? "y" : "n") + " - " + this.getChunks() + " - " + this.location.toString();
    }

    public boolean contains(Vector3i vector) {
        return location.getX() - radius <= vector.getX() && vector.getX() <= location.getX() + radius && location.getZ() - radius <= vector.getZ() && vector.getZ() <= location.getZ() + radius;
    }

    public boolean contains(int chunkX, int chunkZ) {
        return location.getX() - radius <= chunkX && chunkX <= location.getX() + radius && location.getZ() - radius <= chunkZ && chunkZ <= location.getZ() + radius;
    }

}
