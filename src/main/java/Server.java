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
    private final String HOST;
    private final int PORT;
    private final Miner miner;
    private final int[] lastChange = new int[1];
    private ConcurrentLinkedQueue<Node> sendQueue;


    public Server(String host, int port) throws IOException {
        this.HOST = host;
        this.PORT = port;
        this.miner = new Miner(5, this);

        // Add this host (ourselves)
        Node self = new Node(Main.NAME, HOST, PORT, getCurrentEpoch());
        self.setNew(false);
        Database.setNode(new Pair<>(host, port), self);

        // TODO temporary - change to a dynamic first node (maybe reading from db)
        Node franji = new Node("Earth", "35.246.17.73", 8080, getCurrentEpoch());
        Database.setNode(new Pair<>(franji.getHost(), franji.getPort()), franji);
        System.out.format("[*] Successfully initialized server on %s:%d\n", HOST, PORT);
    }

    /**
     * The main runtime of the server
     */
    public void startServer() {

        sendQueue = new ConcurrentLinkedQueue<>(); // nodes to send to them

        // last time a request to 3 nodes was sent
        lastChange[0] = getCurrentEpoch(); // array because Java wanted so (for it to be final)

        Node franji = new Node("Earth", "35.246.17.73", 8080, getCurrentEpoch());
        sendQueue.add(franji);
        // send initialization messages to all known nodes.
        //sendQueue.addAll(Database.getNodes().values());

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

                System.out.format("[*] Starting to listen to connections\n");
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
                        continue;
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
                        System.out.format("[!] ERROR getting data from request. Details:\n%s\n", e.toString());
                        return;
                    }
                    // update database and variables according to the new information
                    // and adding to sendQueue if something changed
                    try {
                        updateDatabase(message);
                    } catch (IOException e) {
                        System.out.println("[!] ERROR writing node change");
                    }
                    // starting to create response
                    System.out.println("[*] creating the response for this message");
                    try {
                        // building the response based on the network state
                        conn.send(generateMessage(2));
                    } catch (IOException e) {
                        System.out.format("[!] ERROR failed to respond to %s:%d\n.Details:\n %s\n",
                                socket.getInetAddress().toString(),
                                socket.getPort(),
                                e.toString());
                    }

                    try {
                        sock.close();
                    } catch (IOException e) {
                        System.out.format("[!] ERROR closing socket, for some stupid reason Java wanted me to catch that exception :(\nDetails:\n%s\n",
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
                    Database.getNode(new Pair<>(HOST, PORT)).setLast_seen_ts(getCurrentEpoch());
                    // open new sockets for new messages
                    while (!sendQueue.isEmpty()) { // there is someone we need to send a message to
                        Node target = sendQueue.remove(); // retrieves the head of the queue and deletes it
                        try {
                            Socket sock = new Socket(target.getHost(), target.getPort());
                            // create the connection
                            Connection conn = new Connection(sock);
                            // build the request and send it
                            conn.send(generateMessage(1));
                            // add the socket to the pending arraylist
                            pending.add(new Pair<>(conn, target));
                        } catch (Exception e) {
                            System.out.format("[!] ERROR sending message to %s:%d\nDetails:\n%s\n",
                                    target.getHost(),
                                    target.getPort(),
                                    e.toString());
                            continue;
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
                                new Thread(() -> {
                                    try {
                                        updateDatabase(msg);
                                    } catch (IOException e) {
                                        System.out.format("[!] ERROR updating database\nDetails:\n%s\n", e.toString());
                                    }
                                }).start();
                                // update new status, because the node responded to us
                                Database.getNode(new Pair<>(target.getHost(), target.getPort())).setNew(false);
                                System.out.format("[*] updated status to verified: %s", target.toString());

                            }
                        } catch (Exception e) {
                            System.out.format("[!] ERROR receiving message from %s:%d\nDetails:\n%s",
                                    target.getHost(),
                                    target.getPort(),
                                    e.toString());
                            continue;
                        }
                    }

                }
            }
        }

        // don't forget we're still inside runServer ;)
        new ServerThread().start();
        new SenderThread().start();
        miner.start();

        while (true) {

            // TODO
            Database.saveToMongoDB();

            try {
                Thread.sleep(60000); // check for changes every minute
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }


            System.out.println("[*] node list: ");
            for (Node n : Database.getNodes().values()) {
                System.out.print("\t" + n.toString());
            }
            addNodesToSend(Database.getNodes());

            if (lastChange[0] + 300 < getCurrentEpoch()) { // if no change in the last 5 minutes
                System.out.println("[*] no change in 5 minutes, adding 3 random nodes to sendQueue");
                lastChange[0] = getCurrentEpoch();
                // add 3 random nodes from the HashMap
                addNodesToSend(Database.getNodes());
            }

            // delete every node that wasn't seen in the last 30 minutes
            for (Pair<String, Integer> pair : Database.getNodes().keySet()) {
                if (Database.getNode(pair).getLast_seen_ts() + 1800 < getCurrentEpoch()) {
                    Database.getNodes().remove(pair);
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
    public Message generateMessage(int cmd) {
        // only sending verified nodes
        ArrayList<Node> nodesToSend = new ArrayList<>();
        for (Node n : Database.getNodes().values()) {
            if (!n.isNew()) {
                nodesToSend.add(n);
            }
        }
        // retrieving the block list
        ArrayList<Block> blocksList = Database.getBlocks();

        return new Message(cmd, nodesToSend, blocksList);
    }


    public void parseSolvedPuzzle(Block nextBlock){
        System.out.println("Parsing solved puzzle");
        try {
            Database.update(nextBlock);
            miner.updateBlock(nextBlock);
            lastChange[0] = getCurrentEpoch();
            addNodesToSend(Database.getNodes());

        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.format("[!] ERROR parsing new block");
        }
    }

    /**
     * @param message    The message containing (possibly new) data
     */
    synchronized public void updateDatabase(Message message) throws IOException {
        // update the blockchain
        int statusCode;
        try {
            statusCode = Database.update(message.getBlocks());
        } catch (NoSuchAlgorithmException | IOException e) {
            System.out.format("[!] ERROR unrecognized algorithm.\nDetails:\n%s", e.toString());
            return;
        }

        // update nodes if necessary
        boolean changed = statusCode != 0; // check if blockchain changed
        if (statusCode != 0) {
            System.out.println("[*] sending new messages and updating Miner thread because blockchain changed");
            miner.blockchainChanged.set(true);
            miner.updateBlock(Database.getLatestBlock());
            System.out.println("Blockchain changed!");
        }
        // check for changes in nodes and update the HashMap
        for (Node n : message.getNodes()) {
            // the key to the HashMap entry
            Pair<String, Integer> addr = new Pair<>(n.getHost(), n.getPort());
            if (Database.getNode(addr) == null) { // node not in the HashMap
                changed = true;
                System.out.println("[*] sending message because the nodes list changed");
                Database.setNode(addr, n);
                Database.getNode(addr).setNew(true); // set the new node to "new" status
            } else {
                // node was in out HashMap
                if (n.getLast_seen_ts() > Database.getNodes().get(addr).getLast_seen_ts()) {
                    // set the timestamp to the maximum one
                    n.setNew(Database.getNode(addr).isNew());
                    Database.getNodes().replace(addr, n);
                }
            }
        }
        if (changed) {
            lastChange[0] = getCurrentEpoch();
            addNodesToSend(Database.getNodes());
        }
    }

    /**
     * @param map       The HashMap of the nodes
     */
    public void addNodesToSend(ConcurrentHashMap<Pair<String, Integer>, Node> map) {
        ArrayList<Node> toAdd = chooseNodes(map);
        sendQueue.addAll(toAdd);
        //TODO temporary until we can trust the network
        Node backup = new Node("Silver2", "82.81.206.242", 1337, getCurrentEpoch());
        sendQueue.add(backup);
    }

    /**
     * @return Current unix epoch in seconds
     */
    public int getCurrentEpoch() {
        return (int) (System.currentTimeMillis() / 1000);
    }
}
