import java.util.ArrayList;

public class Message {
    private final int cmd;
    private final ArrayList<Node> nodes;
    private final int blocks_count = 0; // 0 just for this stage

    //this is a new comment!

    public Message(int cmd, ArrayList<Node> nodes) {
        this.cmd = cmd;
        this.nodes = nodes;
    }

    public Message(MessageBuilder builder){
        this.cmd = builder.getCmd();
        this.nodes = builder.getNodes();
    }

    public int getCmd() {
        return cmd;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public int getBlocks_count() {
        return blocks_count;
    }

    public int getNodes_count(){
        return nodes.size();
    }

    public byte[] toBytes(){
        // TODO
        return null;
    }
}
