package ca.shadownode.betterchunkloader.sponge.events;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.PlayerData;
import ca.shadownode.betterchunkloader.sponge.menu.Menu;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class PlayerListener {

    private final BetterChunkLoader plugin;

    public PlayerListener(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    @Listener
    public void onPlayerLogin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        plugin.getDataStore().getPlayerData(player.getUniqueId()).ifPresent((playerData) -> {
            playerData.setLastOnline(System.currentTimeMillis());
            plugin.getDataStore().updatePlayerData(playerData);
            List<ChunkLoader> clList = plugin.getDataStore().getChunkLoadersByOwner(player.getUniqueId());
            clList.stream().filter((chunkLoader) -> (chunkLoader.isLoadable())).forEachOrdered((chunkLoader) -> {
                plugin.getChunkManager().loadChunkLoader(chunkLoader);
            });
            plugin.getLogger().info("Loaded all online chunkloaders for Player: " + player.getName());
        });
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event) {
        Player player = event.getTargetEntity();
        plugin.getDataStore().getPlayerData(player.getUniqueId()).ifPresent((playerData) -> {
            playerData.setLastOnline(System.currentTimeMillis());
            plugin.getDataStore().updatePlayerData(playerData);
            List<ChunkLoader> clList = plugin.getDataStore().getChunkLoadersByOwner(player.getUniqueId());
            clList.stream().filter((chunkLoader) -> (!chunkLoader.isAlwaysOn() && chunkLoader.isLoadable())).forEachOrdered((chunkLoader) -> {
                plugin.getChunkManager().unloadChunkLoader(chunkLoader);
            });
            plugin.getLogger().info("Unloaded all online chunkloaders for Player: " + player.getName());
        });
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary event) {
        if (!event.getCause().containsType(Player.class)) {
            return;
        }
        Player player = event.getCause().last(Player.class).get();
        BlockSnapshot clickedBlock = event.getTargetBlock();

        if (clickedBlock == null || player == null) {
            return;
        }

        if (!clickedBlock.getState().getType().equals(ChunkLoader.ONLINE_TYPE) && !clickedBlock.getState().getType().equals(ChunkLoader.ALWAYSON_TYPE)) {
            return;
        }

        Optional<ChunkLoader> chunkLoader = plugin.getDataStore().getChunkLoaderAt(clickedBlock.getLocation().get());
        if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent() && player.getItemInHand(HandTypes.MAIN_HAND).get().getItem().getType().getId().equalsIgnoreCase(plugin.getConfig().getCore().chunkLoader.wandType)) {
            if (!chunkLoader.isPresent()) {
                chunkLoader = Optional.of(new ChunkLoader(
                        UUID.randomUUID(),
                        clickedBlock.getWorldUniqueId(),
                        player.getUniqueId(),
                        clickedBlock.getLocation().get().getBlockPosition(),
                        clickedBlock.getLocation().get().getChunkPosition(),
                        -1,
                        System.currentTimeMillis(),
                        clickedBlock.getState().getType().equals(ChunkLoader.ALWAYSON_TYPE)
                ));
            }
            if (!chunkLoader.get().canCreate(player)) {
                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.noPermissionCreate));
                return;
            }
            new Menu(plugin).showMenu(player, chunkLoader.get());
        } else {
            HashMap<String, String> args = new HashMap<>();
            args.put("playerName", player.getName());
            args.put("playerUUID", player.getUniqueId().toString());
            if (chunkLoader.isPresent()) {
                Optional<PlayerData> playerData = plugin.getDataStore().getPlayerData(chunkLoader.get().getOwner());
                if (playerData.isPresent()) {
                    args.put("ownerName", playerData.get().getName());
                    args.put("ownerUUID", playerData.get().getUnqiueId().toString());
                }
                args.put("location", Utilities.getReadableLocation(chunkLoader.get().getWorld(), chunkLoader.get().getLocation()));
                args.put("chunks", String.valueOf(chunkLoader.get().getChunks()));
                args.put("type", (chunkLoader.get().isAlwaysOn() ? "Always On" : "Online"));
                if (chunkLoader.get().canEdit(player)) {
                    plugin.getPaginationService().builder()
                            .contents(Utilities.parseMessageList(plugin.getConfig().getMessages().chunkLoader.info.items, args))
                            .title(Utilities.parseMessage(plugin.getConfig().getMessages().chunkLoader.info.title))
                            .padding(Utilities.parseMessage(plugin.getConfig().getMessages().chunkLoader.info.padding))
                            .sendTo(player);
                } else {
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.noPermissionEdit, args));
                }
            } else {
                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.creationHelp, args));
            }
        }

    }
}
