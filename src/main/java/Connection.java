import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Connection {
    private Socket socket;

    public Connection(Socket socket){
        this.socket = socket;
    }

    public void send(Message msg) throws IOException {
        DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
        System.out.format("[*] ------ sending new message to %s:%d ------\n", socket.getInetAddress(), socket.getPort());
        System.out.println(msg.toString(true));
        dOut.write(msg.toBytes());
    }

    public Message receive() throws Exception {
        DataInputStream stream = new DataInputStream(socket.getInputStream());
        Message msg = new MessageParser(stream).toMessage();
        System.out.format("[*] ------ got new message from %s:%d ------\n", socket.getInetAddress(), socket.getPort());
        System.out.println(msg.toString(true));
        return msg;
    }

    public Socket getSocket() {
        return socket;
    }
}