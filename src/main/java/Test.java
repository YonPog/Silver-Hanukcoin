import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        testParser();
    }

    public static void testParser(){
        System.out.println("Starting to check MessageParser...");
        /*
        cmd = 1
        nodes_count = 2
            name = test_name
            host = 1.3.3.7
            port = 42
            timestamp = 123

            name = silver
            host = 5.4.3.2
            port = 567
            timestamp = 987
         */
        byte[] b1 = {0, 0, 0, 1, (byte) 0xBE, (byte) 0xef, (byte) 0xbe, (byte) 0xef, 0, 0, 0, 2,
                9, 116, 101, 115, 116, 95, 110, 97, 109, 101, 7, 49, 46, 51, 46, 51, 46, 55, 0, 42, 0, 0, 0, 123,
                6, 115, 105, 108, 118, 101, 114, 7, 53, 46, 52, 46, 51, 46, 50, 2, 55, 0, 0, 3, (byte) 219};

        MessageParser parser = new MessageParser(b1);
        Message message = parser.toMessage();
        if (message.getCmd() != 1){
            System.out.println("Error in cmd");
        }
        if (message.getNodes_count() != 2){
            System.out.println("Error in nodes_count");
        }

        if (!message.getNodes().get(0).getName().equals("test_name")){
            System.out.println("Error in name of node 1");
        }
        if (!message.getNodes().get(0).getHost().equals("1.3.3.7")){
            System.out.println("Error in host of node 1");
        }
        if (message.getNodes().get(0).getPort() != 42){
            System.out.println("Error in port of node 1");
        }
        if (message.getNodes().get(0).getLast_seen_ts() != 123){
            System.out.println("Error in timestamp of node 1");
        }

        if (!message.getNodes().get(1).getName().equals("silver")){
            System.out.println("Error in name of node 2");
        }
        if (!message.getNodes().get(1).getHost().equals("5.4.3.2")){
            System.out.println("Error in host of node 2");
        }
        if (message.getNodes().get(1).getPort() != 567){
            System.out.println("Error in port of node 2");
        }
        if (message.getNodes().get(1).getLast_seen_ts() != 987){
            System.out.println("Error in timestamp of node 2");
        }
        if(!Arrays.equals(Utils.intToBytes(0, 2), new byte[]{0, 0})){
            System.out.println(Arrays.toString(Utils.intToBytes(0, 2)));
            System.out.println("Error in Utils.intToBytes 1");
        }
        if(!Arrays.equals(Utils.intToBytes(1, 2), new byte[]{0, 0x1})){
            System.out.println("Error in Utils.intToBytes 2");
        }
        if(!Arrays.equals(Utils.intToBytes(4660, 2), new byte[]{0x12, 0x34})){
            System.out.println(Arrays.toString(Utils.intToBytes(4660, 2)));
            System.out.println("Error in Utils.intToBytes 3");
        }
        if(Utils.bytesToInt(Utils.intToBytes(1337, 10)) != 1337){
            System.out.println("Error in Utils.intToBytes 4");
        }
        if(!Arrays.equals(Utils.intToBytes(Utils.bytesToInt(new byte[] {0, 0, 0x25, 0x3, 0, 0x4}),6),new byte[] {0, 0, 0x25, 0x3, 0, 0x4})){
            System.out.println("Error in Utils.intToBytes 5");
        }

        if(!Arrays.equals(message.getNodes().get(0).toBytes(), new byte[]{9, 116, 101, 115, 116, 95, 110, 97, 109, 101, 7, 49, 46, 51, 46, 51, 46, 55, 0, 42, 0, 0, 0, 123})){
            System.out.println("Error!");
        }

        if(!Arrays.equals(message.getNodes().get(1).toBytes(), new byte[]{6, 115, 105, 108, 118, 101, 114, 7, 53, 46, 52, 46, 51, 46, 50, 2, 55, 0, 0, 3, (byte) 219})){
            System.out.println("Error!");
        }
    }
}
