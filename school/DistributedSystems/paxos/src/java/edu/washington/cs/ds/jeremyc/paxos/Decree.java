package edu.washington.cs.ds.jeremyc.paxos;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.UTF8;
import org.apache.hadoop.io.Writable;

import com.sleepycat.persist.model.Persistent;

/**
 * A small sample of something that we might store in a distributed fashion.
 * 
 * In this case, the distributed store would contain objects keyed by "key" with value "value".
 */
@Persistent
public class Decree implements Writable {

    private String key = "";

    private String value = "";

    public Decree() {
    }

    public void readFields(DataInput in) throws IOException {
        key = UTF8.readString(in);
        value = UTF8.readString(in);
    }

    public void write(DataOutput out) throws IOException {
        UTF8.writeString(out, key);
        UTF8.writeString(out, value);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public String toString() {
        return "(" + key + ", " + value + ")"; 
    }

}
