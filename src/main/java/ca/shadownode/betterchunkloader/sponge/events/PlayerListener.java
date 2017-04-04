package ca.shadownode.betterchunkloader.sponge.events;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.PlayerData;
import ca.shadownode.betterchunkloader.sponge.menu.ChunkLoaderMenu;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

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
        Optional<PlayerData> playerData = plugin.getDataStore().getPlayerData(player.getUniqueId());
        if (playerData.isPresent()) {
            playerData.get().setLastOnline(System.currentTimeMillis());
            plugin.getDataStore().updatePlayerData(playerData.get());
            List<ChunkLoader> clList = plugin.getDataStore().getChunkLoadersByOwner(player.getUniqueId());
            clList.stream().filter((chunkLoader) -> (!chunkLoader.isAlwaysOn() && chunkLoader.isLoadable())).forEachOrdered((chunkLoader) -> {
                plugin.getChunkManager().loadChunkLoader(chunkLoader);
            });
            plugin.getLogger().info("Loaded all online chunkloaders for Player: " + player.getName());
        }
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event) {
        Player player = event.getTargetEntity();
        Optional<PlayerData> playerData = plugin.getDataStore().getPlayerData(player.getUniqueId());
        if (playerData.isPresent()) {
            playerData.get().setLastOnline(System.currentTimeMillis());
            plugin.getDataStore().updatePlayerData(playerData.get());
            List<ChunkLoader> clList = plugin.getDataStore().getChunkLoadersByOwner(player.getUniqueId());
            clList.stream().filter((chunkLoader) -> (!chunkLoader.isAlwaysOn() && chunkLoader.isLoadable())).forEachOrdered((chunkLoader) -> {
                plugin.getChunkManager().unloadChunkLoader(chunkLoader);
            });
            plugin.getLogger().info("Unloaded all online chunkloaders for Player: " + player.getName());
        }
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
                if (!chunkLoader.isPresent()) {
                    chunkLoader = Optional.of(new ChunkLoader(
                            UUID.randomUUID(),
                            clickedBlock.getWorldUniqueId(),
                            player.getUniqueId(),
                            clickedBlock.getLocation().get().getBlockPosition(),
                            clickedBlock.getLocation().get().getChunkPosition(),
                            -1,
                            System.currentTimeMillis(),
                            clickedBlock.getState().getType().equals(BlockTypes.DIAMOND_BLOCK)
                    ));
                }
                if (!chunkLoader.get().canCreate(player)) {
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.noPermissionCreate));
                    return;
                }
                new ChunkLoaderMenu(plugin).showMenu(player, chunkLoader.get());
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

    @Listener
    public void onInventoryDrag(ClickInventoryEvent.Drag event) {
        Optional<InventoryProperty<String, ?>> optChunkLoaderMenu = event.getTargetInventory().getArchetype().getProperty("chunkloadermenuprop");
        if (!optChunkLoaderMenu.isPresent()) {
            return;
        }
        event.setCancelled(true);
    }

    @Listener
    public void onInventoryClickSecondary(ClickInventoryEvent.Secondary event) {
        Optional<InventoryProperty<String, ?>> optChunkLoaderMenu = event.getTargetInventory().getArchetype().getProperty("chunkloadermenuprop");
        if (!optChunkLoaderMenu.isPresent()) {
            return;
        }
        event.setCancelled(true);
    }

    @Listener
    public void onInventoryClickPrimary(ClickInventoryEvent.Primary event) {
        Optional<InventoryProperty<String, ?>> chunkLoaderMenuProp = event.getTargetInventory().getArchetype().getProperty("chunkloadermenuprop");
        if (!chunkLoaderMenuProp.isPresent()) {
            return;
        }
        ChunkLoader chunkLoader = (ChunkLoader) chunkLoaderMenuProp.get().getValue();
        if (chunkLoader == null) {
            return;
        }

        if (event.getCause().last(Player.class).isPresent()) {
            Player player = event.getCause().last(Player.class).get();

            event.setCancelled(true);

            if (!chunkLoader.canCreate(player)) {
                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.noPermissionCreate));
            }

            if (!chunkLoader.canEdit(player)) {
                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.noPermissionEdit));
                return;
            }

            Optional<PlayerData> playerData = plugin.getDataStore().getPlayerData(player.getUniqueId());
            if (!playerData.isPresent()) {
                return;
            }

            Optional<SlotPos> slotPos = getSlotPos(event.getCursorTransaction());
            Optional<Integer> radius = getRadius(event.getCursorTransaction());
            Optional<Integer> chunks = getChunks(event.getCursorTransaction());

            if (!slotPos.isPresent()) {
                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.invalidOption));
                return;
            }

            int available;
            if (chunkLoader.isAlwaysOn()) {
                available = playerData.get().getAlwaysOnChunksAmount();
            } else {
                available = playerData.get().getOnlineChunksAmount();
            }

            switch (slotPos.get().getX()) {
                case 0: {
                    if (chunkLoader.isAlwaysOn()) {
                        playerData.get().addAlwaysOnChunksAmount(chunkLoader.getChunks());
                    } else {
                        playerData.get().addOnlineChunksAmount(chunkLoader.getChunks());
                    }
                    plugin.getDataStore().removeChunkLoader(chunkLoader);
                    plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.removed));
                    return;
                }
                default: {

                    HashMap<String, String> args = new HashMap<>();
                    args.put("playerName", player.getName());
                    args.put("playerUUID", player.getUniqueId().toString());
                    args.put("ownerName", playerData.get().getName());
                    args.put("ownerUUID", playerData.get().getUnqiueId().toString());
                    args.put("chunks", String.valueOf(chunkLoader.getChunks()));
                    args.put("available", String.valueOf(available));

                    if (chunkLoader.getRadius() == -1) {
                        if (!player.hasPermission("betterchunkloader.chunkloader.unlimitedchunks")) {
                            if (chunks.get() > available) {
                                args.put("needed", String.valueOf(chunks.get()));
                                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.notEnough, args));
                                break;
                            } else {
                                if (chunkLoader.isAlwaysOn()) {
                                    playerData.get().setAlwaysOnChunksAmount(Math.subtractExact(available, chunks.get()));
                                } else {
                                    playerData.get().setOnlineChunksAmount(Math.subtractExact(available, chunks.get()));
                                }
                                plugin.getDataStore().updatePlayerData(playerData.get());
                            }
                        }
                        chunkLoader.setRadius(radius.get());
                        chunkLoader.setCreation(System.currentTimeMillis());
                        plugin.getLogger().info(player.getName() + " made a new chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " with radius " + chunkLoader.getRadius());
                        plugin.getDataStore().addChunkLoader(chunkLoader);
                        plugin.getChunkManager().loadChunkLoader(chunkLoader);
                        player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.created));
                        break;
                    } else {
                        if (!player.hasPermission("betterchunkloader.chunkloader.unlimitedchunks")) {
                            if (Math.subtractExact(chunks.get(), chunkLoader.getChunks()) > available) {
                                args.put("needed", String.valueOf(Math.subtractExact(chunks.get(), chunkLoader.getChunks())));
                                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.notEnough, args));
                                break;
                            } else {
                                if (chunkLoader.isAlwaysOn()) {
                                    playerData.get().setAlwaysOnChunksAmount(Math.subtractExact(chunks.get(), Math.addExact(available, chunkLoader.getChunks())));
                                } else {
                                    playerData.get().setOnlineChunksAmount(Math.subtractExact(chunks.get(), Math.addExact(available, chunkLoader.getChunks())));
                                }
                                plugin.getDataStore().updatePlayerData(playerData.get());
                            }
                        }
                        plugin.getLogger().info(player.getName() + " edited " + playerData.get().getName() + "'s chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " radius from " + chunkLoader.getRadius() + " to " + radius.get());
                        chunkLoader.setRadius(radius.get());
                        plugin.getDataStore().updateChunkLoader(chunkLoader);
                        plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                        plugin.getChunkManager().loadChunkLoader(chunkLoader);
                        player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.updated, args));
                        break;
                    }
                }
            }
        }
    }

    // All of this should be replaced when Sponge implements it's custom inventory data API. 
    public Optional<SlotPos> getSlotPos(Transaction<ItemStackSnapshot> transaction) {
        if (transaction.isValid()) {
            try { //SlotPos: X,Y
                List<Text> lore = transaction.getFinal().getOrElse(Keys.ITEM_LORE, new ArrayList<>());
                for (Text text : lore) {
                    if (text.toPlain().contains("SlotPos:")) {
                        String[] values = text.toPlain().substring(9).split(",");
                        return Optional.ofNullable(new SlotPos(Integer.parseInt(values[0]), Integer.parseInt(values[1])));
                    }
                }
            } catch (NumberFormatException ex) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public Optional<Integer> getRadius(Transaction<ItemStackSnapshot> transaction) {
        if (transaction.isValid()) {
            try { //Radius: V
                List<Text> lore = transaction.getFinal().getOrElse(Keys.ITEM_LORE, new ArrayList<>());
                for (Text text : lore) {
                    if (text.toPlain().contains("Radius:")) {
                        return Optional.ofNullable(Integer.parseInt(text.toPlain().substring(8)));
                    }
                }
            } catch (NumberFormatException ex) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public Optional<Integer> getChunks(Transaction<ItemStackSnapshot> transaction) {
        if (transaction.isValid()) {
            try { //Chunks: V
                List<Text> lore = transaction.getFinal().getOrElse(Keys.ITEM_LORE, new ArrayList<>());
                for (Text text : lore) {
                    if (text.toPlain().contains("Chunks:")) {
                        return Optional.ofNullable(Integer.parseInt(text.toPlain().substring(8)));
                    }
                }
            } catch (NumberFormatException ex) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
