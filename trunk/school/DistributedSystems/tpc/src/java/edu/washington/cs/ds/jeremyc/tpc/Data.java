package edu.washington.cs.ds.jeremyc.tpc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.UTF8;
import org.apache.hadoop.io.Writable;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * An example of the kind of data that we might be interesting in storing across multiple cohorts.
 */
@Entity
public class Data implements Writable {

    @PrimaryKey
    private long key;

    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public void readFields(DataInput in) throws IOException {
        key = in.readLong();
        data = UTF8.readString(in);
    }

    public void write(DataOutput out) throws IOException {
        out.writeLong(key);
        UTF8.writeString(out, data);
    }

    public String toString() {
        return "(" + key + ", " + data + ")";
    }

}
