import javafx.util.Pair;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.AbstractQueue;
import java.util.ArrayList;
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
        nodes.put(new Pair<>(host, port), new Node("Silver", HOST, PORT, (int) (System.currentTimeMillis() / 1000)));
    }

    public void startServer() {

        ConcurrentLinkedQueue<Pair<Node, Integer>> sendQueue = new ConcurrentLinkedQueue<>();

        class ServerThread extends Thread {
            // waiting for connections, updating and adding to send queue

            @Override
            public void run() {
                // open the listening socket
                ServerSocketChannel serverSock = null;
                try {
                    serverSock = ServerSocketChannel.open();
                    serverSock.socket().bind(new InetSocketAddress(PORT));
                } catch (IOException e) {
                    System.out.println(String.format("[!] ERROR accepting at port %d", PORT));
                    return;
                }

                // wait for connections
                while (true) {
                    SocketChannel connSocket = null;
                    try {
                        connSocket = serverSock.accept();
                        if (connSocket != null) {
                            new ConnectionHandler().start();
                        }
                    } catch (IOException e) {
                        System.out.println("[!] ERROR accept:\n " + e.toString());
                        return;
                    }
                }
            }

            class ConnectionHandler extends Thread {
                // getting requests via Connection and handling accordingly

                public void run(SocketChannel sock) {
                    Socket socket = sock.socket();
                    Message message = new Connection(socket).receive();
                    // update the blockchain
                    chain.update(message.getBlocks());
                    // update nodes if necessary
                    for (Node n : message.getNodes()) {
                        Pair<String, Integer> addr = new Pair<>(n.getHost(), n.getPort());
                        if (nodes.get(addr) == null) {
                            nodes.put(addr, n);
                        } else {
                            if (n.getLast_seen_ts() > nodes.get(addr).getLast_seen_ts()) {
                                nodes.replace(addr, n);
                            }
                        }
                    }
                    // if we need to respond
                    if (message.getCmd() == 1) {
                        sendQueue.add(new Pair<>(nodes.get(new Pair<>(socket.getInetAddress().toString(),
                                socket.getPort())), 2));
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
                            ArrayList<Node> nodesList = new ArrayList<>(nodes.values());
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

        // TODO: Add another thread that goes over the nodes and adds to the send queue, deletes or changes "new" status

    }
}
