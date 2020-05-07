//TODO save nodelist to file
//TODO save blockchain to file


public class Main {
    public static void main(String[] args) throws Exception {
        //  Socket socket = new Socket("35.246.17.73", 8080);
        //  DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
        //  dOut.write(new Message(1, new ArrayList<>(), new ArrayList<>()).toBytes());
        //
        //  DataInputStream stream = new DataInputStream(socket.getInputStream());
        //  System.out.println(new MessageParser(stream).toMessage().toString(true));

        String[] addr = args[0].split(":");
        String host = addr[0];
        int port = Integer.parseInt(addr[1]);

        Blockchain.init();
        new Server(new Blockchain(), host, port).startServer();
        System.out.println("[!] something horrible happened and main loop of the server returned");

    }
}
