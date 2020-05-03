import java.util.ArrayList;

public class MessageBuilder {
    private int cmd;
    private ArrayList<Node> nodes;
    private ArrayList<Block> blocks;


    public MessageBuilder(int cmd, ArrayList<Node> nodes, ArrayList<Block> blocks) {
        this.cmd = cmd;
        this.nodes = nodes;
        this.blocks = blocks;
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
        return blocks.size();
    }

    public int getNodes_count(){
        return nodes.size();
    }

    public ArrayList<Block> getBlocks(){ return this.blocks; }

    public void setBlocks(ArrayList<Block> blocks){ this.blocks = blocks; }
}
