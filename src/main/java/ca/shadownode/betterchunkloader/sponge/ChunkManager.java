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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.api.world.ChunkTicketManager.LoadingTicket;
import org.spongepowered.api.world.World;

public class ChunkManager {

    private final BetterChunkLoader plugin;
    private final Optional<ChunkTicketManager> ticketManager;

    private final HashMap<UUID, Optional<LoadingTicket>> tickets = new HashMap<>();

    public ChunkManager(BetterChunkLoader plugin) {
        this.plugin = plugin;
        ticketManager = Sponge.getServiceManager().provide(ChunkTicketManager.class);
        if (ticketManager.isPresent()) {
            ticketManager.get().registerCallback(plugin, new ChunkLoadingCallback(plugin));
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
        getChunks(chunkLoader.getRadius(), mainChunk.get()).forEach((chunk) -> {
            loadChunk(world.get(), chunk);
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
        getChunks(chunkLoader.getRadius(), mainChunk.get()).forEach((chunk) -> {
            List<ChunkLoader> clList = plugin.getDataStore().getChunkLoadersAt(chunkLoader.getWorld(), chunk.getPosition());
            if (clList.isEmpty()) {
                unloadChunk(world.get(), chunk);
            }
        });
        return true;
    }

    /**
     *
     * Loads chunk using old or new ticket.
     *
     * @param world
     * @param chunk
     * @return
     */
    public boolean loadChunk(World world, Chunk chunk) {
        if (!ticketManager.isPresent()) {
            return false;
        }
        Optional<LoadingTicket> ticket;
        if (tickets.containsKey(world.getUniqueId()) && tickets.get(world.getUniqueId()).isPresent()) {
            ticket = tickets.get(world.getUniqueId());
        } else {
            ticket = ticketManager.get().createTicket(plugin, world);
            tickets.put(world.getUniqueId(), ticket);
        }
        if (ticket.isPresent()) {
            ticket.get().forceChunk(chunk.getPosition());
            return true;
        }
        return false;
    }

    /**
     * Unloads chunk using tickets.
     *
     * @param world
     * @param chunk
     * @return
     */
    public boolean unloadChunk(World world, Chunk chunk) {
        if (!ticketManager.isPresent()) {
            return false;
        }
        Optional<LoadingTicket> ticket;
        if (tickets.containsKey(world.getUniqueId()) && tickets.get(world.getUniqueId()).isPresent()) {
            ticket = tickets.get(world.getUniqueId());
            if (ticket.isPresent()) {
                ticket.get().unforceChunk(chunk.getPosition());
                return true;
            }
        }
        return false;
    }

    /*
        Gets all tickets controlled by this library.
     */
    public Map<UUID, Optional<LoadingTicket>> getTickets() {
        return tickets;
    }

    /*
       Gets Sponge ChunkTicketManager instance.
     */
    public Optional<ChunkTicketManager> getTicketManager() {
        return ticketManager;
    }

    private Field getField(Class<?> targetClass, String fieldName) throws NoSuchFieldException, SecurityException {
        Field field = targetClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    public List<Chunk> getChunks(Integer radius, Chunk chunk) {
        List<Chunk> chunks = new ArrayList<>(Arrays.asList());
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Optional<Chunk> found = chunk.getWorld().getChunk(chunk.getPosition().add(x, 0, z));
                if(found.isPresent()) {
                    chunks.add(found.get());
                }
            }
        }
        return chunks;
    }
}
