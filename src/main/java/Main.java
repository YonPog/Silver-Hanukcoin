import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
        //  Socket socket = new Socket("35.246.17.73", 8080);
        //  DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
        //  dOut.write(new Message(1, new ArrayList<>(), new ArrayList<>()).toBytes());
        //
        //  DataInputStream stream = new DataInputStream(socket.getInputStream());
        //  System.out.println(new MessageParser(stream).toMessage().toString(true));

        Blockchain.init();
        new Server(new Blockchain(), "85.65.31.137", 7777).startServer();
        byte[] puzzle =  Utils.parseByteStr("71 16 8F 29  D9 FE DF F9");
        byte[] sig = Utils.parseByteStr("BF 3D AE 1F  65 B0 8F 66 AB 2D B5 1E");
        //
        Block genesis = new Block(0, 0, "TEST_BLK".getBytes(), puzzle, sig);
        ArrayList<Block> list = new ArrayList<>();
        list.add(genesis);
        Blockchain.update(list);
        // Now update with genesis...!

        System.out.println("Done!");

    }
}
