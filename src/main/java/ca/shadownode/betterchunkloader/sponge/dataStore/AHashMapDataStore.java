package ca.shadownode.betterchunkloader.sponge.dataStore;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.PlayerData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import com.flowpowered.math.vector.Vector3i;
import java.util.HashMap;
import java.util.Optional;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * An implementation of IDataStore that stores data into HashMaps It's abstract
 * because it doesn't write any data on disk: all data will be lost at server
 * shutdown Classes that extend this class should store the data somewhere.
 */
public abstract class AHashMapDataStore implements IDataStore {

    protected BetterChunkLoader plugin;

    protected Map<UUID, List<ChunkLoader>> chunkLoaders = new HashMap<>();
    protected Map<UUID, PlayerData> playersData = new HashMap<>();

    public AHashMapDataStore(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<ChunkLoader> getChunkLoaders() {
        List<ChunkLoader> chunkloaders = new ArrayList<>();
        this.chunkLoaders.values().forEach((clList) -> {
            chunkloaders.addAll(clList);
        });
        return chunkloaders;
    }

    @Override
    public List<ChunkLoader> getChunkLoaders(World world) {
        List<ChunkLoader> list = this.chunkLoaders.get(world.getUniqueId());
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    @Override
    public List<ChunkLoader> getChunkLoadersByType(Boolean isAlwaysOn) {
        List<ChunkLoader> chunkloaders = new ArrayList<>();
        getChunkLoaders().stream().filter((cl) -> (cl.isAlwaysOn().equals(isAlwaysOn))).forEachOrdered((cl) -> {
            chunkloaders.add(cl);
        });
        return chunkloaders;
    }

    @Override
    public List<ChunkLoader> getChunkLoadersByOwner(UUID ownerUUID) {
        List<ChunkLoader> chunkloaders = new ArrayList<>();
        getChunkLoaders().stream().filter((cl) -> (cl.getOwner().equals(ownerUUID))).forEachOrdered((cl) -> {
            chunkloaders.add(cl);
        });
        return chunkloaders;
    }

    @Override
    public List<ChunkLoader> getChunkLoadersAt(World world, Vector3i chunk) {
        List<ChunkLoader> chunkloaders = new ArrayList<>();
        getChunkLoaders(world).stream().filter((cl) -> (cl.getChunk().getX() == chunk.getX() && cl.getChunk().getZ() == chunk.getZ())).forEachOrdered((cl) -> {
            chunkloaders.add(cl);
        });
        return chunkloaders;
    }

    @Override
    public Optional<ChunkLoader> getChunkLoaderAt(Location<World> blockLocation) {
        for (ChunkLoader cl : this.getChunkLoaders(blockLocation.getExtent())) {
            if (cl.getLocation().equals(blockLocation)) {
                return Optional.of(cl);
            }
        }
        return Optional.empty();
    }

    @Override
    public void addChunkLoader(ChunkLoader chunkLoader) {
        List<ChunkLoader> clList = this.chunkLoaders.get(chunkLoader.getWorld());
        if (clList == null) {
            clList = new ArrayList<>();
            this.chunkLoaders.put(chunkLoader.getWorld(), clList);
        }
        clList.add(chunkLoader);
        plugin.getLogger().info("Added chunk loader at " + Utilities.getReadableLocation(chunkLoader.getLocation()) + " (range:" + chunkLoader.getRadius() + ")");
        Optional<Player> optPlayer = Sponge.getServer().getPlayer(chunkLoader.getOwner());
        if (optPlayer.isPresent()) {
            optPlayer.get().spawnParticles(ParticleEffect.builder().type(ParticleTypes.MOBSPAWNER_FLAMES).quantity(10).build(), chunkLoader.getLocation().getPosition());
        }
        if (chunkLoader.isLoadable()) {
            plugin.getChunkManager().loadChunkLoader(chunkLoader);
        }
    }

    @Override
    public void removeChunkLoader(ChunkLoader chunkLoader) {
        List<ChunkLoader> clList = this.chunkLoaders.get(chunkLoader.getWorld());
        if (clList != null) {
            if (chunkLoader.blockCheck()) {
                Optional<Player> optPlayer = Sponge.getServer().getPlayer(chunkLoader.getOwner());
                if (optPlayer.isPresent()) {
                    optPlayer.get().spawnParticles(ParticleEffect.builder().type(ParticleTypes.SPLASH_POTION).quantity(10).build(), chunkLoader.getLocation().getPosition());
                }
            }
            plugin.getLogger().info("Removed chunk loader at chunk " + Utilities.getReadableLocation(chunkLoader.getLocation()) + " (range:" + chunkLoader.getRadius() + ")");
            clList.remove(chunkLoader);
            plugin.getChunkManager().unloadChunkLoader(chunkLoader);
        }
    }

    @Override
    public void removeAllChunkLoaders(UUID owner) {
        List<ChunkLoader> clList = this.getChunkLoaders();
        clList.forEach((cl) -> {
            if (cl.getOwner().equals(owner)) {
                this.chunkLoaders.get(cl.getWorld()).remove(cl);
            }
        });
    }

    @Override
    public void removeAllChunkLoaders(World world) {
        List<ChunkLoader> clList = this.chunkLoaders.get(world.getUniqueId());
        clList.forEach((cl) -> {
            this.chunkLoaders.get(world.getUniqueId()).remove(cl);
        });
    }

    @Override
    public void setChunkLoaderRadius(ChunkLoader chunkLoader, Integer radius) {
        if (chunkLoader.isLoadable()) {
            removeChunkLoader(chunkLoader);
        }
        chunkLoader.setRadius(radius);
        if (chunkLoader.isLoadable()) {
            addChunkLoader(chunkLoader);
        }
    }

    @Override
    public Optional<PlayerData> getOrCreatePlayerData(UUID playerUUID) {
        if (this.playersData.containsKey(playerUUID)) {
            return Optional.of(this.playersData.get(playerUUID));
        }
        Optional<String> playerName = Utilities.getPlayerName(playerUUID);
        if (playerName.isPresent()) {
            PlayerData playerData = new PlayerData(playerName.get(), playerUUID);
            this.playersData.put(playerUUID, playerData);
            return Optional.of(playerData);
        }
        return Optional.empty();
    }

    @Override
    public Optional<PlayerData> getOrCreatePlayerData(String playerName) {
        Optional<UUID> playerUUID = Utilities.getPlayerUUID(playerName);
        if (playerUUID.isPresent()) {
            if (this.playersData.containsKey(playerUUID.get())) {
                return Optional.of(this.playersData.get(playerUUID.get()));
            }
            PlayerData playerData = new PlayerData(playerName, playerUUID.get());
            this.playersData.put(playerUUID.get(), playerData);
            return Optional.of(playerData);

        }
        return Optional.empty();
    }

    @Override
    public Optional<PlayerData> getPlayerData(UUID playerUUID) {
        if (this.playersData.containsKey(playerUUID)) {
            return Optional.of(this.playersData.get(playerUUID));
        }
        return Optional.empty();
    }

    @Override
    public Optional<PlayerData> getPlayerData(String playerName) {
        Optional<UUID> playerUUID = Utilities.getPlayerUUID(playerName);
        if (playerUUID.isPresent()) {
            if (this.playersData.containsKey(playerUUID.get())) {
                return Optional.of(this.playersData.get(playerUUID.get()));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<PlayerData> getPlayersData() {
        return new ArrayList<>(this.playersData.values());
    }

    @Override
    public Boolean playerDataExists(UUID playerUUID) {
        return this.playersData.containsKey(playerUUID);
    }
}
