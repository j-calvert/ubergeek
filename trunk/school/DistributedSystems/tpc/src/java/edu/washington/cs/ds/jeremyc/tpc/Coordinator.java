package edu.washington.cs.ds.jeremyc.tpc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.ipc.Client;

import edu.washington.cs.ds.jeremyc.tpc.Message.Type;

/**
 * The coordinator keeps track of its cohorts, and an incrementing serial number used in all
 * messages to ensure the blocking nature and sequential consistency of the two-phase commit
 * protocol. The public method coordinate initiates the two phase commit of the data in question.
 * 
 */
public class Coordinator {

    private InetSocketAddress[] cohorts;

    private Client client;

    public static Logger LOG = SimulatedDistributedStore.LOG;

    private Long currentSerialNum = 0l;

    public Coordinator(ArrayList<InetSocketAddress> cohorts) {
        LOG.info("Creating coordinator");
        this.cohorts = cohorts.toArray(new InetSocketAddress[0]);
        client = new Client(Message.class, new Configuration());
    }

    public void coordinate(Data data) throws IOException {
        long serialNum;
        synchronized (currentSerialNum) {
            serialNum = currentSerialNum++;
        }
        Message[] responses = request(serialNum, data);
        if (proceed(responses)) {
            commit(serialNum);
        }
    }

    private Message[] request(long serialNum, Data data) throws IOException {
        LOG.info("Sending prepare query to all for " + data.toString());
        Message[] messages = new Message[cohorts.length];
        for (int i = 0; i < cohorts.length; i++) {
            Message m = new Message();
            m.setSerialNum(serialNum);
            m.setType(Type.PREPARE);
            m.setData(data);
            messages[i] = m;
        }
        Writable responses[] = client.call(messages, cohorts);
        for (int i = 0; i < responses.length; i++) {
            messages[i] = (Message) responses[i];
        }
        return messages;
    }

    private boolean proceed(Message[] responses) {
        LOG.info("Checking request responses");
        for (Message m : responses) {
            if (m == null || m.getType() == Type.ABORT) {
                LOG.info("Got abort message");
                return false;
            }
        }
        LOG.info("All requests granted");
        return true;
    }

    private Message[] commit(long serialNum) throws IOException {
        Message[] messages = new Message[cohorts.length];
        for (int i = 0; i < cohorts.length; i++) {
            Message m = new Message();
            m.setType(Type.COMMIT);
            m.setSerialNum(serialNum);
            messages[i] = m;
        }
        Writable responses[] = client.call(messages, cohorts);
        for (int i = 0; i < responses.length; i++) {
            messages[i] = (Message) responses[i];
        }
        return messages;
    }

}
