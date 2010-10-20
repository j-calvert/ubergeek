package edu.washington.cs.ds.jeremyc.tpc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/**
 * A simple message object that is passed between coordinator and cohort.
 *
 */
public class Message implements Writable {

    private Type type;

    private long serialNum;
    
    private Data data;

    public void write(DataOutput out) throws IOException {
        out.writeByte(type.toByte());
        out.writeLong(serialNum);
        if (type == Type.PREPARE) {
            data.write(out);
        }

    }

    public void readFields(DataInput in) throws IOException {
        type = Type.fromByte(in.readByte());
        serialNum = in.readLong();
        if (type == Type.PREPARE) {
            data = new Data();
            data.readFields(in);
        }
    }

    public static enum Type {
        PREPARE, AGREEMENT, COMMIT, ABORT, ACKNOWLEDGE;
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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
    
    public String toString(){
        String s = type.toString() + " #" + serialNum;
        if(type == Type.PREPARE){
            s += " : " + data.toString();
        }
        return s;
    }

    public long getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(long serialNum) {
        this.serialNum = serialNum;
    }

}
