package ca.shadownode.betterchunkloader.sponge.dataStore;

import ca.shadownode.betterchunkloader.sponge.data.PlayerData;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.ChunkLoader;
import ca.shadownode.betterchunkloader.sponge.data.LocationSerializer;
import com.flowpowered.math.vector.Vector3i;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public final class MYSQLDataStore extends AHashMapDataStore {

    private final Optional<HikariDataSource> dataSource;
    private final LocationSerializer locationSerializer;

    public MYSQLDataStore(BetterChunkLoader plugin) {
        super(plugin);
        this.locationSerializer = new LocationSerializer(plugin);
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
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS bcl_chunkloaders ("
                    + "  uuid VARCHAR(36) NOT NULL,"
                    + "  world VARCHAR(36) NOT NULL,"
                    + "  owner VARCHAR(36) NOT NULL,"
                    + "  location VARCHAR(1000) NOT NULL,"
                    + "  chunk VARCHAR(1000) NOT NULL,"
                    + "  r TINYINT(3) UNSIGNED NOT NULL,"
                    + "  creation TIMESTAMP NOT NULL,"
                    + "  alwaysOn BOOLEAN NOT NULL, UNIQUE KEY uuid (uuid)"
                    + ")");
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS bcl_playerdata ("
                    + "username VARCHAR(16) NOT NULL,"
                    + "uuid VARCHAR(36) NOT NULL, "
                    + "lastOnline TIMESTAMP NOT NULL, "
                    + "onlineAmount SMALLINT(6) UNSIGNED NOT NULL, "
                    + "alwaysOnAmount SMALLINT(6) UNSIGNED NOT NULL, "
                    + "UNIQUE KEY uuid (uuid));");
            if (hasColumn("bcl_playerdata", "offlineAmount")) {
                connection.createStatement().execute("ALTER TABLE `bcl_playerdata` CHANGE `offlineAmount` `alwaysOnAmount` SMALLINT(6);");
            }
            connection.close();
        } catch (SQLException ex) {
            plugin.getLogger().error("Unable to create tables", ex);
            return false;
        }
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM bcl_chunkloaders");
            while (rs.next()) {
                Optional<Location<World>> optLocation = locationSerializer.deserializeLocation(rs.getString("location"));
                Optional<Vector3i> optVector = locationSerializer.deserializeVector(rs.getString("chunk"));
                if (optLocation.isPresent() && optVector.isPresent()) {
                    ChunkLoader chunkLoader = new ChunkLoader(
                            UUID.fromString(rs.getString("uuid")),
                            UUID.fromString(rs.getString("world")),
                            UUID.fromString(rs.getString("owner")),
                            optLocation.get(),
                            optVector.get(),
                            rs.getInt("r"),
                            rs.getTimestamp("creation"),
                            rs.getBoolean("alwaysOn")
                    );
                    List<ChunkLoader> clList = this.chunkLoaders.get(chunkLoader.getWorld());
                    if (clList == null) {
                        clList = new ArrayList<>();
                    }
                    clList.add(chunkLoader);
                    chunkLoaders.put(chunkLoader.getWorld(), clList);
                }
            }
            connection.close();
        } catch (SQLException ex) {
            plugin.getLogger().info("Couldn't read chunk loaders data from MySQL server.", ex);
            return false;
        }
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM bcl_playerdata");
            while (rs.next()) {
                PlayerData playerData = new PlayerData(
                        rs.getString("username"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getTimestamp("lastOnline"),
                        rs.getInt("onlineAmount"),
                        rs.getInt("alwaysOnAmount")
                );
                this.playersData.put(playerData.getUnqiueId(), playerData);
            }
            connection.close();
        } catch (SQLException ex) {
            plugin.getLogger().info("Couldn't read players data from MySQL server.", ex);
            return false;
        }
        return true;
    }

    @Override
    public Optional<ChunkLoader> getChunkLoaderAt(Location<World> blockLocation) {
        List<ChunkLoader> chunkloaders = this.chunkLoaders.get(blockLocation.getExtent().getUniqueId());
        if (chunkloaders == null || chunkloaders.isEmpty()) {
            return Optional.empty();
        }
        for (ChunkLoader chunkLoader : chunkloaders) {
            if (chunkLoader.getLocation().equals(blockLocation)) {
                return Optional.of(chunkLoader);
            }
        }
        return Optional.empty();
    }

    @Override
    public void addChunkLoader(ChunkLoader chunkLoader) {
        super.addChunkLoader(chunkLoader);
        try (Connection connection = getConnection()) {
            Optional<String> locationStr = locationSerializer.serializeLocation(chunkLoader.getLocation());
            Optional<String> vectorStr = locationSerializer.serializeVector(chunkLoader.getChunk());
            if (locationStr.isPresent() && vectorStr.isPresent()) {
                PreparedStatement statement = connection.prepareStatement("REPLACE INTO bcl_chunkloaders VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                statement.setString(1, chunkLoader.getUniqueId().toString());
                statement.setString(2, chunkLoader.getWorld().toString());
                statement.setString(3, chunkLoader.getOwner().toString());
                statement.setObject(4, locationStr.get());
                statement.setObject(5, vectorStr.get());
                statement.setInt(6, chunkLoader.getRadius());
                statement.setTimestamp(7, chunkLoader.getCreation());
                statement.setBoolean(8, chunkLoader.isAlwaysOn());
                statement.executeUpdate();
            }
            connection.close();
        } catch (SQLException ex) {
            plugin.getLogger().error("MYSQL: Error adding ChunkLoader", ex);
        }
    }

    @Override
    public void removeChunkLoader(ChunkLoader chunkLoader) {
        super.removeChunkLoader(chunkLoader);
        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate("DELETE FROM bcl_chunkloaders WHERE uuid = '" + chunkLoader.getUniqueId() + "' LIMIT 1");
            connection.close();
        } catch (SQLException ex) {
            plugin.getLogger().error("MYSQL: Error removing ChunkLoader.", ex);
        }
    }

    @Override
    public void removeAllChunkLoaders(UUID playerUUID) {
        super.removeAllChunkLoaders(playerUUID);
        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate("DELETE FROM bcl_chunkloaders WHERE owner = '" + playerUUID.toString() + "'");
            connection.close();
        } catch (SQLException ex) {
            plugin.getLogger().error("MYSQL: Error removing ChunkLoaders.", ex);
        }
    }

    @Override
    public void removeAllChunkLoaders(World world) {
        super.removeAllChunkLoaders(world);
        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate("DELETE FROM bcl_chunkloaders WHERE owner = '" + world.getUniqueId().toString() + "'");
            connection.close();
        } catch (SQLException ex) {
            plugin.getLogger().error("MYSQL: Error removing ChunkLoaders.", ex);
        }
    }

    @Override
    public void setChunkLoaderRadius(ChunkLoader chunkLoader, Integer radius) {
        super.setChunkLoaderRadius(chunkLoader, radius);
        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate("UPDATE bcl_chunkloaders SET r = " + radius + " WHERE uuid = '" + chunkLoader.getUniqueId() + "' LIMIT 1");
            connection.close();
        } catch (SQLException ex) {
            plugin.getLogger().error("MYSQL: Error changing ChunkLoader range.", ex);
        }
    }

    @Override
    public void updatePlayerData(PlayerData playerData) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("REPLACE INTO bcl_playerdata VALUES (?, ?, ?, ?, ?)");
            statement.setString(1, playerData.getName());
            statement.setString(2, playerData.getUnqiueId().toString());
            statement.setTimestamp(3, playerData.getLastOnline());
            statement.setInt(4, playerData.getOnlineChunksAmount());
            statement.setInt(5, playerData.getAlwaysOnChunksAmount());
            statement.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().error("MYSQL: Error updating player..", ex);
        }
    }

    @Override
    public void refreshPlayer(UUID uuid) {
        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM bcl_playerdata WHERE uuid = '" + uuid.toString() + "' LIMIT 1");
            while (rs.next()) {
                Optional<PlayerData> playerData = this.getOrCreatePlayerData(uuid);
                if (playerData.isPresent()) {
                    playerData.get().setName(rs.getString("username"));
                    playerData.get().setUniqueId(UUID.fromString(rs.getString("uuid")));
                    playerData.get().setLastOnline(rs.getTimestamp("lastOnline"));
                    playerData.get().setOnlineChunksAmount(rs.getInt("onlineAmount"));
                    playerData.get().setAlwaysOnChunksAmount(rs.getInt("alwaysOnAmount"));
                }
            }
            connection.close();
        } catch (SQLException ex) {
            plugin.getLogger().error("MYSQL: Error refreshing player.", ex);
        }
    }

    public boolean hasColumn(String tableName, String columnName) {
        try (Connection connection = getConnection()) {
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getColumns(null, null, tableName, columnName);
            connection.close();
            return rs.next();
        } catch (SQLException ex) {
            plugin.getLogger().error("MYSQL: Error checking if column exists.", ex);
        }
        return false;
    }

    public Optional<HikariDataSource> getDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setMaximumPoolSize(100);
        ds.setDriverClassName("org.mariadb.jdbc.Driver");
        ds.setJdbcUrl("jdbc:mariadb://"
                + plugin.getConfig().getCore().dataStore.mysql.hostname
                + ":" + plugin.getConfig().getCore().dataStore.mysql.port
                + "/" + plugin.getConfig().getCore().dataStore.mysql.database);
        ds.addDataSourceProperty("user", plugin.getConfig().getCore().dataStore.mysql.username);
        ds.addDataSourceProperty("password", plugin.getConfig().getCore().dataStore.mysql.password);
        ds.setAutoCommit(false);
        return Optional.ofNullable(ds);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.get().getConnection();
    }
}
