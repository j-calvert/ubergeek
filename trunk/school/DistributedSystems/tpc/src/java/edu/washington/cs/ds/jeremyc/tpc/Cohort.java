package edu.washington.cs.ds.jeremyc.tpc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.ipc.Server;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;

import edu.washington.cs.ds.jeremyc.tpc.Message.Type;

/**
 * The cohort recieves and responds to three types of messages: Prepare, Commit, and Abort.
 * 
 * Upon receiving a prepare request, it waits until no other previous, unfulfilled requests are
 * blocking it and then attempts to execute a transaction up to the abort/commit phase
 * 
 * Upon receiving a commit or abort request, it waits until no other previous, unfullfilled requests
 * are blocking it and then attempts the abort/commit.
 * 
 * Failure in the commit phase itself can lead to inconsistency in the cohorts' accounts. This is
 * one shortcoming that is dealt with by the three phase commit protocol.
 */
public class Cohort extends Server {

    private ArrayBlockingQueue<QueuedTransaction> blockingQueue = new ArrayBlockingQueue<QueuedTransaction>(
            255);

    private Storage storage;

    private static Logger LOG = SimulatedDistributedStore.LOG;

    private int port;

    public Cohort(InetSocketAddress addr) {
        super(addr.getPort(), Message.class, 3, new Configuration());
        port = addr.getPort();
        try {
            storage = new Storage("port" + port);
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Writable call(Writable param) throws IOException {
        if (!(param instanceof Message)) {
            throw new IOException("Recieved message that was not of type "
                    + Message.class.getCanonicalName());
        }
        Message message = (Message) param;
        long serialNum = message.getSerialNum();
        Transaction t = null;
        LOG.info(port + " Recieved " + message.toString());
        switch (message.getType()) {
        case PREPARE:
            message.setType(Type.AGREEMENT);
            try {
                t = queueTransaction(serialNum);
                storage.getDa().pKey.put(t, message.getData());
            } catch (Exception e) {
                try {
                    if (t != null) {
                        t.abort();
                        t = null;
                    }
                } catch (DatabaseException e1) {
                    if (t != null) {
                        t = null;
                    }
                }
                message.setType(Type.ABORT);
            }
            break;
        case COMMIT:
            try {
                t = dequeueTransaction(serialNum);
                t.commit();
                t = null;
                message.setType(Type.ACKNOWLEDGE);
            } catch (Exception e) {
                LOG.severe("Got exception at commit stage " + e.getMessage());
            }
            break;
        case ABORT:
            try {
                t = dequeueTransaction(serialNum);
                t.abort();
                message.setType(Type.ACKNOWLEDGE);
            } catch (Exception e) {
                LOG.severe("Got exception trying to abort " + e.getMessage());
            }
            break;
        default:
            throw new IOException("Cohort can't handle receiving message of type "
                    + message.getType().toString());
        }
        LOG.info(port + " Returning " + message.toString());
        return message;
    }

    public Transaction queueTransaction(long serialNum) throws DatabaseException,
            InterruptedException {
        QueuedTransaction qt = new QueuedTransaction();
        qt.t = storage.getEnv().beginTransaction(null, null);
        qt.serialNumber = serialNum;
        synchronized (blockingQueue) {
            blockingQueue.add(qt);
        }
        int timeout = 10000;
        while (blockingQueue.peek().serialNumber != serialNum && timeout > 0) {
            Thread.sleep(10);
            timeout = timeout - 10;
        }
        if (timeout <= 0) {
            throw new InterruptedException();
        }
        return qt.t;
    }

    public Transaction dequeueTransaction(long serialNum) throws InterruptedException {
        int timeout = 10000;
        while (blockingQueue.peek().serialNumber != serialNum && timeout > 0) {
            Thread.sleep(10);
            timeout = timeout - 10;
        }
        if (timeout > 0 && blockingQueue.peek().serialNumber == serialNum) {
            return blockingQueue.poll().t;
        } else {
            throw new InterruptedException();
        }
    }

    public static class QueuedTransaction {

        long serialNumber;

        Transaction t;

    }
}