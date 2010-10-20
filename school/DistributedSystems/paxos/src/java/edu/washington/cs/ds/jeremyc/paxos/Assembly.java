package edu.washington.cs.ds.jeremyc.paxos;

import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Just a main method that instantiates one or several assembly members, and starts the servers of those
 * it deems to be local (comprised of Member instances).
 */
public class Assembly {

    /**
     * 
     * @param args
     *            The port numbers, one for each member serving on this host, plus
     *            hostname.port_number one for each member on a remote host.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String localhost = getLocalHost();
        ArrayList<MemberID> memberIDs = new ArrayList<MemberID>();
        for (String member : args) {
            try {
                String[] nameParts = member.split(":");
                if (nameParts.length > 2) {
                    usage();
                    return;
                }
                String host;
                int port;
                if (nameParts.length == 1) {
                    host = localhost;
                    port = Integer.parseInt(nameParts[0]);
                } else {
                    host = nameParts[0];
                    port = Integer.parseInt(nameParts[1]);
                }
                MemberID memberID = new MemberID(host, port);
                if (!memberIDs.contains(memberID)) {
                    // Make sure no duplicate host:port
                    if (new InetSocketAddress(host, port).getAddress() != null) {
                        // Make sure it's resolvable
                        memberIDs.add(memberID);
                    }
                }
            } catch (NumberFormatException e) {
                throw new Exception(
                        "servers to run on this host must be listed as 'hostName.portNum'");
            }
        }
        ArrayList<Member> members = new ArrayList<Member>();
        for (int i = 0; i < memberIDs.size(); i++) {
            if (memberIDs.get(i).getHostname().equals(localhost)) {
                members.add(new Member(i, memberIDs));
            }
        }
        for (Member member : members) {
            member.start();
        }
        Thread.sleep(Long.MAX_VALUE);
    }

    public static void usage() {
        System.out.println("Usage:");
        System.out
                .println("java edu.washington.edu.cs.ds.jeremyc.paxos.Assembly [host.port|port] [host.port|port] ...");
    }

    public static String getLocalHost() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException e) {
            System.err.println("Can't lookup local host name " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
