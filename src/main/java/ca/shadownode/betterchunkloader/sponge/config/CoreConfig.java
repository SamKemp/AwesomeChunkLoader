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
            
        }

        @ConfigSerializable
        public static class H2 {

            @Setting("File")
            public String file = "betterchunkloader.db";

        }
    }

    @ConfigSerializable
    public static class ChunkLoader {

        @Setting("DefaultOnline")
        public Integer defaultOnline = 0;
        
        @Setting("MaxOnline")
        public Integer maxOnline = 300;
        
        @Setting("DefaultAlwaysOn")
        public Integer defaultAlwaysOn = 0;
        
        @Setting("MaxAlwaysOn")
        public Integer maxAlwaysOn = 300;
        
        @Setting(value = "Expiry", comment = "Max amount in hours the owner can be offline before considering this loader 'Expired'")
        public Integer expiry = 72;
    }
}
