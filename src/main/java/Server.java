import javafx.util.Pair;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
        //TODO temporary
        Node franji = new Node("Earth", "35.246.17.73", 8080, getCurrentTime());
        //Node franji = new Node("Copper", "copper-coin.3utilities.com", 42069, getCurrentTime());
        nodes.put(new Pair<>(franji.getHost(), franji.getPort()), franji);
    }

    public void startServer() {

        ConcurrentLinkedQueue<Node> sendQueue = new ConcurrentLinkedQueue<>(); // node:cmd
        // last time a request to 3 nodes was sent
        final int[] lastChange = {getCurrentTime()}; // array because Java wanted so (for it to be final)
        //send first messsages
        Node franji = new Node("Earth", "35.246.17.73", 8080, getCurrentTime());
        //Node franji = new Node("Copper", "copper-coin.3utilities.com", 42069, getCurrentTime());
        sendQueue.add(franji);
        class ServerThread extends Thread {
            // waiting for connections, updating and adding to send queue

            @Override
            public void run() {
                System.out.println("server running");
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
                            System.out.println("------ new connection ------");
                            new RequestHandler(connSocket).start();
                        }
                    } catch (IOException e) {
                        System.out.println("[!] ERROR accept:\n " + e.toString());
                        return;
                    }
                }
            }

            class RequestHandler extends Thread {
                // getting requests via Connection and handling accordingly

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
                        if (message.getCmd() == 2){ //shouldnt get here
                            System.out.println("[!] ERROR RequestHandler got something im not supposed to see ;)");
                        }

                    } catch (Exception e) {
                        System.out.println("[!] ERROR receiving connection");
                        System.out.println(e.toString());
                        return;
                    }
                    //update database
                    updateDatabase(message, lastChange, sendQueue);
                    // if we need to respond
                    System.out.println("going to respond to the this message");
                    try {
                        conn.send(buildMessage(2));
                    } catch (IOException e) {
                        System.out.format("[!] ERROR failed to respond to %s:%d\n", socket.getInetAddress().toString(), socket.getPort());
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
                ArrayList<Pair<Connection, Node>> pending = new ArrayList<>();
                while (true) {
                    // update timestamp of ourselves
                    nodes.get(new Pair<>(HOST, PORT)).setLast_seen_ts(getCurrentTime());
                    //open new sockets for new messages
                    if (!sendQueue.isEmpty()){
                        Node target = sendQueue.remove();
                        try {
                            Socket sock = new Socket(target.getHost(), target.getPort());
                            //send the message
                            Connection conn = new Connection(sock);
                            conn.send(buildMessage(1));
                            //add the socket to the pending arraylist
                            pending.add(new Pair<>(conn, target));
                        } catch (Exception e) {
                            System.out.format("[!] ERROR sending message to %s:%d\n", target.getHost(), target.getPort());
                            return;
                        }
                    }

                    //now, check if there are any pending responses.
                    for (Pair<Connection, Node> pair : pending){
                        Connection conn = pair.getKey();
                        Node target = pair.getValue();
                        try {
                            if (conn.getSocket().getInputStream().available() > 0){ //if new information not read
                                Message msg = conn.receive();
                                updateDatabase(msg, lastChange, sendQueue);
                                nodes.get(new Pair<>(target.getHost(), target.getPort())).setNew(false); //update new status
                                System.out.format("updated status to verified: %s", target.toString());

                            }
                        } catch (Exception e) {
                            System.out.format("[!] ERROR receiving message to %s:%d\n", target.getHost(), target.getPort());
                            return;
                        }
                    }

//                    if (!sendQueue.isEmpty()) {
//                        Pair<Node, Integer> pair = sendQueue.remove();
//                        Node node = pair.getKey();
//                        try {
//                            Socket sock = new Socket(node.getHost(), node.getPort());
//                            ArrayList<Node> nodesList = new ArrayList<>();
//                            // send only the ones that aren't new
//                            for (Node n : nodes.values()) {
//                                if (!n.isNew()) {
//                                    nodesList.add(n);
//                                }
//                            }
//                            ArrayList<Block> blocksList = chain.getBlocks();
//                            new Connection(sock).send(new Message(pair.getValue(), nodesList, blocksList)); //cmd
//                            sock.close();
//                        } catch (IOException e) {
//                            System.out.println("[!] ERROR opening socket!");
//                            System.out.format("at: %s:%d\n", node.getHost(), node.getPort());
//                        }
//                    }
                }
            }
        }

        //dont forget we're still inside runServer ;)
        new ServerThread().start();
        new SenderThread().start();

        while (true) {
            try {
                Thread.sleep(60000); // 1 minute
                System.out.println("node list: ");
                for (Node n : nodes.values()){
                    System.out.println(n.toString());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (lastChange[0] + 300 < getCurrentTime()) { // if no change in the last 5 minutes
                System.out.println("adding 3 random messages to send queue...");
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
        //TODO remove ourselves instead of removing everyone else (maybe removing and adding ourselves)
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

    public Message buildMessage(int cmd){
        //only sending verified nodes
        ArrayList<Node> nodesToSend = new ArrayList<>();
        for (Node n : nodes.values()){
            if (!n.isNew()){
                nodesToSend.add(n);
            }
        }
        //building the block list
        ArrayList<Block> blocksList = chain.getBlocks();

        return new Message(cmd, nodesToSend, blocksList);
    }

    public void updateDatabase(Message message, int[] lastChange, ConcurrentLinkedQueue<Node> sendQueue) {
        // update the blockchain
        int statusCode;
        try {
            statusCode = Blockchain.update(message.getBlocks());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("[!] ERROR - no such algorithm!");
            return;
        }

        //update status of sender node in hashmap
//        System.out.println(sender.toString());
//        if (message.getCmd() == 2 && nodes.get(sender) != null && nodes.get(sender).isNew()) { //response
//            nodes.get(sender).setNew(false); // got response from him, now he is legit
//        }
        // update nodes if necessary
        boolean changed = statusCode != 0; //check changes in blockchain
        if (statusCode != 0){
            System.out.println("sending message because blockchain changed");
        }
        for (Node n : message.getNodes()) { //check changes in nodes and update hashMap
            Pair<String, Integer> addr = new Pair<>(n.getHost(), n.getPort());
            if (nodes.get(addr) == null) {
                changed = true;
                System.out.println("sending message because nodelist changed");
                nodes.put(addr, n);
                nodes.get(addr).setNew(true); // set the new node to "new" status
            } else {
                if (n.getLast_seen_ts() > nodes.get(addr).getLast_seen_ts()) {
                    nodes.replace(addr, n);
                }
            }
        }
        if (changed) {
            lastChange[0] = getCurrentTime();
            addNodesToSend(nodes, sendQueue);
        }
    }

    public void addNodesToSend(ConcurrentHashMap<Pair<String, Integer>, Node> map,
                               ConcurrentLinkedQueue<Node> sendQueue) { //map is the node map, sendQueue is sendqueue
        ArrayList<Node> toAdd = chooseNodes(map);
        sendQueue.addAll(toAdd);
    }

    public int getCurrentTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }
}
