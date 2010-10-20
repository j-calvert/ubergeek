package edu.washington.cs.ds.jeremyc.paxos;

import java.io.File;
import java.util.ArrayList;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

import edu.washington.cs.ds.jeremyc.paxos.Ledger.LedgerAccessor;
import edu.washington.cs.ds.jeremyc.paxos.Paper.PaperAccessor;

/**
 * Container of Sleepycat JE entities that must be defined.
 * @author jeremyc
 *
 */
public class Storage {

    private LedgerAccessor lea;
    private PaperAccessor  pea;
    
    private Environment env;
    
    public Storage(String dataDir) throws DatabaseException{
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(true);
        File root = new File("data");
        if(! root.exists()){ root.mkdir(); }
        File f = new File("data", dataDir);
        if(! f.exists()){ f.mkdir(); }
        env = new Environment(f, envConfig);
        // Open a transactional entity store.
        //
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        storeConfig.setTransactional(true);
        EntityStore store = new EntityStore(env, "DataStore", storeConfig);
        // Initialize the data access object.
        //
        lea = new LedgerAccessor(store);
        pea = new PaperAccessor(store);
    }
    
    public LedgerAccessor getLedgers(){
        return lea;
    }
    
    public PaperAccessor getPapers(){
        return pea;
    }
    
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        
        Storage s = new Storage("blahdiblah");
        Paper p = new Paper(1l);
        ArrayList<MemberID> q =  new ArrayList<MemberID> ();
        q.add(new MemberID("localhost", 9992));
        p.setQuorum(q);
        s.pea.pKey.put(p);
        Paper r = s.pea.pKey.get(1l);
        p.setQuorum(new ArrayList<MemberID>());
        System.out.println(r.toString());
        
    }
}
