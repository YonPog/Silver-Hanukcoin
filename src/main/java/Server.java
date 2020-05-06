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
        this.chain = chain;
        this.nodes = new ConcurrentHashMap<>(); // TODO: read the chain from the database
        // Add yourself
        Node self = new Node("Silver", HOST, PORT, getCurrentTime());
        self.setNew(false);
        nodes.put(new Pair<>(host, port), self);
    }

    public void startServer() {

        ConcurrentLinkedQueue<Pair<Node, Integer>> sendQueue = new ConcurrentLinkedQueue<>();
        // last time a request to 3 nodes was sent
        final int[] lastChange = {getCurrentTime()}; // array because Java wanted so (for it to be final)

        class ServerThread extends Thread {
            // waiting for connections, updating and adding to send queue

            @Override
            public void run() {
                // open the listening socket
                ServerSocketChannel serverSock;
                try {
                    serverSock = ServerSocketChannel.open();
                    serverSock.socket().bind(new InetSocketAddress(PORT));
                } catch (IOException e) {
                    System.out.println(String.format("[!] ERROR accepting at port %d", PORT));
                    return;
                }

                // wait for connections
                while (true) {
                    SocketChannel connSocket;
                    try {
                        connSocket = serverSock.accept();
                        if (connSocket != null) {
                            new ConnectionHandler(connSocket).start();
                        }
                    } catch (IOException e) {
                        System.out.println("[!] ERROR accept:\n " + e.toString());
                        return;
                    }
                }
            }

            class ConnectionHandler extends Thread {
                // getting requests via Connection and handling accordingly

                private SocketChannel sock;

                ConnectionHandler(SocketChannel s) {
                    this.sock = s;
                }

                public void run() {
                    Socket socket = sock.socket();
                    Message message = null;
                    try {
                        message = new Connection(socket).receive();
                    } catch (Exception e) {
                        System.out.println("[!] ERROR receiving connection");
                        return;
                    }
                    // update the blockchain
                    int statusCode = 0;
                    try {
                        statusCode = chain.update(message.getBlocks());
                    } catch (NoSuchAlgorithmException e) {
                        System.out.println("[!] ERROR - no such algorithm!");
                        return;
                    }
                    if (statusCode != 0) { // blockchain changed
                        lastChange[0] = getCurrentTime();
                        addNodesToSend(nodes, sendQueue);
                    }
                    // update nodes if necessary
                    boolean changed = false;
                    for (Node n : message.getNodes()) {
                        Pair<String, Integer> addr = new Pair<>(n.getHost(), n.getPort());
                        if (nodes.get(addr) == null) {
                            changed = true;
                            nodes.put(addr, n);
                            nodes.get(addr).setNew(true); // set the new node to "new" status
                        } else {
                            if (n.getLast_seen_ts() > nodes.get(addr).getLast_seen_ts()) {
                                nodes.replace(addr, n);
                            }
                            if (message.getCmd() == 2) {
                                nodes.get(addr).setNew(false); // got response from him, now he is legit
                            }
                        }
                    }
                    if (changed) {
                        lastChange[0] = getCurrentTime();
                        addNodesToSend(nodes, sendQueue);
                    }
                    // if we need to respond
                    if (message.getCmd() == 1) {
                        sendQueue.add(new Pair<>(nodes.get(new Pair<>(socket.getInetAddress().toString(),
                                socket.getPort())), 2));
                    }
                    try {
                        sock.close();
                    } catch (IOException e) {
                        System.out.println("[!] ERROR closing socket, for some stupid reason Java wanted me to catch that exception :(");
                    }

                }
            }

        }

        class SenderThread extends Thread {
            // sending requests and responses to nodes in the queue

            @Override
            public void run() {
                while (true) {
                    // update timestamp of ourselves
                    nodes.get(new Pair<>(HOST, PORT)).setLast_seen_ts((int) (System.currentTimeMillis() / 1000));

                    if (!sendQueue.isEmpty()) {
                        Pair<Node, Integer> pair = sendQueue.remove();
                        Node node = pair.getKey();
                        try {
                            Socket sock = new Socket(node.getHost(), node.getPort());
                            ArrayList<Node> nodesList = new ArrayList<>();
                            // send only the ones that aren't new
                            for (Node n : nodes.values()) {
                                if (!n.isNew()) {
                                    nodesList.add(n);
                                }
                            }
                            ArrayList<Block> blocksList = chain.getBlocks();
                            new Connection(sock).send(new Message(pair.getValue(), nodesList, blocksList));
                            sock.close();
                        } catch (IOException e) {
                            System.out.println("[!] ERROR opening socket!");
                        }
                    }
                }
            }
        }

        new ServerThread().start();
        new SenderThread().start();

        while (true) {
            try {
                Thread.sleep(60000); // 1 minute
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (lastChange[0] + 300000 < getCurrentTime()) { // if no change in the last 5 minutes
                lastChange[0] = getCurrentTime();
                addNodesToSend(nodes, sendQueue);
            }

            // delete every node that wasn't seen in the last 30 minutes
            for (Pair<String, Integer> pair : nodes.keySet()) {
                if (nodes.get(pair).getLast_seen_ts() + 1800 < getCurrentTime()) {
                    nodes.remove(pair);
                }

            }
        }

    }

    public ArrayList<Node> chooseNodes(ConcurrentHashMap<Pair<String, Integer>, Node> map) {
        Random random = new Random();
        ArrayList<Node> values = new ArrayList<>();
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

    public void addNodesToSend(ConcurrentHashMap<Pair<String, Integer>, Node> map,
                               ConcurrentLinkedQueue<Pair<Node, Integer>> queue) {
        ArrayList<Node> toAdd = chooseNodes(map);
        for (Node n : toAdd) {
            queue.add(new Pair<>(n, 1));
        }
    }

    public int getCurrentTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }
}
