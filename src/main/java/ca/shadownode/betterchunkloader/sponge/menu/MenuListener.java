package ca.shadownode.betterchunkloader.sponge.menu;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

public class MenuListener {

    private final BetterChunkLoader plugin;

    public MenuListener(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onMenuInteract(ClickInventoryEvent event) {

        Optional<MenuProperty> optMenuProperty = event.getTargetInventory().getProperty(MenuProperty.class, MenuProperty.PROPERTY_NAME);
        if (!optMenuProperty.isPresent()) {
            return;
        }

        MenuProperty menuProperty = (MenuProperty) optMenuProperty.get();
        ChunkLoader chunkLoader = menuProperty.getValue();

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

            plugin.getDataStore().getPlayerData(player.getUniqueId()).ifPresent((playerData) -> {

                Optional<SlotPos> slotPos = getSlotPos(event.getCursorTransaction());
                Optional<Integer> radius = getRadius(event.getCursorTransaction());
                Optional<Integer> chunks = getChunks(event.getCursorTransaction());

                if (!slotPos.isPresent()) {
                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.invalidOption));
                    return;
                }

                int available;
                if (chunkLoader.isAlwaysOn()) {
                    available = playerData.getAlwaysOnChunksAmount();
                } else {
                    available = playerData.getOnlineChunksAmount();
                }

                switch (slotPos.get().getX()) {
                    case 0: {
                        if (plugin.getDataStore().removeChunkLoader(chunkLoader)) {
                            plugin.getChunkManager().unloadChunkLoader(chunkLoader);

                            if (chunkLoader.isAlwaysOn() && !player.hasPermission("betterchunkloader.chunkloader.unlimitedchunks")) {
                                playerData.addAlwaysOnChunksAmount(chunkLoader.getChunks());
                            } else {
                                playerData.addOnlineChunksAmount(chunkLoader.getChunks());
                            }
                            plugin.getDataStore().updatePlayerData(playerData);
                            player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.removeSuccess));
                        } else {
                            player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.removeFailure));
                        }
                        return;
                    }
                    default: {

                        HashMap<String, String> args = new HashMap<>();
                        args.put("playerName", player.getName());
                        args.put("playerUUID", player.getUniqueId().toString());
                        args.put("ownerName", playerData.getName());
                        args.put("ownerUUID", playerData.getUnqiueId().toString());
                        args.put("chunks", String.valueOf(chunkLoader.getChunks()));
                        args.put("available", String.valueOf(available));

                        if (chunkLoader.getRadius() == -1) {
                            if (!player.hasPermission("betterchunkloader.chunkloader.unlimitedchunks") && chunks.get() > available) {
                                args.put("needed", String.valueOf(chunks.get()));
                                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.notEnough, args));
                                break;
                            } else {
                                chunkLoader.setRadius(radius.get());
                                chunkLoader.setCreation(System.currentTimeMillis());
                                if (plugin.getDataStore().addChunkLoader(chunkLoader)) {
                                    plugin.getChunkManager().loadChunkLoader(chunkLoader);
                                    if (!player.hasPermission("betterchunkloader.chunkloader.unlimitedchunks")) {
                                        if (chunkLoader.isAlwaysOn()) {
                                            playerData.setAlwaysOnChunksAmount(Math.subtractExact(available, chunks.get()));
                                        } else {
                                            playerData.setOnlineChunksAmount(Math.subtractExact(available, chunks.get()));
                                        }
                                    }
                                    plugin.getDataStore().updatePlayerData(playerData);
                                    plugin.getLogger().info(player.getName() + " made a new chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " with radius " + chunkLoader.getRadius());
                                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.createSuccess));
                                } else {
                                    plugin.getLogger().info(player.getName() + " failed to create new chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " with radius " + chunkLoader.getRadius());
                                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.createFailure));
                                }
                            }
                            break;
                        } else {
                            if (!player.hasPermission("betterchunkloader.chunkloader.unlimitedchunks") && Math.subtractExact(chunks.get(), chunkLoader.getChunks()) > available) {
                                args.put("needed", String.valueOf(Math.subtractExact(chunks.get(), chunkLoader.getChunks())));
                                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.notEnough, args));
                                break;
                            } else {
                                chunkLoader.setRadius(radius.get());
                                if (plugin.getDataStore().updateChunkLoader(chunkLoader)) {
                                    plugin.getChunkManager().unloadChunkLoader(chunkLoader);
                                    plugin.getChunkManager().loadChunkLoader(chunkLoader);
                                    if (!player.hasPermission("betterchunkloader.chunkloader.unlimitedchunks")) {
                                        if (chunkLoader.isAlwaysOn()) {
                                            playerData.setAlwaysOnChunksAmount(Math.subtractExact(chunks.get(), Math.addExact(available, chunkLoader.getChunks())));
                                        } else {
                                            playerData.setOnlineChunksAmount(Math.subtractExact(chunks.get(), Math.addExact(available, chunkLoader.getChunks())));
                                        }
                                    }
                                    plugin.getDataStore().updatePlayerData(playerData);
                                    plugin.getLogger().info(player.getName() + " edited " + playerData.getName() + "'s chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " radius from " + chunkLoader.getRadius() + " to " + radius.get());
                                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.updateSuccess, args));
                                } else {
                                    plugin.getLogger().info(player.getName() + " failed to edit " + playerData.getName() + "'s chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()) + " radius from " + chunkLoader.getRadius() + " to " + radius.get());
                                    player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunkLoader.updateSuccess, args));
                                }
                            }
                            break;
                        }
                    }
                }
            });
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
