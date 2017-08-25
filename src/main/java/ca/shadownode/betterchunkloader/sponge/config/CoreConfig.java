package ca.shadownode.betterchunkloader.sponge.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CoreConfig {

    @Setting("Debug")
    public Boolean debug = false;

    @Setting("DataStore")
    public DataStore dataStore = new DataStore();

    @Setting("ChunkLoader")
    public ChunkLoader chunkLoader = new ChunkLoader();

    @ConfigSerializable
    public static class DataStore {

        @Setting("Selected")
        public String selected = "H2";

        @Setting("MYSQL")
        public MYSQL mysql = new MYSQL();

        @Setting("H2")
        public H2 h2 = new H2();

        @ConfigSerializable
        public static class MYSQL {

            @Setting("Hostname")
            public String hostname = "127.0.0.1";

            @Setting("Port")
            public Integer port = 3306;

            @Setting("Database")
            public String database = "database";

            @Setting("Username")
            public String username = "username";

            @Setting("Password")
            public String password = "password";
            
            @Setting("Prefix")
            public String prefix = "bcl_";

        }

        @ConfigSerializable
        public static class H2 {

            @Setting("File")
            public String file = "betterchunkloader.db";
                        
            @Setting("Prefix")
            public String prefix = "bcl_";

        }
    }

    @ConfigSerializable
    public static class ChunkLoader {

        @Setting("Online")
        public Online online = new Online();

        @Setting("AlwaysOn")
        public AlwaysOn alwaysOn = new AlwaysOn();

        @Setting("WandType")
        public String wandType = "minecraft:blaze_rod";

        @ConfigSerializable
        public static class Online {

            @Setting("DefaultOnline")
            public Integer defaultOnline = 0;

            @Setting("MaxOnline")
            public Integer maxOnline = 300;

            @Setting(value = "Expiry", comment = "Max amount in hours the owner can be offline before considering this loader 'Expired'")
            public Integer expiry = 72;

            @Setting("BlockType")
            public String blockType = "minecraft:iron_block";
        }

        @ConfigSerializable
        public static class AlwaysOn {

            @Setting("DefaultAlwaysOn")
            public Integer defaultAlwaysOn = 0;

            @Setting("MaxAlwaysOn")
            public Integer maxAlwaysOn = 300;

            @Setting(value = "Expiry", comment = "Max amount in hours the owner can be offline before considering this loader 'Expired'")
            public Integer expiry = 72;

            @Setting("BlockType")
            public String blockType = "minecraft:diamond_block";

        }
    }
}
