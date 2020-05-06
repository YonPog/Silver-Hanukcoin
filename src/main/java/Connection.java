import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Connection {
    private Socket socket;

    public Connection(Socket socket){
        this.socket = socket;
    }

    public void send(Message msg) throws IOException {
        DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
        System.out.format("------ sending new message to %s------ \n", socket.getInetAddress().toString());
        System.out.println(msg.toString(true));
        dOut.write(msg.toBytes());
        //socket.close();
    }

    public Message receive() throws Exception {
        DataInputStream stream = new DataInputStream(socket.getInputStream());
        Message msg = new MessageParser(stream).toMessage();
        System.out.format("------ got new message from %s------ \n", socket.getInetAddress().toString());
        System.out.println(msg.toString(true));
        socket.close();
        return msg;
    }
}