import java.util.ArrayList;

public class MessageBuilder {
    private int cmd;
    private ArrayList<Node> nodes;
    private int blocks_count = 0; // 0 just for this stage

    public MessageBuilder(int cmd, ArrayList<Node> nodes, int blocks_count) {
        this.cmd = cmd;
        this.nodes = nodes;
        this.blocks_count = blocks_count;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public int getBlocks_count() {
        return blocks_count;
    }

    public void setBlocks_count(int blocks_count) {
        this.blocks_count = blocks_count;
    }

    public int getNodes_count(){
        return nodes.size();
    }
}
