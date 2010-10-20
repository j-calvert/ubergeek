package edu.washington.cs.ds.jeremyc.paxos;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

import com.sleepycat.persist.model.Persistent;

/**
 * The ballot number is comprised of the unique ID of the member initiating the ballot (to gurantee
 * global uniqueness of ballot numbers) and an integer which is incremented as subsequent ballots
 * are issued for the same decree by the same host.
 */
@Persistent
public class BallotNumber implements Writable, Comparable<BallotNumber> {

    private MemberID memberID;

    private int ballotId;

    public BallotNumber() {
    }

    public BallotNumber(MemberID memberID) {
        this.memberID = memberID;
        this.ballotId = 0;
    }

    /**
     * Comparision of ballot numbers is central to the reponse of accept and prepare requests. We
     * guarantee uniqueness of ballot numbers by using the unique memberID belonging to the
     * proposing member. The comparison of ballot numbers considers the ballotId first, so that if a
     * server with a "large" (large in the sense of string comparison) name goes down after issuing
     * a prepare request, a server with a smaller name will be able to usurp the request with a
     * larger ballot number
     * 
     * @param o
     * @return less than 0 iff this less than that. Equal to 0 iff this equal to that.
     */
    public int compareTo(BallotNumber o) {
        if (ballotId == o.ballotId) {
            return memberID.compareTo(o.memberID);
        } else {
            return ballotId - o.ballotId;
        }
    }

    public boolean equals(BallotNumber o) {
        return compareTo(o) == 0;
    }

    public void readFields(DataInput in) throws IOException {
        memberID = new MemberID();
        memberID.readFields(in);
        ballotId = in.readInt();
    }

    public void write(DataOutput out) throws IOException {
        memberID.write(out);
        out.writeInt(ballotId);
    }

    public int getBallotId() {
        return ballotId;
    }

    public void setBallotId(int ballotId) {
        this.ballotId = ballotId;
    }

    public String toString() {
        return memberID.toString() + ":" + ballotId;
    }

    public MemberID getMemberID() {
        return memberID;
    }

    public void setMemberID(MemberID memberID) {
        this.memberID = memberID;
    }

}