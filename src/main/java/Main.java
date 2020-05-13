import java.util.Arrays;

public class Main {

    public static String NAME;

    public static void main(String[] args) throws Exception {
        //  Socket socket = new Socket("35.246.17.73", 8080);
        //  DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
        //  dOut.write(new Message(1, new ArrayList<>(), new ArrayList<>()).toBytes());
        //
        //  DataInputStream stream = new DataInputStream(socket.getInputStream());
        //  System.out.println(new MessageParser(stream).toMessage().toString(true));

//        Block b = new Block(925, 0x27634ff8, new byte[]{0x6C, (byte) 0xE8, (byte) 0xDE, 0x4C, 0x33, (byte) 0x94, 0x77, (byte) 0xB5},
//                new byte[]{(byte) 0x80, 0x00, 0x00, 0x00, 0x65, 0x3B, (byte) 0x86, (byte) 0xF0}, new byte[12]);
//        System.out.println(Arrays.toString(b.calcMD5()));

        String[] addr = args[0].split(":");
        String host = addr[0];
        int port = Integer.parseInt(addr[1]);

        NAME = args[1];
        //important: use those two functions to reset the database.
//        Database.saveBlockchain();
//        Database.saveNodeList();
        Database.wipe();
        Database.init();
        Server server = new Server(host, port);
        server.startServer();
        System.out.println("[!] something horrible happened and main loop of the server returned");

    }
}
