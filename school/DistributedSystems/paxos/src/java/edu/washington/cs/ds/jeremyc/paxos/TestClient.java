package edu.washington.cs.ds.jeremyc.paxos;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.Client;

import edu.washington.cs.ds.jeremyc.paxos.Message.Type;

/**
 * Client to submit new decrees to some specified proposer.
 */
public class TestClient {

    public static void main(String[] args){
        try {
            InetSocketAddress addr = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
            Decree d = new Decree();
            d.setKey(args[2]);
            d.setValue(args[3]);
            Message m = new Message();
            m.setType(Type.NEW);
            m.setDecree(d);
            Client client = new Client(Message.class, new Configuration());
            client.call(m, addr);
        } catch (NumberFormatException e) {
            usage(null);
        } catch (IOException e) {
            usage(e.getMessage());
        }
    }
    
    public static void usage(String message){
        System.out.println("Usage:");
        System.out
        .println("java edu.washington.edu.cs.ds.jeremyc.paxos.TestClient hostname port key value");
        System.out.println("Where:");
        System.out.println("host = hostname of member to initiate decree");
        System.out.println("port = port number of member to initiate decree");
        System.out.println("key = key of decreed value");
        System.out.println("value = value of decree value");
        if(message != null){
            System.out.println("Got error:");
            System.out.println(message);
        }
    }   
}
