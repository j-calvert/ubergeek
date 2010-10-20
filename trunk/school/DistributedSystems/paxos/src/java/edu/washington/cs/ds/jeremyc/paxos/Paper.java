package edu.washington.cs.ds.jeremyc.paxos;

import java.util.ArrayList;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Names and descriptions taken directly from Appendix A1 of Lamport's
 * "Part-time Parliament", items which are written on a piece of paper.
 */
@Entity
public class Paper {

    @PrimaryKey
    private long decreeNum = -1;

    /**
     * The status of the decree numbered decreeNum
     */
    private Status status = Status.IDLE;

    /**
     * The set of votes received in LastVote messages for the current ballot (the one with ballot
     * number lastTried).
     */
    private ArrayList<MemberID> prevVotes = new ArrayList<MemberID>();

    /**
     * If status = polling , then the set of priests forming the quorum of the current ballot;
     * otherwise, meaningless.
     */
    private ArrayList<MemberID> quorum = new ArrayList<MemberID>();

    /**
     * If status = polling , then the set of quorum members from whom p has received Voted messages
     * in the current ballot; otherwise, meaning less.
     */
    private ArrayList<MemberID> voters = new ArrayList<MemberID>();

    /**
     * If status = idle or trying, the decree that this member will initiate if no other decree has
     * been voted upon If status = polling , then the decree of the current ballot.
     */
    private Decree decree = new Decree();

    private Paper() {
    }

    public Paper(long decreeNum) {
        this.decreeNum = decreeNum;
    }

    public long getDecreeNum() {
        return decreeNum;
    }

    public void setDecreeNum(long decreeNum) {
        this.decreeNum = decreeNum;
    }

    public Decree getDecree() {
        return decree;
    }

    public void setDecree(Decree decree) {
        this.decree = decree;
    }

    @Persistent
    public static enum Status {
        /**
         * Not conducting or trying to begin a ballot
         */
        IDLE,
        /**
         * Trying to begin ballot number lastTried
         */
        TRYING, 
        /**
         * Now conducting ballot number lastTried
         */
        POLLING;
    }

    public ArrayList<MemberID> getPrevVotes() {
        return prevVotes;
    }

    public void setPrevVotes(ArrayList<MemberID> prevVotes) {
        this.prevVotes = prevVotes;
    }

    public ArrayList<MemberID> getQuorum() {
        return quorum;
    }

    public void setQuorum(ArrayList<MemberID> quorum) {
        this.quorum = quorum;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ArrayList<MemberID> getVoters() {
        return voters;
    }

    public void setVoters(ArrayList<MemberID> voters) {
        this.voters = voters;
    }


    /**
     * Sleepycat JE Persistence implementation for accessing {@link Paper} entries by decree number.
     */
    public static class PaperAccessor {

        PrimaryIndex<Long, Paper> pKey;

        public PaperAccessor(EntityStore store) throws DatabaseException {
            pKey = store.getPrimaryIndex(Long.class, Paper.class);
        }
        
    }

    
}
