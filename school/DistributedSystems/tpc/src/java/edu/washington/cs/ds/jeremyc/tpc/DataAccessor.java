package edu.washington.cs.ds.jeremyc.tpc;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

/**
 * Mid-level API for accessing Data entries.
 */
public class DataAccessor {

    PrimaryIndex<Long, Data> pKey;

    public DataAccessor(EntityStore store) throws DatabaseException {
        pKey = store.getPrimaryIndex(Long.class, Data.class);
    }

}
