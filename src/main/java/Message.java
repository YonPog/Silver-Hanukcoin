import java.util.ArrayList;

public class Message {
    private final int cmd;
    private final ArrayList<Node> nodes;
    private final ArrayList<Block> blocks;



    //this is a new comment!

    public Message(int cmd, ArrayList<Node> nodes, ArrayList<Block> blocks) {
        this.cmd = cmd;
        this.nodes = nodes;
        this.blocks = blocks;
    }

    public Message(MessageBuilder builder){
        this.cmd = builder.getCmd();
        this.nodes = builder.getNodes();
        this.blocks = builder.getBlocks();
    }

    public int getCmd() {
        return cmd;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public int getBlocks_count() {
        return blocks.size();
    }

    public ArrayList<Block> getBlocks() {
        return blocks;
    }

    public int getNodes_count() {
        return nodes.size();
    }

    /**
     * @return A 2D byte array, each inner array is representing one node according to the message.
     */
    private byte[][] getNodesArray() {
        byte[][] ret = new byte[this.getNodes_count()][];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = this.nodes.get(i).toBytes();
        }
        return ret;
    }

    /**
     * @return A 2D byte array, each inner array is representing one block according to the message.
     */
    private byte[][] getBlocksArray() {
        byte[][] ret = new byte[this.getBlocks_count()][];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = this.blocks.get(i).toBytes();
        }
        return ret;
    }

    /**
     * @return The byte representation of the message to be sent, in a byte array.
     */
    public byte[] toBytes() {
        byte[] cmd = Utils.intToBytes(this.cmd, 4);
        byte[] nodes_count = Utils.intToBytes(this.getNodes_count(), 4);
        byte[] nodes = Utils.concat(this.getNodesArray());
        byte[] blocksCount = Utils.intToBytes(this.getBlocks_count(), 4);
        byte[] blocks = Utils.concat(this.getBlocksArray());
        return Utils.concat(cmd, Utils.intToBytes(0xBeefBeef, 4), nodes_count, nodes,
                Utils.intToBytes(0xDeadDead, 4), blocksCount, blocks);
    }

    @Override
    public String toString(){
        return String.format("<Message cmd=%d, with %d nodes and %d blocks>\n",
                this.cmd, this.nodes.size(), this.blocks.size());
    }

    public String toString(boolean full){
        if (full){
            StringBuilder ret = new StringBuilder(String.format("<Message cmd=%d, with %d nodes: >\n", this.cmd, this.nodes.size()));
            for (Node n : this.nodes){
                String msg = "\t" + n.toString();
                ret.append(msg);
            }
            ret.append(String.format("\nand %d blocks:\n", this.blocks.size()));
            for (Block b : this.blocks){
                String msg = "\t" + b.toString();
                ret.append(msg);
            }
            return ret.toString();
        }
        else{
            return this.toString();
        }
    }
}
