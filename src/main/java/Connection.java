import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Connection {
    private Socket socket;

    public Connection(Socket socket){
        this.socket = socket;
    }

    public void send(Message msg) throws IOException {
        DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
        System.out.println("------ sending new message ------");
        System.out.println(msg.toString(true));
        dOut.write(msg.toBytes());
    }

    public Message receive() throws Exception {
        DataInputStream stream = new DataInputStream(socket.getInputStream());
        Message msg = new MessageParser(stream).toMessage();
        System.out.println("------ got new message ------");
        System.out.println(msg.toString(true));
        return msg;
    }
}