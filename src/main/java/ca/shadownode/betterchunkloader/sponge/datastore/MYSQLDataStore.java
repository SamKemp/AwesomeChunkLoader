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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public final class MYSQLDataStore implements IDataStore {

    private final BetterChunkLoader plugin;

    private final Optional<HikariDataSource> dataSource;
    private final VectorSerializer serializer;

    public MYSQLDataStore(BetterChunkLoader plugin) {
        this.plugin = plugin;
        this.serializer = new VectorSerializer(plugin);
        this.dataSource = getDataSource();
    }

    @Override
    public String getName() {
        return "MYSQL";
    }

    @Override
    public boolean load() {
        if (!dataSource.isPresent()) {
            plugin.getLogger().error("Selected datastore: 'MYSQL' is not avaiable please select another datastore.");
            return false;
        }
        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + plugin.getConfig().getCore().dataStore.mysql.prefix + "chunkloaders ("
                    + "uuid VARCHAR(36) NOT NULL, "
                    + "world VARCHAR(36) NOT NULL, "
                    + "owner VARCHAR(36) NOT NULL, "
                    + "location VARCHAR(1000) NOT NULL, "
                    + "chunk VARCHAR(1000) NOT NULL, "
                    + "r TINYINT(3) UNSIGNED NOT NULL, "
                    + "creation BIGINT(20) NOT NULL, "
                    + "alwaysOn BOOLEAN NOT NULL, "
                    + "UNIQUE KEY uuid (uuid));");
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS bcl_playerdata ("
                    + "username VARCHAR(16) NOT NULL,"
                    + "uuid VARCHAR(36) NOT NULL, "
                    + "lastOnline BIGINT(20) NOT NULL, "
                    + "onlineAmount SMALLINT(6) UNSIGNED NOT NULL, "
                    + "alwaysOnAmount SMALLINT(6) UNSIGNED NOT NULL, "
                    + "UNIQUE KEY uuid (uuid));");
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
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + plugin.getConfig().getCore().dataStore.mysql.prefix + "chunkloaders");
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
            plugin.getLogger().info("MYSQL: Couldn't read all chunk loaders from database.", ex);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChunkLoader> getChunkLoaders(World world) {
        List<ChunkLoader> clList = new ArrayList<>();
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + plugin.getConfig().getCore().dataStore.mysql.prefix + "chunkloaders WHERE world = '" + world.getUniqueId().toString() + "'");
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
            plugin.getLogger().info("MYSQL: Couldn't read chunk loaders from database for world: " + world.getName(), ex);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChunkLoader> getChunkLoadersByType(Boolean isAlwaysOn) {
        List<ChunkLoader> clList = new ArrayList<>();
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + plugin.getConfig().getCore().dataStore.mysql.prefix + "chunkloaders WHERE alwaysOn = '" + isAlwaysOn + "'");
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
            plugin.getLogger().info("MYSQL: Couldn't read chunk loaders from database by type: " + (isAlwaysOn ? "Always On" : "Online Only"), ex);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChunkLoader> getChunkLoadersByOwner(UUID ownerUUID) {
        List<ChunkLoader> clList = new ArrayList<>();
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + plugin.getConfig().getCore().dataStore.mysql.prefix + "chunkloaders WHERE owner = '" + ownerUUID.toString() + "'");
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
            plugin.getLogger().info("MYSQL: Couldn't read chunk loaders from database for owner: " + ownerUUID.toString(), ex);
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
    public Optional<ChunkLoader> getChunkLoaderAt(Location<World> blockLocation) {
        List<ChunkLoader> chunkloaders = getChunkLoaders(blockLocation.getExtent());
        if (chunkloaders == null || chunkloaders.isEmpty()) {
            return Optional.empty();
        }
        for (ChunkLoader chunkLoader : chunkloaders) {
            if (chunkLoader.getLocation().equals(blockLocation.getBlockPosition())) {
                return Optional.of(chunkLoader);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean addChunkLoader(ChunkLoader chunkLoader) {
        try (Connection connection = getConnection()) {
            Optional<String> locationStr = serializer.serialize(chunkLoader.getLocation());
            Optional<String> vectorStr = serializer.serialize(chunkLoader.getChunk());
            if (locationStr.isPresent() && vectorStr.isPresent()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO " + plugin.getConfig().getCore().dataStore.mysql.prefix + "chunkloaders VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                statement.setString(1, chunkLoader.getUniqueId().toString());
                statement.setString(2, chunkLoader.getWorld().toString());
                statement.setString(3, chunkLoader.getOwner().toString());
                statement.setObject(4, locationStr.get());
                statement.setObject(5, vectorStr.get());
                statement.setInt(6, chunkLoader.getRadius());
                statement.setLong(7, chunkLoader.getCreation());
                statement.setBoolean(8, chunkLoader.isAlwaysOn());
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            plugin.getLogger().error("MYSQL: Error adding ChunkLoader", ex);
        }
        return false;
    }

    @Override
    public boolean removeChunkLoader(ChunkLoader chunkLoader) {
        try (Connection connection = getConnection()) {
            return connection.createStatement().executeUpdate("DELETE FROM " + plugin.getConfig().getCore().dataStore.mysql.prefix + "chunkloaders WHERE uuid = '" + chunkLoader.getUniqueId() + "' LIMIT 1") > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("MYSQL: Error removing ChunkLoader.", ex);
        }
        return false;
    }

    @Override
    public boolean removeAllChunkLoaders(UUID playerUUID) {
        try (Connection connection = getConnection()) {
            return connection.createStatement().executeUpdate("DELETE FROM " + plugin.getConfig().getCore().dataStore.mysql.prefix + "chunkloaders WHERE owner = '" + playerUUID.toString() + "'") > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("MYSQL: Error removing ChunkLoaders.", ex);
        }
        return false;
    }

    @Override
    public boolean removeAllChunkLoaders(World world) {
        try (Connection connection = getConnection()) {
            return connection.createStatement().executeUpdate("DELETE FROM " + plugin.getConfig().getCore().dataStore.mysql.prefix + "chunkloaders WHERE owner = '" + world.getUniqueId().toString() + "'") > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("MYSQL: Error removing ChunkLoaders.", ex);
        }
        return false;
    }

    @Override
    public boolean updateChunkLoader(ChunkLoader chunkLoader) {
        try (Connection connection = getConnection()) {
            Optional<String> locationStr = serializer.serialize(chunkLoader.getLocation());
            Optional<String> vectorStr = serializer.serialize(chunkLoader.getChunk());
            if (locationStr.isPresent() && vectorStr.isPresent()) {
                PreparedStatement statement = connection.prepareStatement("REPLACE INTO " + plugin.getConfig().getCore().dataStore.mysql.prefix + "chunkloaders VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                statement.setString(1, chunkLoader.getUniqueId().toString());
                statement.setString(2, chunkLoader.getWorld().toString());
                statement.setString(3, chunkLoader.getOwner().toString());
                statement.setObject(4, locationStr.get());
                statement.setObject(5, vectorStr.get());
                statement.setInt(6, chunkLoader.getRadius());
                statement.setLong(7, chunkLoader.getCreation());
                statement.setBoolean(8, chunkLoader.isAlwaysOn());
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            plugin.getLogger().error("MYSQL: Error updating chunk loader in database.", ex);
        }
        return false;
    }

    @Override
    public Optional<PlayerData> getPlayerData(UUID playerUUID) {
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + plugin.getConfig().getCore().dataStore.mysql.prefix + "playerdata WHERE uuid = '" + playerUUID.toString() + "' LIMIT 1");
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
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + plugin.getConfig().getCore().dataStore.mysql.prefix + "playerdata WHERE username = '" + playerName + "' LIMIT 1");
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
    public boolean updatePlayerData(PlayerData playerData) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("REPLACE INTO " + plugin.getConfig().getCore().dataStore.mysql.prefix + "playerdata VALUES (?, ?, ?, ?, ?)");
            statement.setString(1, playerData.getName());
            statement.setString(2, playerData.getUnqiueId().toString());
            statement.setLong(3, playerData.getLastOnline());
            statement.setInt(4, playerData.getOnlineChunksAmount());
            statement.setInt(5, playerData.getAlwaysOnChunksAmount());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("MYSQL: Error updating player..", ex);
        }
        return false;
    }

    @Override
    public List<PlayerData> getPlayersData() {
        List<PlayerData> playerData = new ArrayList<>();
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + plugin.getConfig().getCore().dataStore.mysql.prefix + "playerdata");
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
            plugin.getLogger().error("MYSQL: Error checking if column exists.", ex);
        }
        return false;
    }

    public Optional<HikariDataSource> getDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setMaximumPoolSize(50);
        ds.setDriverClassName("org.mariadb.jdbc.Driver");
        ds.setJdbcUrl("jdbc:mariadb://"
                + plugin.getConfig().getCore().dataStore.mysql.hostname
                + ":" + plugin.getConfig().getCore().dataStore.mysql.port
                + "/" + plugin.getConfig().getCore().dataStore.mysql.database);
        ds.addDataSourceProperty("user", plugin.getConfig().getCore().dataStore.mysql.username);
        ds.addDataSourceProperty("password", plugin.getConfig().getCore().dataStore.mysql.password);
        ds.setAutoCommit(true);
        return Optional.ofNullable(ds);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.get().getConnection();
    }
}
