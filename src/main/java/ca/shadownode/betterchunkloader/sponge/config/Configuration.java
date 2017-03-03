package ca.shadownode.betterchunkloader.sponge.config;

import ca.shadownode.betterchunkloader.sponge.BetterChunkLoader;
import com.google.common.reflect.TypeToken;
import java.io.File;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public final class Configuration {

    private final BetterChunkLoader plugin;
    private final File configFile;

    private ConfigurationLoader loader;
    private CommentedConfigurationNode config;

    public String selectedDataStore;

    public String mysqlHost;
    public Integer mysqlPort;
    public String mysqlDatabase;
    public String mysqlUsername;
    public String mysqlPassword;

    public String h2File;

    public Integer maxOfflineTime;

    public Integer defaultOnlineChunks;
    public Integer defaultOfflineChunks;

    public Integer maxOnlineChunks;
    public Integer maxOfflineChunks;

    public String clMenuOptionNotValid;
    public String clMenuNotAllowed;
    public String clMenuNotEnoughChunks;
    public String clMenuCreated;
    public String clMenuRemove;
    public String clMenuUpdated;

    public List<String> clClickExists;
    public String clClickNotExists;
    public String clClickNotAllowed;

    public String clBreakMessage;
    public String clBreakOwnerNotify;
    public String clBreakNotAllowed;

    public String cmdNoPermission;
    public String cmdPlayerNotExists;

    public String msgPrefix;
    public List<String> cmdRootUsage;

    public List<String> cmdBalanceSuccess;

    public String cmdChunksAddSuccess;
    public String cmdChunksAddFailure;
    public String cmdChunksSetSuccess;
    public String cmdChunksSetFailure;
    public String cmdChunksUsage;

    public String cmdDeleteSuccess;
    public String cmdDeleteFailure;
    public String cmdDeleteUsage;

    public List<String> cmdInfoSuccess;
    public String cmdInfoFailure;
    public String cmdPurgeSuccess;
    public String cmdReloadSuccess;

    public Configuration(BetterChunkLoader plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.configDir, "config.conf");
        loadConfig();
    }

    public void loadConfig() {
        try {
            loader = HoconConfigurationLoader.builder().setFile(configFile).build();
            saveDefaultConfig();
            config = (CommentedConfigurationNode) loader.load();

            selectedDataStore = config.getNode("DataStore", "Selected").getString();

            mysqlHost = config.getNode("DataStore", "MySQL", "Host").getString();
            mysqlPort = config.getNode("DataStore", "MySQL", "Port").getInt();
            mysqlDatabase = config.getNode("DataStore", "MySQL", "Database").getString();
            mysqlUsername = config.getNode("DataStore", "MySQL", "Username").getString();
            mysqlPassword = config.getNode("DataStore", "MySQL", "Password").getString();

            h2File = config.getNode("DataStore", "H2", "File").getString();

            maxOfflineTime = config.getNode("ChunkLoader, MaxOfflineTime").getInt();

            defaultOnlineChunks = config.getNode("ChunkLoader", "Online", "DefaultChunks").getInt();
            defaultOfflineChunks = config.getNode("ChunkLoader", "Offline", "DefaultChunks").getInt();

            maxOnlineChunks = config.getNode("ChunkLoader", "Online", "MaxChunks").getInt();
            maxOfflineChunks = config.getNode("ChunkLoader", "Offline", "MaxChunks").getInt();

            msgPrefix = config.getNode("Messages", "Prefix").getString();

            clClickExists = config.getNode("Messages", "ChunkLoader", "Click", "Exists").getList(TypeToken.of(String.class));
            clClickNotAllowed = config.getNode("Messages", "ChunkLoader", "Click", "NotAllowed").getString();
            clClickNotExists = config.getNode("Messages", "ChunkLoader", "Click", "NotExists").getString();

            clMenuOptionNotValid = config.getNode("Messages", "ChunkLoader", "Menu", "OptionNotValid").getString();
            clMenuNotAllowed = config.getNode("Messages", "ChunkLoader", "Menu", "NotAllowed").getString();
            clMenuNotEnoughChunks = config.getNode("Messages", "ChunkLoader", "Menu", "NotEnoughChunks").getString();
            clMenuCreated = config.getNode("Messages", "ChunkLoader", "Menu", "Created").getString();
            clMenuRemove = config.getNode("Messages", "ChunkLoader", "Menu", "Remove").getString();
            clMenuUpdated = config.getNode("Messages", "ChunkLoader", "Menu", "Updated").getString();

            clBreakMessage = config.getNode("Messages", "ChunkLoader", "Break", "Message").getString();
            clBreakOwnerNotify = config.getNode("Messages", "ChunkLoader", "Break", "OwnerNotify").getString();

            cmdNoPermission = config.getNode("Messages", "Commands", "NoPermission").getString();
            cmdPlayerNotExists = config.getNode("Messages", "Commands", "PlayerNotExists").getString();

            cmdRootUsage = config.getNode("Messages", "Commands", "BCL", "Usage").getList(TypeToken.of(String.class));
            cmdBalanceSuccess = config.getNode("Messages", "Commands", "BCL", "Balance", "Success").getList(TypeToken.of(String.class));

            cmdChunksUsage = config.getNode("Messages", "Commands", "BCL", "Chunks", "Usage").getString();
            cmdChunksAddSuccess = config.getNode("Messages", "Commands", "BCL", "Chunks", "Add", "Success").getString();
            cmdChunksAddFailure = config.getNode("Messages", "Commands", "BCL", "Chunks", "Add", "Failure").getString();
            cmdChunksSetSuccess = config.getNode("Messages", "Commands", "BCL", "Chunks", "Set", "Success").getString();
            cmdChunksSetFailure = config.getNode("Messages", "Commands", "BCL", "Chunks", "Set", "Failure").getString();

            cmdDeleteUsage = config.getNode("Messages", "Commands", "BCL", "Delete", "Usage").getString();
            cmdDeleteSuccess = config.getNode("Messages", "Commands", "BCL", "Delete", "Success").getString();
            cmdDeleteFailure = config.getNode("Messages", "Commands", "BCL", "Delete", "Failure").getString();

            cmdInfoSuccess = config.getNode("Messages", "Commands", "BCL", "Info", "Success").getList(TypeToken.of(String.class));
            cmdInfoFailure = config.getNode("Messages", "Commands", "BCL", "Info", "Failure").getString();

            cmdPurgeSuccess = config.getNode("Messages", "Commands", "BCL", "Purge", "Success").getString();

            cmdReloadSuccess = config.getNode("Messages", "Commands", "BCL", "Reload", "Success").getString();

        } catch (IOException | ObjectMappingException ex) {
            plugin.getLogger().error("The configuration could not be loaded!", ex);
        }
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            try {
                plugin.configDir.mkdirs();
                configFile.createNewFile();
                config = (CommentedConfigurationNode) loader.load();
                config.getNode("DataStore", "Selected").setValue("H2");
                config.getNode("DataStore", "MySQL", "Host").setValue("127.0.0.1");
                config.getNode("DataStore", "MySQL", "Port").setValue(3306);
                config.getNode("DataStore", "MySQL", "Database").setValue("database");
                config.getNode("DataStore", "MySQL", "Username").setValue("username");
                config.getNode("DataStore", "MySQL", "Password").setValue("password");
                config.getNode("DataStore", "H2", "File").setValue("betterchunkloader.db");
                config.getNode("ChunkLoader", "MaxOfflineTime").setValue(3).setComment("Time in days before player's chunkloaders become inactive.");
                config.getNode("ChunkLoader", "Online", "DefaultChunks").setValue(0);
                config.getNode("ChunkLoader", "Offline", "DefaultChunks").setValue(0);
                config.getNode("ChunkLoader", "Online", "MaxChunks").setValue(250);
                config.getNode("ChunkLoader", "Offline", "MaxChunks").setValue(250);

                config.getNode("Messages", "Prefix").getString();

                config.getNode("Messages", "ChunkLoader", "Click", "Exists").setValue(new TypeToken<List<String>>() {
                }, Arrays.asList(
                        "&8=======[&5ShadowNode&8]=======",
                        "&8| &e/bcl bal",
                        "&8| &e/bcl chunks <add|set|remove> <player> <online|offline>",
                        "&8| &e/bcl delete <player>",
                        "&8| &e/bcl info",
                        "&8| &e/bcl purge",
                        "&8| &e/bcl reload",
                        "&8========================="
                ));
                config.getNode("Messages", "ChunkLoader", "Click", "NotAllowed").setValue("&aIron and Diamond blocks can be converted into chunk loaders. Right click it with a blaze rod.");
                config.getNode("Messages", "ChunkLoader", "Click", "NotExists").setValue("&cYou can't edit other's5 chunk loaders.");

                config.getNode("Messages", "ChunkLoader", "Menu", "OptionNotValid").setValue("&cThat option is not valid.");
                config.getNode("Messages", "ChunkLoader", "Menu", "NotAllowed").setValue("&cYou can't edit other's chunk loaders.");
                config.getNode("Messages", "ChunkLoader", "Menu", "NotEnoughChunks").setValue("&cNot enough chunks Needed: &e{0}&c Available: &e{1}&c.");
                config.getNode("Messages", "ChunkLoader", "Menu", "Created").setValue("&aChunk loader created.");
                config.getNode("Messages", "ChunkLoader", "Menu", "Remove").setValue("&aChunk loader removed.");
                config.getNode("Messages", "ChunkLoader", "Menu", "Updated").setValue("&aChunk loader updated.");
                config.getNode("Messages", "ChunkLoader", "Break", "Message").setValue("&cChunk loader removed.");
                config.getNode("Messages", "ChunkLoader", "Break", "OwnerNotify").setValue("&cYour chunk loader at &e{0}&c has been removed by &e{1}&c.");

                config.getNode("Messages", "Commands", "NoPermission").setValue("&cYou do not have permission for that command.");
                config.getNode("Messages", "Commands", "PlayerNotExists").setValue("&cThat player does not exist.");
                config.getNode("Messages", "Commands", "BCL", "Usage").setValue(new TypeToken<List<String>>() {
                }, Arrays.asList(
                        "First Message",
                        "Second Message"
                ));
                config.getNode("Messages", "Commands", "BCL", "Balance", "Success").setValue(new TypeToken<List<String>>() {
                }, Arrays.asList(
                        "&8=======[&5ShadowNode&8]=======",
                        "&8| &e{0}'s &aBalance: ",
                        "&8|    &aOnline: &e{1}",
                        "&8|    &aOffline: &e{2}",
                        "&8========================="
                ));
                config.getNode("Messages", "Commands", "BCL", "Chunks", "Usage").setValue("&eUsage: /bcl chunks <add|set|remove> <player> <online|offline> <amount>");
                config.getNode("Messages", "Commands", "BCL", "Chunks", "Add", "Success").setValue("&aAdded &e{0}&a {1} chunks to &e{2}'s&a balance!");
                config.getNode("Messages", "Commands", "BCL", "Chunks", "Add", "Failure").setValue("&cCouldn't add &e{0}&c {1} chunks to &e{2}'s&c balance because it would exceed the offline chunks limit of &e{3}&c.");
                config.getNode("Messages", "Commands", "BCL", "Chunks", "Set", "Success").setValue("&aSet &e{0}'s&a {1} chunk balance to &e{2}&a.");
                config.getNode("Messages", "Commands", "BCL", "Chunks", "Set", "Failure").setValue("&cValue can not be less than 0.");
                config.getNode("Messages", "Commands", "BCL", "Delete", "Usage").setValue("&eUsage: /bcl delete <player>");
                config.getNode("Messages", "Commands", "BCL", "Delete", "Success").setValue("&aAll chunk loaders placed by this player have been removed!");
                config.getNode("Messages", "Commands", "BCL", "Delete", "Failure").setValue("&cThis player doesn't have any chunk loader(s).");
                config.getNode("Messages", "Commands", "BCL", "Info", "Success").setValue(new TypeToken<List<String>>() {
                }, Arrays.asList(
                        "&8=======[&5ShadowNode&8]=======",
                        "&8| &aChunkloading Statistics:",
                        "&8|    &aOnline: &e{0}&a loaders loading &e{1}&a chunks.",
                        "&8|    &aOffline: &e{2}&a loaders loading &e{3}&a chunks.",
                        "&8|    &e{4}&a player(s) loading chunks!",
                        "&8========================="
                ));
                config.getNode("Messages", "Commands", "BCL", "Info", "Failure").setValue("&cNo statistics available!");
                config.getNode("Messages", "Commands", "BCL", "Purge", "Success").setValue("&aAll invalid chunk loaders have been removed.");
                config.getNode("Messages", "Commands", "BCL", "Reload", "Success").setValue("&aReloaded plugin configurations and datastores.");

                loader.save(config);
            } catch (IOException | ObjectMappingException ex) {
                plugin.getLogger().error("The default configuration could not be created!", ex);
            }
        }
    }
}
