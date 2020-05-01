import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class MessageTest {

    @Test
    public void toBytes() {
        /*
        cmd = 1
        nodes_count = 2
            name = test_name
            host = 1.3.3.7
            port = 42
            timestamp = 123

            name = silver
            host = 5.4.3.2
            port = 567
            timestamp = 987

         blocks_count = 2
         NOTE: THE BLOCKS ARE NOT VALID, THEY JUST TEST MessageParser!!
            serial = 65
            wallet = 49
            prev_sig = {0, 1, 2, 3, 4, 5, 6, 7}
            puzzle = {12, 13, 14, 15, 16, 17, 18, 19}
            sig = {23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34}

            serial = 305419896
            wallet = 12648430
            prev_sig = {1, 2, 4, 8, 16, 32, 64, 128}
            puzzle = {0, 3, 6, 9, 12, 15, 18, 21}
            sig = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120}
         */

        byte[] b1 = {0, 0, 0, 1, (byte) 0xBE, (byte) 0xef, (byte) 0xbe, (byte) 0xef,
                0, 0, 0, 2,
                9, 116, 101, 115, 116, 95, 110, 97, 109, 101, 7, 49, 46, 51, 46, 51, 46, 55, 0, 42, 0, 0, 0, 123,
                6, 115, 105, 108, 118, 101, 114, 7, 53, 46, 52, 46, 51, 46, 50, 2, 55, 0, 0, 3, (byte) 219,
                (byte) 0xDE, (byte) 0xAD, (byte) 0xDE, (byte) 0xAD, 0, 0, 0, 2,
                0, 0, 0, 65, 0, 0, 0, 49, 0, 1, 2, 3, 4, 5, 6, 7, 12, 13, 14, 15, 16, 17, 18, 19, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34,
                0x12, 0x34, 0x56, 0x78, 0, (byte) 0xC0, (byte) 0xFF, (byte) 0xEE, 1, 2, 4, 8, 16, 32, 64, (byte) 128, 0, 3, 6, 9, 12, 15, 18, 21, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120};

        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(new Node("test_name", "1.3.3.7", 42, 123));
        nodes.add(new Node("silver", "5.4.3.2", 567, 987));

        ArrayList<Block> blocks = new ArrayList<>();
        blocks.add(new Block(65, 49, new byte[]{0, 1, 2, 3, 4, 5, 6, 7},
                new byte[]{12, 13, 14, 15, 16, 17, 18, 19},
                new byte[]{23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34}));
        blocks.add(new Block(305419896, 12648430, new byte[]{1, 2, 4, 8, 16, 32, 64, (byte) 128},
                new byte[]{0, 3, 6, 9, 12, 15, 18, 21},
                new byte[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120}));

        Message m1 = new Message(1, nodes, blocks);
        Assert.assertArrayEquals(b1, m1.toBytes());
    }
}