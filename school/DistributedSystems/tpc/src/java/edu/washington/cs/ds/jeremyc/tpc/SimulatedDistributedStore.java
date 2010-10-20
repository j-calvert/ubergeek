package edu.washington.cs.ds.jeremyc.tpc;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * Sets up a set of cohorts listening on ports passed into the main method,
 * the sets up a coordinator of those cohorts, and has the coordinator issue
 * 1000 edit instructions involving a total to 10 data elements.
 */
public class SimulatedDistributedStore {

    public static Logger LOG = Logger.getLogger("TPClog");

    public static String getLocalHost() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException e) {
            System.err.println("Can't lookup local host name " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     *            The ports on which the cohorts run.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        LOG.addHandler(new StreamHandler(System.out, new SimpleFormatter()));
        String localhost = getLocalHost();
        ArrayList<Integer> ports = new ArrayList<Integer>();
        ArrayList<InetSocketAddress> cohortAddrs = new ArrayList<InetSocketAddress>();
        ArrayList<Cohort> cohorts = new ArrayList<Cohort>();
        for (String arg : args) {
            try {
                int port = Integer.parseInt(arg);
                if (!ports.contains(port)) {
                    cohortAddrs.add(new InetSocketAddress(localhost, port));
                }
            } catch (NumberFormatException e) {
                throw new Exception(
                        "servers to run on this host must be listed as 'hostName.portNum'");
            }
        }

        for (InetSocketAddress addr : cohortAddrs) {
            cohorts.add(new Cohort(addr));
        }

        for (Cohort cohort : cohorts) {
            cohort.start();
        }

        Coordinator coordinator = new Coordinator(cohortAddrs);
        for (long i = 0; i < 1000; i++) {
            Data data = new Data();
            data.setKey(i % 10);
            data.setData("dataNumber" + i);
            coordinator.coordinate(data);
        }

    }

}
