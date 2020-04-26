public class Node {
    private String name;
    private String host;
    private int port;
    private int last_seen_ts;

    public Node(String name, String host, int port, int last_seen_ts){
        this.name = name;
        this.host = host;
        this.port = port;
        this.last_seen_ts = last_seen_ts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getLast_seen_ts() {
        return last_seen_ts;
    }

    public void setLast_seen_ts(int last_seen_ts) {
        this.last_seen_ts = last_seen_ts;
    }

    public byte[] toBytes(){
        // TODO
        return null;
    }

    @Override
    public String toString(){
        return String.format("<Node %s on %s : %d, last seen %d>\n", this.name, this.host, this.port, this.last_seen_ts);
    }
}
