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

        Blockchain.init();
        new Server(new Blockchain(), "85.65.31.137", 25565).startServer();
        System.out.println("sad");

    }
}
