package edu.washington.cs.ds.jeremyc.paxos;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/**
 * The general object conveyed in communication.
 * 
 * There is roughly one message type per request.
 * 
 * The set of non-null message payload elements is determined by the type.
 */
public class Message implements Writable {

    private Type type = Type.NULL;

    private long decreeNum = -1;

    private BallotNumber ballotNumber = null;

    private Decree decree = null;

    private MemberID voter = null;

    public void write(DataOutput out) throws IOException {
        out.writeByte(type.toByte());
        out.writeLong(decreeNum);
        switch (type) {
        case NEW:
            decree.write(out);
        case NULL:
            break;
        case NEXT_BALLOT:
            ballotNumber.write(out);
            break;
        case LAST_VOTE:
            ballotNumber.write(out);
            decree.write(out);
            voter.write(out);
            break;
        case BEGIN_BALLOT:
            ballotNumber.write(out);
            decree.write(out);
            break;
        case VOTED:
            ballotNumber.write(out);
            voter.write(out);
            break;
        case SUCCESS:
            decree.write(out);
            break;
        default:
            throw new IOException("Unidentifyable message type: " + type.toString());
        }
    }

    public void readFields(DataInput in) throws IOException {
        type = Type.fromByte(in.readByte());
        decreeNum = in.readLong();
        switch (type) {
        case NEW:
            decree = new Decree();
            decree.readFields(in);
        case NULL:
            break;
        case NEXT_BALLOT:
            ballotNumber = new BallotNumber();
            ballotNumber.readFields(in);
            break;
        case LAST_VOTE:
            ballotNumber = new BallotNumber();
            ballotNumber.readFields(in);
            decree = new Decree();
            decree.readFields(in);
            voter = new MemberID();
            voter.readFields(in);
            break;
        case BEGIN_BALLOT:
            ballotNumber = new BallotNumber();
            ballotNumber.readFields(in);
            decree = new Decree();
            decree.readFields(in);
            break;
        case VOTED:
            ballotNumber = new BallotNumber();
            ballotNumber.readFields(in);
            voter = new MemberID();
            voter.readFields(in);
            break;
        case SUCCESS:
            decree = new Decree();
            decree.readFields(in);
            break;
        default:
            throw new IOException("Unidentifyable message type: " + type.toString());
        }
    }

    /**
     * A message type enumeration, corresponding to the messages described in Appendix A1 of
     * Lamport's "Part-time Parliament" with the following two exceptions:
     * 
     * The NEW message is used to ask a parliament member to initiate a new decree. It's not a
     * message that's part of the consensus protocol.
     * 
     * The NULL message type is used in many response messages (since response is not a part of the
     * protocol) to indicate that no non-trivial information is being conveyed, but is sent in order to
     * conform to the IPC client/server request/response API.
     */
    public static enum Type {
        NEW, NULL, NEXT_BALLOT, LAST_VOTE, BEGIN_BALLOT, VOTED, SUCCESS;
        public static Type fromByte(byte b) throws IOException {
            if (b >= values().length) {
                throw new IOException("Tried to set value at position " + b + " from "
                        + values().toString());
            }
            return values()[b];
        }

        public byte toByte() {
            return (byte) ordinal();
        }

    }

    public BallotNumber getBallotNumber() {
        return ballotNumber;
    }

    public void setBallotNumber(BallotNumber ballotNumber) {
        this.ballotNumber = ballotNumber;
    }

    public Decree getDecree() {
        return decree;
    }

    public void setDecree(Decree decree) {
        this.decree = decree;
    }

    public long getDecreeNum() {
        return decreeNum;
    }

    public void setDecreeNum(long decreeNum) {
        this.decreeNum = decreeNum;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public MemberID getVoter() {
        return voter;
    }

    public void setVoter(MemberID voter) {
        this.voter = voter;
    }

    public String toString() {
        return type.toString() + " decree:" + decreeNum + " ballot:"
                + (ballotNumber != null ? ballotNumber.toString() : "NULL") + " decree:"
                + (decree != null ? decree.toString() : "NULL") + " voter:"
                + (voter != null ? voter.toString() : "NULL");
    }

}
