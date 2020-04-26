import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("35.246.17.73", 8080);
        DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
        dOut.write(new Message(1, new ArrayList<Node>()).toBytes());

        DataInputStream stream = new DataInputStream(socket.getInputStream());
        byte[] data = new byte[100];
        int count = stream.read(data);
        System.out.println(new MessageParser(data).toMessage().toString(true));
    }
}
