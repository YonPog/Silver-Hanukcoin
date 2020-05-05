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
        dOut.write(msg.toBytes());
    }

    public Message receive() throws Exception {
        DataInputStream stream = new DataInputStream(socket.getInputStream());
        return new MessageParser(stream).toMessage();
    }
}