package edu.washington.cs.ds.jeremyc.tpc;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

/**
 * Low level API for storing data in Sleepycat's Berkeley Database, Java Edition.
 * 
 * The environment object brokers transactions that provide ACID interaction with the data store.
 */
public class Storage {

    private Environment env;

    private DataAccessor da;

    public Storage(String dataDir) throws DatabaseException {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(true);
        File root = new File("data");
        if (!root.exists()) {
            root.mkdir();
        }
        File f = new File("data", dataDir);
        if (!f.exists()) {
            f.mkdir();
        }
        env = new Environment(f, envConfig);
        // Open a transactional entity store.
        //
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        storeConfig.setTransactional(true);
        EntityStore store = new EntityStore(env, "DataStore", storeConfig);
        // Initialize the data access object.
        //
        da = new DataAccessor(store);
    }

    public Environment getEnv() {
        return env;
    }

    public DataAccessor getDa() {
        return da;
    }

}
