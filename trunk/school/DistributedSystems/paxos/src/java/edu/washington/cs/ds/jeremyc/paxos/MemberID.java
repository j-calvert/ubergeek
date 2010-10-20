package edu.washington.cs.ds.jeremyc.paxos;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.UTF8;
import org.apache.hadoop.io.Writable;

import com.sleepycat.persist.model.Persistent;

/**
 * The ID of a member of the paxos parliament. Identified uniquely by hostname and port (and hence,
 * assumes this is unique).
 * 
 * Also used in formulation of {@link BallotNumber}
 */
@Persistent
public class MemberID implements Comparable, Writable {

    private String hostname;

    private int port;

    public MemberID() {
    }

    public MemberID(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int compareTo(Object o) {
        if (o instanceof MemberID) {
            MemberID mid = (MemberID) o;
            if (hostname.compareTo(mid.hostname) == 0) {
                return port - mid.port;
            } else {
                return hostname.compareTo(mid.hostname);
            }
        } else {
            return -1;
        }
    }

    public boolean equals(Object o) {
        if (this.compareTo(o) == 0) {
            return true;
        }
        return false;
    }

    public void readFields(DataInput in) throws IOException {
        hostname = UTF8.readString(in);
        port = in.readInt();
    }

    public void write(DataOutput out) throws IOException {
        UTF8.writeString(out, hostname);
        out.writeInt(port);
    }

    public String toDirString() {
        return hostname + "." + port;
    }

    public String toString() {
        return hostname + ":" + port;
    }

}
