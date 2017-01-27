
package ca.shadownode.betterchunkloader.sponge;

import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import com.flowpowered.math.vector.Vector3i;
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
    
    private final HashMap<UUID, Optional<LoadingTicket>> tickets; //World UUID : LoadingTicket

    public ChunkManager(BetterChunkLoader plugin) {
        this.plugin = plugin;
        tickets = new HashMap<>();
        ticketManager = Sponge.getServiceManager().provide(ChunkTicketManager.class);
    }
    
    /*
        Loads chunk using old or new ticket based on vector.
    */
    public boolean loadChunk(UUID worldUUID, Vector3i vector) {
        if (!ticketManager.isPresent()) return false;
        Optional<World> world = Sponge.getServer().getWorld(worldUUID);
        if(!world.isPresent()) return false;
        Optional<Chunk> chunk = world.get().getChunk(vector);
        if (!chunk.isPresent()) return false;
        Optional<LoadingTicket> ticket;
        if (tickets.containsKey(worldUUID) && tickets.get(worldUUID).isPresent()) {
            ticket = tickets.get(worldUUID);
        } else {
            ticket = ticketManager.get().createTicket(plugin, world.get());
            tickets.put(worldUUID, ticket);
        }
        if (ticket.isPresent()) {
            ticket.get().forceChunk(chunk.get().getPosition());
            return true;
        }
        System.out.println("Ticket not present");
        return false;
    }

    /*
        Unloads chunk using tickets based on vector.  
    */
    public boolean unloadChunk(UUID worldUUID, Vector3i vector) {
        if (!ticketManager.isPresent()) return false;
        Optional<World> world = Sponge.getServer().getWorld(worldUUID);
        if (!world.isPresent()) return false;
        Optional<Chunk> chunk = world.get().getChunk(vector);
        if (!chunk.isPresent()) return false;
        Optional<LoadingTicket> ticket;
        if(tickets.containsKey(worldUUID) && tickets.get(worldUUID).isPresent()) {
            ticket = tickets.get(worldUUID);
            ticket.get().unforceChunk(chunk.get().getPosition());
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
}
