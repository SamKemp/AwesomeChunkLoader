package ca.shadownode.betterchunkloader.sponge.events;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoaderMenu;
import ca.shadownode.betterchunkloader.sponge.data.PlayerData;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

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
        plugin.getDataStore().refreshPlayer(player.getUniqueId());
        PlayerData playerData = plugin.getDataStore().getPlayerData(player.getUniqueId());
        playerData.setLastOnline(new Timestamp(System.currentTimeMillis()));
        plugin.getDataStore().updatePlayerData(playerData);
        List<ChunkLoader> clList = plugin.getDataStore().getChunkLoaders(player.getUniqueId());
        clList.stream().filter((chunkLoader) -> (!chunkLoader.isAlwaysOn() && chunkLoader.isLoadable())).forEachOrdered((chunkLoader) -> {
            plugin.getChunkManager().loadChunkLoader(chunkLoader);
        });
        plugin.getLogger().info("Loaded all online chunkloaders for Player: " + player.getName());
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event) {
        Player player = event.getTargetEntity();
        PlayerData playerData = plugin.getDataStore().getPlayerData(player.getUniqueId());
        playerData.setLastOnline(new Timestamp(System.currentTimeMillis()));
        plugin.getDataStore().updatePlayerData(playerData);
        List<ChunkLoader> clList = plugin.getDataStore().getChunkLoaders(player.getUniqueId());
        clList.stream().filter((chunkLoader) -> (!chunkLoader.isAlwaysOn() && chunkLoader.isLoadable())).forEachOrdered((chunkLoader) -> {
            plugin.getChunkManager().unloadChunkLoader(chunkLoader);
        });
        plugin.getLogger().info("Unloaded all online chunkloaders for Player: " + player.getName());
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

        if (clickedBlock.getState().getType().equals(BlockTypes.DIAMOND_BLOCK) || clickedBlock.getState().getType().equals(BlockTypes.IRON_BLOCK)) {
            Optional<ChunkLoader> chunkLoader = plugin.getDataStore().getChunkLoaderAt(clickedBlock.getLocation().get());
            if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent() && player.getItemInHand(HandTypes.MAIN_HAND).get().getItem().getType().equals(ItemTypes.BLAZE_ROD)) {
                if (chunkLoader.isPresent()) {
                    if (player.getUniqueId().equals(chunkLoader.get().getOwner()) || player.hasPermission("betterchunkloader.edit")) {
                        chunkLoader.get().showUI(player);
                    } else {
                        player.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().clClickNotAllowed));
                    }
                } else {
                    chunkLoader = Optional.of(new ChunkLoader(
                            UUID.randomUUID(),
                            clickedBlock.getWorldUniqueId(),
                            player.getUniqueId(),
                            clickedBlock.getLocation().get(),
                            clickedBlock.getLocation().get().getChunkPosition(),
                            -1,
                            new Timestamp(System.currentTimeMillis()),
                            clickedBlock.getState().getType().equals(BlockTypes.DIAMOND_BLOCK)
                    ));
                    chunkLoader.get().showUI(player);
                }
            } else {
                if (chunkLoader.isPresent()) {
                    PlayerData playerData = plugin.getDataStore().getPlayerData(chunkLoader.get().getOwner());
                    for (String message : plugin.getConfig().clClickExists) {
                        player.sendMessage(Utilities.parseMessage(message, new String[]{
                            playerData.getName(),
                            Utilities.getReadableLocation(chunkLoader.get().getLocation()),
                            (chunkLoader.get().isAlwaysOn() ? "Always On" : "Online Only"),
                            String.valueOf(chunkLoader.get().getChunks())
                        }));
                    }
                } else {
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().clClickNotExists));
                }
            }
        }
    }

    @Listener
    public void onInventoryDrag(ClickInventoryEvent.Drag event) {
        Optional<InventoryProperty<String, ?>> optChunkLoaderMenu = event.getTargetInventory().getArchetype().getProperty("chunkloadermenu");
        if (!optChunkLoaderMenu.isPresent()) {
            return;
        }
        event.setCancelled(true);
    }

    @Listener
    public void onInventoryClickSecondary(ClickInventoryEvent.Secondary event) {
        Optional<InventoryProperty<String, ?>> optChunkLoaderMenu = event.getTargetInventory().getArchetype().getProperty("chunkloadermenu");
        if (!optChunkLoaderMenu.isPresent()) {
            return;
        }
        event.setCancelled(true);
    }

    @Listener
    public void onInventoryClickPrimary(ClickInventoryEvent.Primary event) {
        Optional<InventoryProperty<String, ?>> optChunkLoaderMenu = event.getTargetInventory().getArchetype().getProperty("chunkloadermenu");
        if (!optChunkLoaderMenu.isPresent()) {
            return;
        }
        ChunkLoaderMenu chunkLoaderMenu = (ChunkLoaderMenu) optChunkLoaderMenu.get();
        ChunkLoader chunkLoader = chunkLoaderMenu.getValue();
        if (chunkLoader == null) {
            return;
        }

        if (event.getCause().last(Player.class).isPresent()) {
            Player player = event.getCause().last(Player.class).get();

            event.setCancelled(true);

            if (!player.getUniqueId().equals(chunkLoader.getOwner()) && !player.hasPermission("betterchunkloader.edit")) {
                player.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().clMenuNotAllowed));
                return;
            }

            PlayerData playerData = plugin.getDataStore().getPlayerData(player.getUniqueId());

            Integer option = getRadius(event.getTransactions());

            int available;
            if (chunkLoader.isAlwaysOn()) {
                available = playerData.getOfflineChunksAmount();
            } else {
                available = playerData.getOnlineChunksAmount();
            }

            switch (option) {
                case -1: {
                    plugin.getDataStore().removeChunkLoader(chunkLoader);
                    if (chunkLoader.isAlwaysOn()) {
                        playerData.setOfflineChunksAmount((available + chunkLoader.getChunks()));
                    } else {
                        playerData.setOnlineChunksAmount((available + chunkLoader.getChunks()));
                    }
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().clMenuRemove));
                    return;
                }
                case -2: {
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().clMenuOptionNotValid));
                    return;
                }
                default: {
                    int needed = chunkLoader.getSide(option) * chunkLoader.getSide(option);
                    switch (chunkLoader.getRadius()) {
                        case -1: {
                            /*Create new chunkloader*/
                            if (!player.hasPermission("betterchunkloader.unlimitedchunks")) {
                                if (needed > available) {
                                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().clMenuNotEnoughChunks, new String[]{String.valueOf(needed), String.valueOf(available)}));
                                    break;
                                }
                            }
                            if (chunkLoader.isAlwaysOn()) {
                                playerData.setOfflineChunksAmount(available - needed);
                            } else {
                                playerData.setOnlineChunksAmount(available - needed);
                            }
                            plugin.getDataStore().updatePlayerData(playerData);
                            chunkLoader.setRadius(option);
                            chunkLoader.setCreation(new Timestamp(System.currentTimeMillis()));
                            plugin.getLogger().info(player.getName() + " made a new chunk loader at " + Utilities.getReadableLocation(chunkLoader.getLocation()) + " with radius " + chunkLoader.getRadius());
                            plugin.getDataStore().addChunkLoader(chunkLoader);
                            player.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().clMenuCreated));
                            break;
                        }
                        default: {
                            if (!player.hasPermission("betterchunkloader.unlimitedchunks")) {
                                if ((needed - chunkLoader.getChunks()) > available) {
                                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().clMenuNotEnoughChunks, new String[]{String.valueOf((needed - chunkLoader.getChunks())), String.valueOf(available)}));
                                    break;
                                }
                            }
                            if (chunkLoader.isAlwaysOn()) {
                                playerData.setOfflineChunksAmount((available - (needed - chunkLoader.getChunks())));
                            } else {
                                playerData.setOnlineChunksAmount((available - (needed - chunkLoader.getChunks())));
                            }
                            plugin.getDataStore().updatePlayerData(playerData);
                            plugin.getLogger().info(player.getName() + " edited " + playerData.getName() + "'s chunk loader at " + Utilities.getReadableLocation(chunkLoader.getLocation()) + " radius from " + chunkLoader.getRadius() + " to " + option);
                            plugin.getDataStore().changeChunkLoaderRadius(chunkLoader, option);
                            player.sendMessage(Utilities.parseMessage(plugin.getConfig().msgPrefix + plugin.getConfig().clMenuUpdated));
                            break;
                        }
                    }
                }
            }
        }
    }

    public Integer getRadius(List<SlotTransaction> transactions) {
        String firstChar;
        if (!transactions.isEmpty() && transactions.get(0).getOriginal().get(Keys.DISPLAY_NAME).isPresent()) {
            try {
                firstChar = String.valueOf(transactions.get(0).getOriginal().get(Keys.DISPLAY_NAME).get().toPlain().charAt(8));
                return Integer.parseInt(firstChar);
            } catch (Exception e) {
                return -1;
            }
        }
        return -2;
    }
}
