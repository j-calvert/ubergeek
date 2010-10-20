package edu.washington.cs.ds.jeremyc.paxos;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.ipc.Client;
import org.apache.hadoop.ipc.Server;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;

import edu.washington.cs.ds.jeremyc.paxos.Message.Type;
import edu.washington.cs.ds.jeremyc.paxos.Paper.Status;

/**
 * A member of parliament. Responsible for receiving messages from other members and responsible for taking action on decrees based on the state as
 * defined in the slip of {@link Paper}
 * 
 */
public class Member extends Server {

    /**
     * Used to print output.
     */
    private static Logger LOG = Logger.getLogger("ParliamentLog");

    /**
     * Considered constant in this implementation 
     */
    private ArrayList<MemberID> assemblyIDs;
    
    /**
     * A trivial function of assemblyIDs.  Used only for a minor optimization.
     */
    private InetSocketAddress[] addresses;

    /**
     * Used primarily to generate and determine ownership of ballot IDs
     */
    private MemberID thisHost;

    /**
     * Used internally to communicate with other members (including self).
     */
    private Client client;

    /**
     * Used internally to read and write ledger and paper entries.
     */
    private Storage storage;


    /**
     * Initiates action on decrees.
     */
    BallotProcessor ballotProcessor;

    /**
     * Construct the i^th member of the assembly comprised of servers
     * 
     * @throws IOException
     */
    public Member(int i, ArrayList<MemberID> servers) throws IOException {
        super(servers.get(i).getPort(), Message.class, servers.size() - 1, new Configuration());
        LOG.addHandler(new StreamHandler(System.out, new SimpleFormatter()));
        client = new Client(Message.class, new Configuration());
        thisHost = servers.get(i);
        assemblyIDs = servers;
        LOG.info("Adding member " + thisHost.toString());
        try {
            storage = new Storage(thisHost.toDirString());
        } catch (DatabaseException e) {
            throw new IOException("Failed to open storage" + e.getMessage());
        }
        addresses = new InetSocketAddress[assemblyIDs.size()];
        for (int j = 0; j < addresses.length; j++) {
            addresses[j] = new InetSocketAddress(assemblyIDs.get(j).getHostname(), assemblyIDs.get(j)
                    .getPort());
        }
        ballotProcessor = new BallotProcessor(thisHost.getPort());
    }

    @Override
    public synchronized void start() throws IOException {
        super.start();
        ballotProcessor.start();

    }

    @Override
    public Writable call(Writable param) throws IOException {
        if (!(param instanceof Message)) {
            throw new IOException("Recieved paxos message that was not of type "
                    + Message.class.getCanonicalName());
        }
        Message message = (Message) param;
        switch (message.getType()) {
        case NEW:
            intitiateNewDecree(message.getDecree());
            break;
        case NEXT_BALLOT:
            receiveNextBallot(message.getDecreeNum(), message.getBallotNumber());
            break;
        case LAST_VOTE:
            receiveLastVote(message.getDecreeNum(), message.getBallotNumber(), message.getDecree(),
                    message.getVoter());
            break;
        case BEGIN_BALLOT:
            receiveBeginBallot(message.getDecreeNum(), message.getBallotNumber(), message
                    .getDecree());
            break;
        case VOTED:
            receiveVoted(message.getDecreeNum(), message.getBallotNumber(), message.getVoter());
            break;
        case SUCCESS:
            receiveSuccess(message.getDecreeNum(), message.getDecree());
            break;
        default:
            throw new IOException("Can't respond to message type: " + message.getType().toString());
        }
        return new Message();
    }

    /**
     * Creates an entry for the decree provided, presumably by a client, who hopefully knows that
     * this member is the distinguished proposer.
     * 
     * The next available decree number is chosen based on the existing ledger entries. If another
     * client initiates a decree with another member at or close to the same time, one of the
     * decrees will be subsumed by the other. If this were being used for an actual distributed
     * state machine, we would want to respond to the initiate decree request with some indication
     * of whether or not the value of this decree was the outcome for the decree number that was
     * assigned in this request.
     * 
     * @param decree
     *            The key and value pair of the decree.
     * @throws IOException
     */
    private void intitiateNewDecree(Decree decree) throws IOException {
        try {
            EntityCursor<Long> c = storage.getLedgers().pKey.keys();
            Long decNum = c.last();
            c.close();
            decNum = (decNum == null ? 0 : decNum + 1);
            Paper paper = new Paper(decNum);
            paper.setDecree(decree);
            paper.setQuorum(assemblyIDs);
            Ledger ledger = new Ledger(decNum);
            // This member was asked, so we set last tried to indicate as much
            ledger.setLastTried(new BallotNumber(thisHost));
            storage.getLedgers().pKey.put(ledger);
            storage.getPapers().pKey.put(paper);
            LOG.info(thisHost + " initiating new decree #" + decNum + ": " + decree);
        } catch (DatabaseException e) {
            LOG.severe(thisHost + "got Database Exception " + e.toString()
                    + " initiating new decree " + decree.toString());
            throw new IOException();
        }
    }

    /**
     * Another member (proposer) has sent this member (acceptor) a NextBallot message for the
     * provided decree and ballot numbers.
     * 
     * It compares the ballot number sent to the nextBal number found in it's ledger. If the ballot
     * number sent is greater than the next ballot number, it sets nextBal to the sent ballot
     * number, as prescribed by the algorithm.
     * 
     * @param decreeNum
     *            The decreeNum (used to look up the appropriate ledger entry).
     * @param bn
     *            The ballot number (compared to nextBal in the ledger entry and assigned if the
     *            former exceeds the latter).
     * @throws IOException
     */
    private void receiveNextBallot(long decreeNum, BallotNumber bn) throws IOException {
        LOG.info(thisHost + " received next ballot for decreeNum " + decreeNum
                + " with ballot num: " + bn.toString());
        try {
            Ledger ledger = storage.getLedgers().pKey.get(decreeNum);
            if (ledger == null) {
                ledger = new Ledger(decreeNum);
            }
            if (ledger.getNextBal() == null) {
                ledger.setNextBal(bn);
                LOG.info(thisHost + " received next ballot for decreeNum " + decreeNum
                        + " with ballot num: " + bn.toString() + " GRANTED");
            } else {
                if (ledger.getNextBal().compareTo(bn) < 0) {
                    ledger.setNextBal(bn);
                    LOG.info(thisHost + " received next ballot for decreeNum " + decreeNum
                            + " with ballot num: " + bn.toString() + " GRANTED");
                } else {
                    LOG.warning(thisHost + " received next ballot for decreeNum " + decreeNum
                            + " with ballot num: " + bn.toString() + " DECLINED");
                }
            }
            storage.getLedgers().pKey.put(ledger);
        } catch (DatabaseException e) {
            throw new IOException();
        }
    }

    /**
     * Another member (acceptor) has sent this member (proposer) a LastVote message.
     * 
     * This member responds by checking the ballot number against the ballot number for which it
     * last tried to get other members to prepare. If they match, and the slip of paper indicates
     * that the status for this decree is TRYING, it adds the sending member's ID to the PrevVotes
     * for this decree.
     * 
     * If the sending member has already voted on the decree, then we substitue whatever decree
     * value we may have been about to propose with this value.
     * 
     * @param decreeNum
     *            The decree for which this LastVote message is being sent.
     * @param bn
     *            The last ballot number for which the sending member has agreed to vote.
     * @param d
     *            The decree value that the sending member has already agreed to vote for.
     * @param voter
     *            The ID of the member that has sent the LastVote msg.
     * @throws IOException
     */
    private void receiveLastVote(long decreeNum, BallotNumber bn, Decree d, MemberID voter)
            throws IOException {
        try {
            Paper paper = storage.getPapers().pKey.get(decreeNum);
            Ledger ledger = storage.getLedgers().pKey.get(decreeNum);
            if (ledger.getLastTried().equals(bn) && paper.getStatus() == Status.TRYING
                    && !paper.getPrevVotes().contains(voter)) {
                LOG
                        .info(thisHost + " received last vote for decreeNum " + decreeNum
                                + " with ballot num: " + bn.toString() + " from member "
                                + voter.toString());
                paper.getPrevVotes().add(voter);
                storage.getPapers().pKey.put(paper);
            } else if (ledger.getLastTried().compareTo(bn) < 0) {
                // Our ballot number was usurped by another members, so
                // substitute their decree value in place of ours.
                paper.setDecree(d);
                storage.getPapers().pKey.put(paper);
            }
        } catch (DatabaseException e) {
            throw new IOException();
        }
    }

    /**
     * Another member (proposer) has sent this member (acceptor) a BeginBallot message. Since we're
     * being asked to begin the ballot, a decree value is also sent (unlike what's included in a
     * NextBallot msg).
     * 
     * If this ballot number is greater than any other for which this member has voted, it votes on
     * this ballot (by setting PrevBallot to the provided ballot num). Otherwise (it has already
     * voted for a higher ballot number) it does not.
     * 
     * @param decreeNum
     *            The number of the decree being voted on
     * @param bn
     *            The number of the ballot the proposer is asking to being.
     * @param decree
     *            The value of the decree that this vote would establish
     * @throws IOException
     */
    private void receiveBeginBallot(long decreeNum, BallotNumber bn, Decree decree)
            throws IOException {
        LOG.info(thisHost + " receive begin ballot for decreeNum " + decreeNum
                + " with ballot num: " + bn.toString() + " and decree " + decree.toString());
        try {
            Ledger ledger = storage.getLedgers().pKey.get(decreeNum);
            if (ledger == null) {
                throw new RuntimeException(
                        "Asked to begin ballot on for a decree num that has no ledger");
            }
            if ((ledger.getNextBal() != null) && bn.equals(ledger.getNextBal())
                    && (ledger.getPrevBal() == null || bn.compareTo(ledger.getPrevBal()) > 0)) {
                ledger.setPrevBal(bn);
                LOG.info(thisHost + " receive begin ballot for decreeNum " + decreeNum
                        + " with ballot num: " + bn.toString() + " and decree " + decree.toString()
                        + " YES VOTE");
            } else {
                LOG.info(thisHost + " receive begin ballot for decreeNum " + decreeNum
                        + " with ballot num: " + bn.toString() + " and decree " + decree.toString()
                        + " NO VOTE");
            }
            ledger.setPrevDec(decree);
            storage.getLedgers().pKey.put(ledger);
        } catch (DatabaseException e) {
            LOG.severe("Got database exception " + e.getMessage());
            throw new IOException();
        }
    }

    /**
     * Another member (acceptor) has sent this member (proposer) a ReceiveVote message.
     * 
     * This member responds by adding the sending member's ID to the list of voters on the slip of
     * paper, provided that the status of the decree was POLLING and that the ballot number sent
     * matches the number of the last ballot tried.
     * 
     * @param decreeNum
     *            The number of the decree being voted on.
     * @param bn
     *            The number of the ballot for which the sender is voting.
     * @param voter
     *            The ID of the sending (voting) member (acceptor).
     * @throws IOException
     */
    private void receiveVoted(long decreeNum, BallotNumber bn, MemberID voter) throws IOException {
        LOG.info(thisHost + " receive vote for decreeNum " + decreeNum + " with ballot num: "
                + bn.toString() + " from voter " + voter.toString());
        try {
            Paper paper = storage.getPapers().pKey.get(decreeNum);
            Ledger ledger = storage.getLedgers().pKey.get(decreeNum);
            if (ledger.getLastTried().equals(bn) && paper.getStatus() == Status.POLLING) {
                if (!paper.getVoters().contains(voter)) {
                    paper.getVoters().add(voter);
                    LOG.info(thisHost + " receive vote for decreeNum " + decreeNum
                            + " with ballot num: " + bn.toString() + " from voter "
                            + voter.toString() + " YES VOTE");
                } else {
                    LOG.info(thisHost + " receive vote for decreeNum " + decreeNum
                            + " with ballot num: " + bn.toString() + " from voter "
                            + voter.toString() + " (duplicate report)");
                }
            } else {
                LOG.info(thisHost + " receive vote for decreeNum " + decreeNum
                        + " with ballot num: " + bn.toString() + " from voter " + voter.toString()
                        + " NO VOTE");
            }
            storage.getPapers().pKey.put(paper);
        } catch (DatabaseException e) {
            throw new IOException();
        }
    }

    /**
     * Another member (any member, but for us, only a proposer) has sent this member (learner) a
     * message indicating that the decree numbered decNum has achieved consesus on the value
     * provided by the Decree arg.
     * 
     * @param decreeNum
     *            The number of the successful decree.
     * @param decree
     *            The value on which at least a majority has reached consensus.
     * @throws IOException
     */
    private void receiveSuccess(long decreeNum, Decree decree) throws IOException {
        LOG.info(thisHost + " receive success report for decreeNum " + decreeNum
                + " for decreeVal " + decree.toString());
        try {
            Ledger ledger = storage.getLedgers().pKey.get(decreeNum);
            if (ledger.getOutcome() == null) {
                ledger.setOutcome(decree);
            } else {
                LOG.info(thisHost + " receive success report for decreeNum " + decreeNum
                        + " for decreeVal " + decree.toString() + " (duplicate report)");
            }
            storage.getLedgers().pKey.put(ledger);
        } catch (DatabaseException e) {
            throw new IOException();
        }
    }

    /**
     * A thread that initiates action based on data in Paper and Ledger.
     */       
    private class BallotProcessor extends Thread {

        public BallotProcessor(int portNum) {
            this.setName("Ballot proc for " + portNum);
            this.setDaemon(true);
        }

        /**
         * Periodically runs through ledger and initiates messages based on paper and ledger data.
         */
        @Override
        public void run() {
            try {
                while (true) {
                    EntityCursor<Long> c = storage.getLedgers().pKey.keys();
                    Long first = c.first();
                    Long last = c.last();
                    c.close();
                    if (first != null && last != null) {
                        for (long decNum = first; decNum <= last; decNum++) {
                            Paper p = storage.getPapers().pKey.get(decNum);
                            Ledger l = storage.getLedgers().pKey.get(decNum);
                            // If we have a ledger entry for this decree & it indicates that
                            // consensus has not been reached.
                            if (l != null && l.getOutcome() == null) {
                                tryNewBallot(l, p);
                                sendNextBallot(l, p);
                                sendLastVote(l);
                                startPollingMajoritySet(p);
                                sendBeginBallot(l, p);
                                sendVoted(l);
                                succeed(l, p);
                                sendSucceeded(l, p);
                            }
                        }
                    }
                    try {
                        // sleep a tiny bit so we don't have an overly tight loop
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            } catch (DatabaseException e) {
                LOG.severe("Got DatabaseException: " + e.getMessage());
                throw new RuntimeException(e);
            } catch (IOException e) {
                LOG.severe("Got IOException: " + e.getMessage());
            }
        }

        /**
         * For the provided ledger entry, if this member is the owner of the ballot number last
         * tried for this decree, (and this is the first time or it's been at least 5 seconds since
         * the last time the member did this), then the ledger lastTried ballot number is
         * instantiated or incrememnted, the status is set to TRYING, and the account of prevVotes
         * is cleared.
         * 
         * @param l
         *            The ledger entry
         * @param p
         *            The slip of paper
         * @throws DatabaseException
         */
        private void tryNewBallot(Ledger l, Paper p) throws DatabaseException {
            try {
                // If this member is the owner of this ballot.
                if (l.getLastTried() != null && ownThisBallotNumber(l.getLastTried())) {
                    // And we haven't tried to start in the last 5 seconds
                    if (System.currentTimeMillis() - l.getTimeLastTried() > 5000) {
                        // set to ballot number greater than previous
                        BallotNumber bn = l.getLastTried();
                        if (bn == null) {
                            bn = new BallotNumber(thisHost);
                        } else {
                            bn.setBallotId(bn.getBallotId() + 1);
                        }
                        LOG.info("Trying to start ballot for decree: " + l.getDecreeNum()
                                + " and ballot num " + bn);
                        l.setLastTried(bn);
                        // set status to trying
                        if (p == null) {
                            // If this is the first ballot for this decree, there is no
                            // piece of paper yet, so instatiate p
                            p = new Paper(l.getDecreeNum());
                        }
                        p.setStatus(Status.TRYING);
                        // set prev votes to null
                        p.getPrevVotes().clear();
                        // mark time of retry.
                        l.setTimeLastTried(System.currentTimeMillis());
                        storage.getPapers().pKey.put(p);
                        storage.getLedgers().pKey.put(l);
                    }
                }
            } catch (DatabaseException e) {
                LOG.severe(thisHost + " got DatabaseException in tryNewBallot: " + e.getMessage());
            }
        }

        /**
         * Sends a NextBallot message to all members provided that the piece of paper indicates that
         * the status of this decree is TRYING (ie, tryNewBallot has set this status)
         * 
         * @param l
         *            The ledger entry
         * @param p
         *            The slip of paper
         * @throws IOException
         */
        private void sendNextBallot(Ledger l, Paper p) throws IOException {
            if (p != null && p.getStatus() == Status.TRYING) {
                Message message = new Message();
                message.setDecreeNum(l.getDecreeNum());
                message.setType(Type.NEXT_BALLOT);
                message.setBallotNumber(l.getLastTried());
                multicastCall(message);
            }
        }

        /**
         * Sends a last vote message to the owner of the ballot number in the getNextBal field of
         * the ledger entry...provided that getNextBal is greater than getPrevBal.
         * 
         * @param l
         *            The ledger entry
         * @throws IOException
         */
        private void sendLastVote(Ledger l) throws IOException {
            if (l.getNextBal() != null
                    && (l.getPrevBal() == null || l.getNextBal().compareTo(l.getPrevBal()) > 0)) {
                Decree d = new Decree();
                if (l.getPrevDec() != null) {
                    d = l.getPrevDec();
                }
                Message m = new Message();
                m.setType(Type.LAST_VOTE);
                m.setDecreeNum(l.getDecreeNum());
                m.setBallotNumber(l.getNextBal());
                m.setVoter(thisHost);
                m.setDecree(d);
                InetSocketAddress addr = new InetSocketAddress(l.getNextBal().getMemberID()
                        .getHostname(), l.getNextBal().getMemberID().getPort());
                LOG.info(thisHost + " sending " + m.toString() + " to " + addr.toString());
                client.call(m, addr);
            }
        }

        /**
         * If the number of members in the paper's PrevVotes set is greater than half the set of all
         * members (a majority) then we start polling, meaning, we set the status on the piece of
         * paper to POLLING, set the assemblyIDs on the piece of paper to the members in PrevVotes
         * 
         * @param p
         *            The slip of paper
         * @throws DatabaseException
         */
        private void startPollingMajoritySet(Paper p) throws DatabaseException {
            if (p != null && p.getPrevVotes().size() * 2 > assemblyIDs.size()) {
                LOG.info(thisHost + " got majority, setting status to POLLING");
                p.setStatus(Status.POLLING);
                p.setQuorum(p.getPrevVotes());
                // Decree has already been set based on lastVotes received and
                // default of initiated value
                storage.getPapers().pKey.put(p);
            }
        }

        /**
         * If the status on the slip of paper equals POLLING, then this member (a proposer) send a
         * begin ballot message to all members (which by definition of "all", will include our
         * assemblyIDs).
         * 
         * @param l
         *            The ledger entry
         * @param p
         *            The slip of paper
         * @throws IOException
         */
        private void sendBeginBallot(Ledger l, Paper p) throws IOException {
            if (p != null && p.getStatus() == Status.POLLING) {
                LOG.info(thisHost + " sending Begin Ballot to everybody");
                Message m = new Message();
                m.setType(Type.BEGIN_BALLOT);
                m.setDecreeNum(l.getDecreeNum());
                m.setBallotNumber(l.getLastTried());
                m.setDecree(p.getDecree());
                multicastCall(m);
            }
        }

        /**
         * If this ledger entry has a non-null PrevBal number, then we send the message that we have
         * voted for this ballot number to the owner of the ballot.
         * 
         * @param l
         *            The ledger entry
         * @throws IOException
         */
        private void sendVoted(Ledger l) throws IOException {
            if (l.getPrevBal() != null) {
                Message m = new Message();
                m.setType(Type.VOTED);
                m.setDecreeNum(l.getDecreeNum());
                m.setBallotNumber(l.getPrevBal());
                m.setVoter(thisHost);
                InetSocketAddress addr = new InetSocketAddress(l.getPrevBal().getMemberID()
                        .getHostname(), l.getPrevBal().getMemberID().getPort());
                LOG.info(thisHost + " sending " + m.toString() + " to " + addr.toString());
                client.call(m, addr);
            }
        }

        /**
         * If the piece of paper indicates that we're polling for this decree, and the set of voters
         * contains the assemblyIDs, we set the outcome of the decree because it's just passed
         * 
         * @param l
         *            The ledger entry
         * @param p
         *            The piece of paper.
         * @throws DatabaseException
         */
        private void succeed(Ledger l, Paper p) throws DatabaseException {
            if (p != null && p.getStatus() == Status.POLLING
                    && p.getVoters().containsAll(p.getQuorum()) && l.getOutcome() == null) {
                l.setOutcome(p.getDecree());
                LOG.info(thisHost + " succeeded with decree " + p.getDecree().toString());
                storage.getLedgers().pKey.put(l);
            }
        }

        /**
         * If we've reached consensus on this ballot, and we're the owner of the ballot (according
         * to the ballot number) then we send a message to all members (learners) indicating such.
         * 
         * Note: This implies that only the proposer of the ballot informs learners of the ballot's
         * success. This is not part of the algorithms specification, and lessens the liklihood of
         * progess in the event of failure (specifically, failure in the proposer between ballot
         * success and informing others of this success), but we do this to keep success messages
         * from overwhelming the log output.
         * 
         * @param l
         *            The ledger entry
         * @param p
         *            The slip of paper
         * @throws IOException
         */
        private void sendSucceeded(Ledger l, Paper p) throws IOException {
            if (l.getOutcome() != null && l.getLastTried() != null
                    && ownThisBallotNumber(l.getLastTried())) {
                Message m = new Message();
                m.setType(Type.SUCCESS);
                m.setDecreeNum(l.getDecreeNum());
                m.setDecree(l.getOutcome());
                multicastCall(m);
            }
        }

        /**
         * A utility method to send the same message to all other members.
         * 
         * @param message
         *            The message being sent.
         * @throws IOException
         */
        private void multicastCall(Message message) throws IOException {
            LOG.info(thisHost + " sending to all " + message.toString());
            Message[] msgs = new Message[addresses.length];
            for (int i = 0; i < addresses.length; i++) {
                msgs[i] = message;
            }
            client.call(msgs, addresses);
        }

        /**
         * A utility method to determine if this member (host) owns the provided ballot number
         * 
         * @param bn
         *            The ballot number
         * @return True iff this member owns the provided ballot number
         */
        private boolean ownThisBallotNumber(BallotNumber bn) {
            return bn.getMemberID().equals(thisHost);
        }

    }
}
