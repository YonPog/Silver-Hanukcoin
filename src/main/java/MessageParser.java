import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MessageParser {

    private final DataInputStream stream;

    private static final int BEEFBEEF = 0xBeefBeef;
    private static final int DEADDEAD = 0xDeadDead;

    /**
     * @param stream Must be a legal representation of a legal message according to the protocol.
     */
    public MessageParser(DataInputStream stream){
        this.stream = stream;
    }

    /**
     * @return The Message that is represented by the byte stream initializer.
     */
    public Message toMessage() throws Exception {
        int cmd = this.stream.readInt();
        ArrayList<Node> nodes = this.parseNodes();
        ArrayList<Block> blocks = this.parseBlocks();
        return new Message(cmd, nodes, blocks);
    }

    private ArrayList<Block> parseBlocks() throws Exception{
        int a = this.stream.readInt();
        if (a != DEADDEAD) {
            throw new Exception ("No DeadDead found!");
        }
        int blocksCount = this.stream.readInt();
        ArrayList<Block> blocks = new ArrayList<>();
        while (blocks.size() < blocksCount){
            Block block = this.parseBlock();
            blocks.add(block);
        }

        return blocks;
    }

    private Block parseBlock() throws IOException {
        int serNum = this.stream.readInt();
        int wallet = this.stream.readInt();
        byte[] prevSig = new byte[8];
        this.stream.read(prevSig);

        byte[] puzzle = new byte[8];
        this.stream.read(puzzle);

        byte[] sig = new byte[12];
        this.stream.read(sig);

        return new Block(serNum, wallet, prevSig, puzzle, sig);
    }

    /**
     * @return An ArrayList representing every node in the network according to the message.
     */
    private ArrayList<Node> parseNodes() throws Exception {
        if (this.stream.readInt() != BEEFBEEF){ throw new Exception("No BeefBeef found!"); }
        int nodesCount = this.stream.readInt();
        ArrayList<Node> nodes = new ArrayList<>();
        while (nodes.size() < nodesCount){
            Node node = this.parseNode();
            nodes.add(node);
        }

        return nodes;
    }

    /**
     * @return The Node represented in this.stream.
     */
    private Node parseNode() throws IOException {
        // Node starts with the length of the name
        int nameLen = this.stream.readUnsignedByte();
        byte[] nameBytes = new byte[nameLen];
        this.stream.read(nameBytes);
        String name = new String(nameBytes);

        // After the name length and name there is the host name length.
        int host_len = this.stream.readUnsignedByte();
        byte[] hostBytes = new byte[host_len];
        this.stream.read(hostBytes);
        String host = new String(hostBytes);


        // After the host name length and name there is the port number.
        byte[] portBinary = new byte[2];
        this.stream.read(portBinary);
        int port = Utils.bytesToInt(portBinary); //two byte int

        // Port is 2 bytes, after them there are 4 bytes of the timestamp.
        int lastSeenTs = this.stream.readInt();


        return new Node(name, host, port, lastSeenTs);
    }

}
