package ca.shadownode.betterchunkloader.sponge.menu;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

public class ChunkLoaderMenu {

    private final BetterChunkLoader plugin;

    public ChunkLoaderMenu(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    public void showMenu(Player player, ChunkLoader chunkLoader) {
        plugin.getDataStore().getPlayerData(chunkLoader.getOwner()).ifPresent((playerData) -> {
            String title = (chunkLoader.getRadius() != -1 ? "BCL: " + playerData.getName() + " Chunks: " + chunkLoader.getChunks() + " " : chunkLoader.isAlwaysOn() ? "Always On Chunk Loader" : "Online Only ChunkLoader");
            if (title.length() > 32) {
                title = title.substring(0, 32);
            }
            Inventory inventory = Inventory.builder()
                    .of(InventoryArchetypes.MENU_ROW)
                    .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(title)))
                    .listener(ClickInventoryEvent.class, createMenuListener(chunkLoader))
                    .build(plugin);
            if (chunkLoader.getRadius() != -1) {
                SlotPos slotPos = new SlotPos(0, 0);
                HashMap<Key, Object> keys = new HashMap<>();
                List<Text> lores = new ArrayList<>();
                lores.add(Text.of("SlotPos: " + slotPos.getX() + "," + slotPos.getY()));
                keys.put(Keys.ITEM_LORE, lores);
                keys.put(Keys.DISPLAY_NAME, Text.of("Remove"));
                addMenuOption(inventory, slotPos, ItemTypes.REDSTONE_TORCH, keys);
            }

            int pos = 2;
            for (int radius = 0; radius < 5;) {
                Integer chunks = Double.valueOf(Math.pow((2 * radius) + 1, 2)).intValue();
                SlotPos slotPos = new SlotPos(pos, 0);
                HashMap<Key, Object> keys = new HashMap<>();
                List<Text> lores = new ArrayList<>();
                lores.add(Text.of("SlotPos: " + slotPos.getX() + "," + slotPos.getY()));
                lores.add(Text.of("Radius: " + radius));
                lores.add(Text.of("Chunks: " + chunks));
                keys.put(Keys.ITEM_LORE, lores);
                keys.put(Keys.DISPLAY_NAME, Text.of((chunkLoader.getRadius() == radius ? "Size: " + (radius + 1) + " [Active]" : "Size: " + (radius + 1))));
                addMenuOption(inventory, slotPos, (chunkLoader.getRadius() == radius ? ItemTypes.POTION : ItemTypes.GLASS_BOTTLE), keys);
                pos++;
                radius++;
            }
            player.openInventory(inventory);
        });
    }

    public void addMenuOption(Inventory inventory, SlotPos slotPos, ItemType icon, HashMap<Key, Object> keys) {
        ItemStack itemStack = ItemStack.builder().itemType(icon).quantity(1).build();
        keys.entrySet().forEach((entry) -> {
            itemStack.offer(entry.getKey(), entry.getValue());
        });
        inventory.query(slotPos).set(itemStack);
    }

    public Consumer<ClickInventoryEvent> createMenuListener(ChunkLoader chunkLoader) {
        return event -> {
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
        };
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
