package edu.washington.csep576;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class Store {
	private Environment environment;
	private EntityStore entityStore;

	// Our constructor does nothing
	public Store() {
	}

	// The setup() method opens the environment and store
	// for us.
	public void setup(File envHome, boolean readOnly) throws DatabaseException {
		EnvironmentConfig myEnvConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
		myEnvConfig.setReadOnly(readOnly);
		storeConfig.setReadOnly(readOnly);
		// If the environment is opened for write, then we want to be

		// able to create the environment and entity store if
		// they do not exist.
		myEnvConfig.setAllowCreate(!readOnly);
		storeConfig.setAllowCreate(!readOnly);
		// Allow transactions if we are writing to the store.
		myEnvConfig.setTransactional(!readOnly);
		storeConfig.setTransactional(!readOnly);
		// Open the environment and entity store
		environment = new Environment(envHome, myEnvConfig);
		entityStore = new EntityStore(environment, "EntityStore", storeConfig);
	}

	// Return a handle to the entity store
	public EntityStore getEntityStore() {
		return entityStore;
	}

	// Return a handle to the environment
	public Environment getEnv() {
		return environment;
	}

	// Close the store and environment.
	public void close() {
		if (entityStore != null) {
			try {
				entityStore.close();
			} catch (DatabaseException dbe) {
				System.err.println("Error closing store: " + dbe.toString());
				System.exit(-1);
			}
		}
		if (environment != null) {
			try {
				// Finally, close environment.
				environment.close();
			} catch (DatabaseException dbe) {
				System.err.println("Error closing MyDbEnv: " + dbe.toString());
				System.exit(-1);
			}
		}
	}
}