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

    private byte[][] getNodesArray(){
        byte[][] ret = new byte[this.getNodes_count()][];
        for (int i = 0 ; i < ret.length ; ++i){
            ret[i] = this.nodes.get(i).toBytes();
        }
        return ret;
    }

    public byte[] toBytes(){
        byte[] cmd = Utils.intToBytes(this.cmd, 4);
        byte[] nodes_count = Utils.intToBytes(this.getNodes_count(), 4);
        byte[] nodes = Utils.concat(this.getNodesArray());
        byte[] blocksCount = Utils.intToBytes(this.getBlocks_count(), 4);
        byte[] blocks = Utils.intToBytes(0, 0); // TODO
        return Utils.concat(cmd, Utils.intToBytes(0xBeefBeef, 4), nodes_count, nodes, Utils.intToBytes(0xDeadDead, 4), blocksCount, blocks);
    }

    @Override
    public String toString(){
        return String.format("<Message cmd=%d, with %d nodes>\n", this.cmd, this.nodes.size());
    }

    public String toString(boolean full){
        if (full){
            StringBuilder ret = new StringBuilder(String.format("<Message cmd=%d, with %d nodes: >\n", this.cmd, this.nodes.size()));
            for (Node n : this.nodes){
                String msg = "\t" + n.toString();
                ret.append(msg);
            }
            return ret.toString();
        }
        else{
            return this.toString();
        }
    }
}
