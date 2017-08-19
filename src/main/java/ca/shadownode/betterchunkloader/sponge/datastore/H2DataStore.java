package ca.shadownode.betterchunkloader.sponge.datastore;

import ca.shadownode.betterchunkloader.sponge.data.PlayerData;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.VectorSerializer;
import ca.shadownode.betterchunkloader.sponge.utils.Utilities;
import com.flowpowered.math.vector.Vector3i;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public final class H2DataStore implements IDataStore {

    private final BetterChunkLoader plugin;

    private final Optional<HikariDataSource> dataSource;
    private final VectorSerializer serializer;

    public H2DataStore(BetterChunkLoader plugin) {
        this.plugin = plugin;
        this.serializer = new VectorSerializer(plugin);
        this.dataSource = getDataSource();
    }

    @Override
    public String getName() {
        return "H2";
    }

    @Override
    public boolean load() {
        if (!dataSource.isPresent()) {
            plugin.getLogger().error("Selected datastore: 'H2' is not avaiable please select another datastore.");
            return false;
        }
        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS bcl_chunkloaders ("
                    + "  uuid VARCHAR(36) NOT NULL PRIMARY KEY,"
                    + "  world VARCHAR(36) NOT NULL,"
                    + "  owner VARCHAR(36) NOT NULL,"
                    + "  location VARCHAR(1000) NOT NULL,"
                    + "  chunk VARCHAR(1000) NOT NULL,"
                    + "  r TINYINT(3) UNSIGNED NOT NULL,"
                    + "  creation BIGINT(20) NOT NULL,"
                    + "  alwaysOn BOOLEAN NOT NULL"
                    + ")");
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS bcl_playerdata ("
                    + "username VARCHAR(16) NOT NULL,"
                    + "uuid VARCHAR(36) NOT NULL PRIMARY KEY, "
                    + "lastOnline BIGINT(20) NOT NULL, "
                    + "onlineAmount SMALLINT(6) UNSIGNED NOT NULL, "
                    + "alwaysOnAmount SMALLINT(6) UNSIGNED NOT NULL, "
                    + ");");
            if (hasColumn("bcl_playerdata", "offlineAmount")) {
                connection.createStatement().execute("ALTER TABLE `bcl_playerdata` CHANGE `offlineAmount` `alwaysOnAmount` SMALLINT(6);");
            }
            if (hasColumn("bcl_playerdata", "lastOnline")) {
                connection.createStatement().execute("ALTER TABLE `bcl_playerdata` CHANGE `lastOnline` `lastOnline` BIGINT(20);");
            }
            if (hasColumn("bcl_chunkloaders", "lastOnline")) {
                connection.createStatement().execute("ALTER TABLE `bcl_chunkloaders` CHANGE `creation` `creation` BIGINT(20);");
            }
        } catch (SQLException ex) {
            plugin.getLogger().error("Unable to create tables", ex);
            return false;
        }
        return true;
    }

    @Override
    public List<ChunkLoader> getChunkLoaders() {
        List<ChunkLoader> clList = new ArrayList<>();
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM bcl_chunkloaders");
            while (rs.next()) {
                Optional<Vector3i> optLocation = serializer.deserialize(rs.getString("location"));
                Optional<Vector3i> optVector = serializer.deserialize(rs.getString("chunk"));
                if (optLocation.isPresent() && optVector.isPresent()) {
                    ChunkLoader chunkLoader = new ChunkLoader(
                            UUID.fromString(rs.getString("uuid")),
                            UUID.fromString(rs.getString("world")),
                            UUID.fromString(rs.getString("owner")),
                            optLocation.get(),
                            optVector.get(),
                            rs.getInt("r"),
                            rs.getLong("creation"),
                            rs.getBoolean("alwaysOn")
                    );
                    clList.add(chunkLoader);
                }
            }
            return clList;
        } catch (SQLException ex) {
            plugin.getLogger().info("H2: Couldn't read chunk loaders data from H2 database.", ex);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChunkLoader> getChunkLoaders(World world) {
        List<ChunkLoader> clList = new ArrayList<>();
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM bcl_chunkloaders WHERE world = '" + world.getUniqueId().toString() + "'");
            while (rs.next()) {
                Optional<Vector3i> optLocation = serializer.deserialize(rs.getString("location"));
                Optional<Vector3i> optVector = serializer.deserialize(rs.getString("chunk"));
                if (optLocation.isPresent() && optVector.isPresent()) {
                    ChunkLoader chunkLoader = new ChunkLoader(
                            UUID.fromString(rs.getString("uuid")),
                            UUID.fromString(rs.getString("world")),
                            UUID.fromString(rs.getString("owner")),
                            optLocation.get(),
                            optVector.get(),
                            rs.getInt("r"),
                            rs.getLong("creation"),
                            rs.getBoolean("alwaysOn")
                    );
                    clList.add(chunkLoader);
                }
            }
            return clList;
        } catch (SQLException ex) {
            plugin.getLogger().info("H2: Couldn't read chunk loaders data from H2 database.", ex);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChunkLoader> getChunkLoadersByType(Boolean isAlwaysOn) {
        List<ChunkLoader> clList = new ArrayList<>();
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM bcl_chunkloaders WHERE alwaysOn = '" + isAlwaysOn + "'");
            while (rs.next()) {
                Optional<Vector3i> optLocation = serializer.deserialize(rs.getString("location"));
                Optional<Vector3i> optVector = serializer.deserialize(rs.getString("chunk"));
                if (optLocation.isPresent() && optVector.isPresent()) {
                    ChunkLoader chunkLoader = new ChunkLoader(
                            UUID.fromString(rs.getString("uuid")),
                            UUID.fromString(rs.getString("world")),
                            UUID.fromString(rs.getString("owner")),
                            optLocation.get(),
                            optVector.get(),
                            rs.getInt("r"),
                            rs.getLong("creation"),
                            rs.getBoolean("alwaysOn")
                    );
                    clList.add(chunkLoader);
                }
            }
            return clList;
        } catch (SQLException ex) {
            plugin.getLogger().info("H2: Couldn't read chunk loaders data from H2 database.", ex);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChunkLoader> getChunkLoadersByOwner(UUID ownerUUID) {
        List<ChunkLoader> clList = new ArrayList<>();
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM bcl_chunkloaders WHERE owner = '" + ownerUUID.toString() + "'");
            while (rs.next()) {
                Optional<Vector3i> optLocation = serializer.deserialize(rs.getString("location"));
                Optional<Vector3i> optVector = serializer.deserialize(rs.getString("chunk"));
                if (optLocation.isPresent() && optVector.isPresent()) {
                    ChunkLoader chunkLoader = new ChunkLoader(
                            UUID.fromString(rs.getString("uuid")),
                            UUID.fromString(rs.getString("world")),
                            UUID.fromString(rs.getString("owner")),
                            optLocation.get(),
                            optVector.get(),
                            rs.getInt("r"),
                            rs.getLong("creation"),
                            rs.getBoolean("alwaysOn")
                    );
                    clList.add(chunkLoader);
                }
            }
            return clList;
        } catch (SQLException ex) {
            plugin.getLogger().info("H2: Couldn't read chunk loaders data from H2 database.", ex);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChunkLoader> getChunkLoadersAt(World world, Vector3i chunk) {
        List<ChunkLoader> chunkloaders = new ArrayList<>();
        getChunkLoaders(world).stream().filter((chunkLoader) -> (chunkLoader.getChunk().equals(chunk))).forEachOrdered((chunkLoader) -> {
            chunkloaders.add(chunkLoader);
        });
        return chunkloaders;
    }

    @Override
    public Optional<ChunkLoader> getChunkLoaderAt(Location<World> location) {
        List<ChunkLoader> chunkloaders = getChunkLoaders(location.getExtent());
        if (chunkloaders == null || chunkloaders.isEmpty()) {
            return Optional.empty();
        }
        for (ChunkLoader chunkLoader : chunkloaders) {
            if (chunkLoader.getLocation().equals(location.getBlockPosition())) {
                return Optional.of(chunkLoader);
            }
        }
        return Optional.empty();
    }

    @Override
    public void addChunkLoader(ChunkLoader chunkLoader) {
        try (Connection connection = getConnection()) {
            Optional<String> locationStr = serializer.serialize(chunkLoader.getLocation());
            Optional<String> vectorStr = serializer.serialize(chunkLoader.getChunk());
            if (locationStr.isPresent() && vectorStr.isPresent()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO bcl_chunkloaders VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                statement.setString(1, chunkLoader.getUniqueId().toString());
                statement.setString(2, chunkLoader.getWorld().toString());
                statement.setString(3, chunkLoader.getOwner().toString());
                statement.setObject(4, locationStr.get());
                statement.setObject(5, vectorStr.get());
                statement.setInt(6, chunkLoader.getRadius());
                statement.setLong(7, chunkLoader.getCreation());
                statement.setBoolean(8, chunkLoader.isAlwaysOn());
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error adding ChunkLoader", ex);
        }
    }

    @Override
    public void removeChunkLoader(ChunkLoader chunkLoader) {
        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate("DELETE FROM bcl_chunkloaders WHERE uuid = '" + chunkLoader.getUniqueId() + "' LIMIT 1");
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error removing ChunkLoader.", ex);
        }
    }

    @Override
    public void updateChunkLoader(ChunkLoader chunkLoader) {
        try (Connection connection = getConnection()) {
            Optional<String> locationStr = serializer.serialize(chunkLoader.getLocation());
            Optional<String> vectorStr = serializer.serialize(chunkLoader.getChunk());
            if (locationStr.isPresent() && vectorStr.isPresent()) {
                PreparedStatement statement = connection.prepareStatement("MERGE INTO bcl_chunkloaders VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                statement.setString(1, chunkLoader.getUniqueId().toString());
                statement.setString(2, chunkLoader.getWorld().toString());
                statement.setString(3, chunkLoader.getOwner().toString());
                statement.setObject(4, locationStr.get());
                statement.setObject(5, vectorStr.get());
                statement.setInt(6, chunkLoader.getRadius());
                statement.setLong(7, chunkLoader.getCreation());
                statement.setBoolean(8, chunkLoader.isAlwaysOn());
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error updating chunk loader in database.", ex);
        }
    }

    @Override
    public void removeAllChunkLoaders(UUID owner) {
        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate("DELETE FROM bcl_chunkloaders WHERE owner = '" + owner.toString() + "'");
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error removing ChunkLoaders.", ex);
        }
    }

    @Override
    public void removeAllChunkLoaders(World world) {
        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate("DELETE FROM bcl_chunkloaders WHERE world = '" + world.getUniqueId().toString() + "'");
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error removing ChunkLoaders.", ex);
        }
    }

    @Override
    public Optional<PlayerData> getPlayerData(UUID playerUUID) {
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM bcl_playerdata WHERE uuid = '" + playerUUID.toString() + "' LIMIT 1");
            if (rs.next()) {
                return Optional.ofNullable(new PlayerData(
                        rs.getString("username"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getLong("lastOnline"),
                        rs.getInt("onlineAmount"),
                        rs.getInt("alwaysOnAmount")
                ));
            }
            Optional<String> playerName = Utilities.getPlayerName(playerUUID);
            if (playerName.isPresent()) {
                return Optional.ofNullable(new PlayerData(
                        playerName.get(),
                        playerUUID
                ));
            }
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error refreshing Player data.", ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<PlayerData> getPlayerData(String playerName) {
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM bcl_playerdata WHERE username = '" + playerName + "' LIMIT 1");
            if (rs.next()) {
                return Optional.ofNullable(new PlayerData(
                        rs.getString("username"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getLong("lastOnline"),
                        rs.getInt("onlineAmount"),
                        rs.getInt("alwaysOnAmount")
                ));
            }
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error getting player data for: " + playerName, ex);
        }
        return Optional.empty();
    }

    @Override
    public void updatePlayerData(PlayerData playerData) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("MERGE INTO bcl_playerdata VALUES (?, ?, ?, ?, ?)");
            statement.setString(1, playerData.getName());
            statement.setString(2, playerData.getUnqiueId().toString());
            statement.setLong(3, playerData.getLastOnline());
            statement.setInt(4, playerData.getOnlineChunksAmount());
            statement.setInt(5, playerData.getAlwaysOnChunksAmount());
            statement.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error updating player data for: " + playerData.getName(), ex);
        }
    }

    @Override
    public List<PlayerData> getPlayersData() {
        List<PlayerData> playerData = new ArrayList<>();
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM bcl_playerdata");
            while (rs.next()) {
                playerData.add(new PlayerData(
                        rs.getString("username"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getLong("lastOnline"),
                        rs.getInt("onlineAmount"),
                        rs.getInt("alwaysOnAmount")
                ));
            }
            return playerData;
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error getting all player data.", ex);
        }
        return new ArrayList<>();
    }

    public boolean hasColumn(String tableName, String columnName) {
        try (Connection connection = getConnection()) {
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getColumns(null, null, tableName, columnName);
            return rs.next();
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error checking if column exists.", ex);
        }
        return false;
    }

    public Optional<HikariDataSource> getDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setMaximumPoolSize(50);
        ds.setDriverClassName("org.h2.Driver");
        ds.setJdbcUrl("jdbc:h2://" + new File(plugin.configDir, plugin.getConfig().getCore().dataStore.h2.file).getAbsolutePath());
        ds.setAutoCommit(true);
        return Optional.ofNullable(ds);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.get().getConnection();
    }
}
