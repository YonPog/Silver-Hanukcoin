import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
//        Socket socket = new Socket("35.246.17.73", 8080);
//        DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
//        dOut.write(new Message(1, new ArrayList<>(), new ArrayList<>()).toBytes());
//
//        DataInputStream stream = new DataInputStream(socket.getInputStream());
//        System.out.println(new MessageParser(stream).toMessage().toString(true));

        Blockchain.init();


        System.out.println("Done!");

    }
}
