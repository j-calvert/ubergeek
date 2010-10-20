package edu.washington.cs.ds.jeremyc.paxos;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Entries made in indelable ink.  One record per decreeNum.
 * 
 * The member names and descriptions are taken directly from Appendix A1 of Lamport's "Part-time
 * Parliament".
 */
@Entity
public class Ledger {

    /**
     * The number of the decree being maintained by this ledger entry.
     */
    @PrimaryKey
    private long decreeNum;

    /**
     * The decree of this decree number or null if there is nothing written there yet.
     */
    private Decree outcome = null;

    /**
     * The number of the last ballot that this member tried to begin, or null if there was none.
     */
    private BallotNumber lastTried = null;

    /**
     * The number of the last ballot in which this member voted, or null if he never voted.
     */
    private BallotNumber prevBal = null;

    /** The decree for which this member last voted, or blank if never voted. */
    private Decree prevDec = null;

    /**
     * The number of the last ballot in which this member agreed to participate, or null if he has
     * never agreed to participate in a ballot
     */
    private BallotNumber nextBal = null;

    /**
     * This element is not specified in the paper, but we add it to keep a local timestamp of *when*
     * we last tried to initiate a ballot for this decree number, so as we don't retry too often.
     */
    private long timeLastTried = 0;

    private Ledger() {
    }

    public Ledger(long decreeNum) {
        this.decreeNum = decreeNum;
    }

    public long getDecreeNum() {
        return decreeNum;
    }

    public void setDecreeNum(long decreeNum) {
        this.decreeNum = decreeNum;
    }

    public BallotNumber getLastTried() {
        return lastTried;
    }

    public void setLastTried(BallotNumber lastTried) {
        this.lastTried = lastTried;
    }

    public BallotNumber getNextBal() {
        return nextBal;
    }

    public void setNextBal(BallotNumber nextBal) {
        this.nextBal = nextBal;
    }

    public Decree getOutcome() {
        return outcome;
    }

    public void setOutcome(Decree outcome) {
        this.outcome = outcome;
    }

    public BallotNumber getPrevBal() {
        return prevBal;
    }

    public void setPrevBal(BallotNumber prevBal) {
        this.prevBal = prevBal;
    }

    public Decree getPrevDec() {
        return prevDec;
    }

    public void setPrevDec(Decree prevDec) {
        this.prevDec = prevDec;
    }

    public long getTimeLastTried() {
        return timeLastTried;
    }

    public void setTimeLastTried(long timeLastTried) {
        this.timeLastTried = timeLastTried;
    }

    /**
     * Sleepycat JE Persistence implementation for accessing {@link Ledger} entries by decree number.
     */
    public static class LedgerAccessor {

        PrimaryIndex<Long, Ledger> pKey;

        public LedgerAccessor(EntityStore store) throws DatabaseException {
            pKey = store.getPrimaryIndex(Long.class, Ledger.class);
        }

    }

    
}
