import java.util.ArrayList;
import java.util.Arrays;

public class MessageParser {

    private final byte[] stream;

    /**
     * @param stream Must be a legal representation of a legal message according to the protocol.
     */
    public MessageParser(byte[] stream){
        this.stream = stream;
    }

    /**
     * @return The Message that is represented by the byte stream initializer.
     */
    public Message toMessage(){
        int cmd = this.parseCmd();
        ArrayList<Node> nodes = this.parseNodes();
        return new Message(cmd, nodes);
    }

    /**
     * @return The cmd part of the protocol specification.
     */
    private int parseCmd(){
        byte[] bytes = Arrays.copyOfRange(this.stream, 0, 4);
        return Utils.bytesToInt(bytes);
    }

    /**
     * @return The nodes_count part of the protocol specification.
     */
    private int parseNodesCount(){
        byte[] count = Arrays.copyOfRange(this.stream, 8, 12);
        return Utils.bytesToInt(count);
    }

    /**
     * @return An ArrayList representing every node in the network according to the message.
     */
    private ArrayList<Node> parseNodes(){
        ArrayList<Node> nodes = new ArrayList<>();
        int nodeIndex = 12; // after nodes_count in the protocol
        int nodesToParse = this.parseNodesCount();

        while (nodes.size() < nodesToParse){
            Node node = this.parseNode(nodeIndex);
            nodes.add(node);

            // Length of the current node according to protocol
            nodeIndex += 8 + node.getName().length() + node.getHost().length();
        }

        return nodes;
    }

    /**
     * @param start_index The index which the node starts within this.stream - the byte representation of the message.
     * @return The Node represented in this.stream at index start_index.
     */
    private Node parseNode(int start_index){
        // Node starts with the length of the name
        int nameLen = this.stream[start_index];
        String name = new String(Arrays.copyOfRange(this.stream,
                start_index + 1, // Name starts after the name length.
                start_index + 1 + nameLen));

        // After the name length and name there is the host name length.
        int hostLenIndex = start_index + 1 + nameLen;
        int host_len = this.stream[hostLenIndex];
        String host = new String(Arrays.copyOfRange(this.stream,
                hostLenIndex + 1, // Host name starts after its length.
                hostLenIndex + 1 + host_len));

        // After the host name length and name there is the port number.
        int portIndex = hostLenIndex + 1 + host_len;
        int port = Utils.bytesToInt(Arrays.copyOfRange(this.stream, portIndex, portIndex + 2));

        // Port is 2 bytes, after them there are 4 bytes of the timestamp.
        int lastSeenTs = Utils.bytesToInt(Arrays.copyOfRange(this.stream,
                portIndex + 2, // 2 bytes after the port.
                portIndex + 6));


        return new Node(name, host, port, lastSeenTs);
    }

}
