package ca.shadownode.betterchunkloader.sponge;

import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.events.ChunkLoadingCallback;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;

public class ChunkManager {

    private final BetterChunkLoader plugin;

    private final HashMap<UUID, Optional<Ticket>> tickets = new HashMap<>();

    public ChunkManager(BetterChunkLoader plugin) {
        this.plugin = plugin;
        try {
            boolean overridesEnabled = getField(ForgeChunkManager.class, "overridesEnabled").getBoolean(null);

            if (!overridesEnabled) {
                getField(ForgeChunkManager.class, "overridesEnabled").set(null, true);
            }

            Map<String, Integer> ticketConstraints = (Map<String, Integer>) getField(ForgeChunkManager.class, "ticketConstraints").get(null);
            Map<String, Integer> chunkConstraints = (Map<String, Integer>) getField(ForgeChunkManager.class, "chunkConstraints").get(null);

            ticketConstraints.put("betterchunkloader", Integer.MAX_VALUE);
            chunkConstraints.put("betterchunkloader", Integer.MAX_VALUE);

            ForgeChunkManager.setForcedChunkLoadingCallback(plugin, new ChunkLoadingCallback(plugin));

        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            plugin.getLogger().debug("ChunkManager failed to force chunk constraints", ex);
        }
    }

    public boolean loadChunkLoader(ChunkLoader chunkLoader) {
        Optional<World> world = Sponge.getServer().getWorld(chunkLoader.getWorld());
        if (!world.isPresent()) {
            return false;
        }
        Optional<Chunk> mainChunk = world.get().getChunk(chunkLoader.getChunk());
        if (!mainChunk.isPresent()) {
            return false;
        }
        List<Chunk> chunks = getChunks(chunkLoader.getRadius(), mainChunk.get());
        chunks.forEach((chunk) -> {
            loadChunk(chunkLoader, chunk);
        });
        return true;
    }

    public boolean unloadChunkLoader(ChunkLoader chunkLoader) {
        Optional<World> world = Sponge.getServer().getWorld(chunkLoader.getWorld());
        if (!world.isPresent()) {
            return false;
        }
        Optional<Chunk> mainChunk = world.get().getChunk(chunkLoader.getChunk());
        if (!mainChunk.isPresent()) {
            return false;
        }
        List<Chunk> chunks = getChunks(chunkLoader.getRadius(), mainChunk.get());
        chunks.forEach((chunk) -> {
            unloadChunk(chunkLoader, chunk);
        });
        return true;
    }

    public Optional<WorldServer> getWorld(String worldName) {
        for (WorldServer world : DimensionManager.getWorlds()) {
            if (world.getWorldInfo().getWorldName().equalsIgnoreCase(worldName)) {
                return Optional.of(world);
            }
        }
        return Optional.empty();
    }

    /**
     *
     * Loads chunk using old or new ticket.
     *
     * @param chunkLoader
     * @param chunk
     * @return
     */
    public boolean loadChunk(ChunkLoader chunkLoader, Chunk chunk) {
        Optional<WorldServer> world = this.getWorld(chunk.getWorld().getName());
        if (!world.isPresent()) {
            return false;
        }
        Optional<Ticket> ticket;
        if (tickets.containsKey(chunkLoader.getUniqueId()) && tickets.get(chunkLoader.getUniqueId()).isPresent()) {
            ticket = tickets.get(chunkLoader.getUniqueId());
        } else {
            ticket = Optional.of(ForgeChunkManager.requestTicket(plugin, world.get(), Type.NORMAL));
            tickets.put(chunkLoader.getUniqueId(), ticket);
        }
        if (ticket.isPresent()) {
            ForgeChunkManager.forceChunk(ticket.get(), new ChunkPos(chunk.getPosition().getX(), chunk.getPosition().getZ()));
            if (plugin.getConfig().getCore().debug) {
                System.out.println("LOAD");
                System.out.println("CList: " + Arrays.toString(ticket.get().getChunkList().toArray()));
            }
            return true;
        }
        return false;
    }

    /**
     * Unloads chunk using tickets.
     *
     * @param chunkLoader
     * @param chunk
     * @return
     */
    public boolean unloadChunk(ChunkLoader chunkLoader, Chunk chunk) {
        if (tickets.containsKey(chunkLoader.getUniqueId())) {
            Optional<Ticket> ticket = tickets.get(chunkLoader.getUniqueId());
            if (ticket.isPresent() && chunk != null) {
                ForgeChunkManager.unforceChunk(ticket.get(), new ChunkPos(chunk.getPosition().getX(), chunk.getPosition().getZ()));
                if (plugin.getConfig().getCore().debug) {
                    System.out.println("UNLOAD");
                    System.out.println("CList: " + Arrays.toString(ticket.get().getChunkList().toArray()));
                }
                return true;
            }
        }
        return false;
    }

    /*
        Gets all tickets controlled by this library.
     */
    public Map<UUID, Optional<Ticket>> getTickets() {
        return tickets;
    }

    public List<Chunk> getChunks(Integer radius, Chunk chunk) {
        List<Chunk> chunks = new ArrayList<>(Arrays.asList());
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Optional<Chunk> found = chunk.getWorld().getChunk(chunk.getPosition().add(x, 0, z));
                if (found.isPresent()) {
                    chunks.add(found.get());
                }
            }
        }
        return chunks;
    }

    private Field getField(Class<?> targetClass, String fieldName) throws NoSuchFieldException, SecurityException {
        Field field = targetClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
}
