import javafx.util.Pair;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {
    private Blockchain chain;
    private ConcurrentHashMap<Pair<String, Integer>, Node> nodes;
    private final String HOST;
    private final int PORT;

    public Server(Blockchain chain, String host, int port) {
        this.HOST = host;
        this.PORT = port;
        this.chain = chain; // TODO - read from database
        this.nodes = new ConcurrentHashMap<>(); // TODO: read the nodes from the database

        // Add this host (ourselves)
        Node self = new Node("Silver", HOST, PORT, getCurrentEpoch());
        self.setNew(false);
        nodes.put(new Pair<>(host, port), self);

        // TODO temporary - will be fixed after reading from database
        Node franji = new Node("Earth", "35.246.17.73", 8080, getCurrentEpoch());
        nodes.put(new Pair<>(franji.getHost(), franji.getPort()), franji);
        System.out.format("[*] Successfully initialized server on %s:%d", HOST, PORT);
    }

    /**
     * The main runtime of the server
     */
    public void startServer() {

        ConcurrentLinkedQueue<Node> sendQueue = new ConcurrentLinkedQueue<>(); // nodes to send to them

        // last time a request to 3 nodes was sent
        final int[] lastChange = {getCurrentEpoch()}; // array because Java wanted so (for it to be final)

        // TODO - send first messsages (temporary)
        Node franji = new Node("Earth", "35.246.17.73", 8080, getCurrentEpoch());
        sendQueue.add(franji);

        /**
         * Waiting for connections, updating and adding to send queue
         */
        class ServerThread extends Thread {

            @Override
            public void run() {
                System.out.println("[*] Server started");
                // open the listening socket
                ServerSocketChannel serverSock;
                try {
                    serverSock = ServerSocketChannel.open();
                    serverSock.socket().bind(new InetSocketAddress(PORT));
                } catch (IOException e) {
                    System.out.println(String.format("[!] ERROR accepting at port %d", PORT));
                    return;
                }

                System.out.format("[*] Starting to listen to connections");
                // wait for connections
                while (true) {
                    SocketChannel connSocket;
                    try {
                        connSocket = serverSock.accept();
                        if (connSocket != null) {
                            System.out.println("[*] ------ new connection ------");
                            // handle the new incoming request
                            new RequestHandler(connSocket).start();
                        }
                    } catch (IOException e) {
                        System.out.format("[!] ERROR accepting connection. Details:\n%s", e.toString());
                        return;
                    }
                }
            }

            /**
             * getting requests via Connection and handling accordingly
             */
            class RequestHandler extends Thread {

                private SocketChannel sock;

                RequestHandler(SocketChannel s) {
                    this.sock = s;
                }

                @Override
                public void run() {
                    Socket socket = sock.socket();
                    Message message;
                    Connection conn = new Connection(socket);
                    try {
                        message = conn.receive();
                        if (message.getCmd() == 2) {
                            // should not get here, this handler is only for requests
                            System.out.println("[!] ERROR RequestHandler got something he's not supposed to see ;)");
                            return;
                        }

                    } catch (Exception e) {
                        System.out.format("[!] ERROR getting data from request. Details:\n%s", e.toString());
                        return;
                    }
                    // update database and variables according to the new information
                    // and adding to sendQueue if something changed
                    updateDatabase(message, lastChange, sendQueue);
                    // starting to create response
                    System.out.println("[*] creating the response for this message");
                    try {
                        // building the response based on the network state
                        conn.send(buildMessage(2));
                    } catch (IOException e) {
                        System.out.format("[!] ERROR failed to respond to %s:%d\n.Details:\n %s",
                                socket.getInetAddress().toString(),
                                socket.getPort(),
                                e.toString());
                    }

                    try {
                        sock.close();
                    } catch (IOException e) {
                        System.out.format("[!] ERROR closing socket, for some stupid reason Java wanted me to catch that exception :(\nDetails:\n%s",
                                e.toString());
                    }

                }
            }

        }

        /**
         * sending requests and responses to nodes in the queue
         */
        class SenderThread extends Thread {

            @Override
            public void run() {
                // the list of sent requests that have not been answered yet
                ArrayList<Pair<Connection, Node>> pending = new ArrayList<>();

                while (true) {
                    // update timestamp of ourselves
                    nodes.get(new Pair<>(HOST, PORT)).setLast_seen_ts(getCurrentEpoch());
                    // open new sockets for new messages
                    if (!sendQueue.isEmpty()) { // there is someone we need to send a message to
                        Node target = sendQueue.remove(); // retrieves the head of the queue and deletes it
                        try {
                            Socket sock = new Socket(target.getHost(), target.getPort());
                            // create the connection
                            Connection conn = new Connection(sock);
                            // build the request and send it
                            conn.send(buildMessage(1));
                            // add the socket to the pending arraylist
                            pending.add(new Pair<>(conn, target));
                        } catch (Exception e) {
                            System.out.format("[!] ERROR sending message to %s:%d\nDetails:\n%s",
                                    target.getHost(),
                                    target.getPort(),
                                    e.toString());
                            return;
                        }
                    }

                    // still in the infinite loop, check if a connecion in the pending list has data to receive
                    for (Pair<Connection, Node> pair : pending){
                        Connection conn = pair.getKey();
                        Node target = pair.getValue();
                        try {
                            if (conn.getSocket().getInputStream().available() > 0) { // if new data is present
                                Message msg = conn.receive();
                                // update database and variables according to the new information
                                // and adding to sendQueue if something changed
                                updateDatabase(msg, lastChange, sendQueue);
                                // update new status, because the node responded to us
                                nodes.get(new Pair<>(target.getHost(), target.getPort())).setNew(false);
                                System.out.format("[*] updated status to verified: %s", target.toString());

                            }
                        } catch (Exception e) {
                            System.out.format("[!] ERROR receiving message to %s:%d\nDetails:\n%s",
                                    target.getHost(),
                                    target.getPort(),
                                    e.toString());
                            return;
                        }
                    }

                }
            }
        }

        // don't forget we're still inside runServer ;)
        new ServerThread().start();
        new SenderThread().start();

        while (true) {
            try {
                Thread.sleep(60000); // check for changes every minute
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println("[*] node list: ");
            for (Node n : nodes.values()) {
                System.out.print("\t" + n.toString());
            }

            if (lastChange[0] + 300 < getCurrentEpoch()) { // if no change in the last 5 minutes
                System.out.println("[*] no change in 5 minutes, adding 3 random nodes to sendQueue");
                lastChange[0] = getCurrentEpoch();
                // add 3 random nodes from the HashMap
                addNodesToSend(nodes, sendQueue);
            }

            // delete every node that wasn't seen in the last 30 minutes
            for (Pair<String, Integer> pair : nodes.keySet()) {
                if (nodes.get(pair).getLast_seen_ts() + 1800 < getCurrentEpoch()) {
                    nodes.remove(pair);
                }

            }
        }

    }

    /**
     * @param map A HashMap of the nodes to choose from
     * @return An ArrayList of 3 random different nodes that aren't ourselves
     */
    public ArrayList<Node> chooseNodes(ConcurrentHashMap<Pair<String, Integer>, Node> map) {
        Random random = new Random();
        ArrayList<Node> values = new ArrayList<>();
        /* TODO whole implementation is stupid -
            remove ourselves instead of removing everyone else (maybe removing and adding ourselves) */
        for (Pair<String, Integer> key : map.keySet()) {
            if (!(key.getKey().equals(HOST) && key.getValue() == PORT)) { // to not choose ourselves
                values.add(map.get(key));
            }
        }
        int bound = values.size();
        ArrayList<Integer> indexes = new ArrayList<>();
        while (indexes.size() < Math.min(3, values.size())) {
            int choice = random.nextInt(bound);
            if (!indexes.contains(choice)) {
                indexes.add(choice);
            }
        }
        ArrayList<Node> ret = new ArrayList<>();
        for (int index : indexes) {
            ret.add(values.get(index));
        }
        return ret;
    }

    /**
     * @param cmd The cmd field of the message to be sent
     * @return The message built from cmd and the state of the network (nodes and blockchain)
     */
    public Message buildMessage(int cmd) {
        // only sending verified nodes
        ArrayList<Node> nodesToSend = new ArrayList<>();
        for (Node n : nodes.values()) {
            if (!n.isNew()) {
                nodesToSend.add(n);
            }
        }
        // retrieving the block list
        ArrayList<Block> blocksList = chain.getBlocks();

        return new Message(cmd, nodesToSend, blocksList);
    }

    /**
     * @param message    The message containing (possibly new) data
     * @param lastChange The last time a message to 3 nodes was sent, needs update if the network state changed in this function
     * @param sendQueue  The queue to add 3 nodes to send a message to if needed
     */
    public void updateDatabase(Message message, int[] lastChange, ConcurrentLinkedQueue<Node> sendQueue) {
        // update the blockchain
        int statusCode;
        try {
            statusCode = Blockchain.update(message.getBlocks());
        } catch (NoSuchAlgorithmException e) {
            System.out.format("[!] ERROR unrecognized algorithm.\nDetails:\n%s", e.toString());
            return;
        }

        // update nodes if necessary
        boolean changed = statusCode != 0; // check if blockchain changed
        if (statusCode != 0) {
            System.out.println("[*] sending new messages because blockchain changed");
        }
        // check for changes in nodes and update the HashMap
        for (Node n : message.getNodes()) {
            // the key to the HashMap entry
            Pair<String, Integer> addr = new Pair<>(n.getHost(), n.getPort());
            if (nodes.get(addr) == null) { // node not in the HashMap
                changed = true;
                System.out.println("[*] sending message because the nodes list changed");
                nodes.put(addr, n);
                nodes.get(addr).setNew(true); // set the new node to "new" status
            } else {
                // node was in out HashMap
                if (n.getLast_seen_ts() > nodes.get(addr).getLast_seen_ts()) {
                    // set the timestamp to the maximum one
                    nodes.replace(addr, n);
                }
            }
        }
        if (changed) {
            lastChange[0] = getCurrentEpoch();
            addNodesToSend(nodes, sendQueue);
        }
    }

    /**
     * @param map       The HashMap of the nodes
     * @param sendQueue The queue to add the nodes to
     */
    public void addNodesToSend(ConcurrentHashMap<Pair<String, Integer>, Node> map,
                               ConcurrentLinkedQueue<Node> sendQueue) {
        ArrayList<Node> toAdd = chooseNodes(map);
        sendQueue.addAll(toAdd);
    }

    /**
     * @return Current unix epoch in seconds
     */
    public int getCurrentEpoch() {
        return (int) (System.currentTimeMillis() / 1000);
    }
}
