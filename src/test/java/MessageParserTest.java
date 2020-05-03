import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import static org.junit.Assert.*;

public class MessageParserTest {

    @Test
    public void toMessage() {

    }

    @Test
    public void All() throws Exception {
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
                0x12, 0x34, 0x56, 0x78, 0, (byte)0xC0, (byte)0xFF, (byte)0xEE, 1, 2, 4, 8, 16, 32, 64, (byte)128, 0, 3, 6, 9, 12, 15, 18, 21, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120};

        MessageParser parser = new MessageParser(new DataInputStream(new ByteArrayInputStream(b1)));
        Message message = parser.toMessage();

        Assert.assertEquals(message.getCmd(), 1);
        Assert.assertEquals(message.getNodes_count(), 2);

        Assert.assertEquals(message.getNodes().get(0).getName(), "test_name");
        Assert.assertEquals(message.getNodes().get(0).getHost(), "1.3.3.7");

        Assert.assertEquals(message.getNodes().get(0).getPort(), 42);
        Assert.assertEquals(message.getNodes().get(0).getLast_seen_ts(), 123);
        Assert.assertEquals(message.getNodes().get(1).getName(), "silver");

        Assert.assertEquals(message.getNodes().get(1).getHost(), "5.4.3.2");
        Assert.assertEquals(message.getNodes().get(1).getPort(), 567);
        Assert.assertEquals(message.getNodes().get(1).getLast_seen_ts(), 987);

        Assert.assertEquals(message.getBlocks_count(), 2);

        Assert.assertEquals(message.getBlocks().get(0).getSerial_number(), 65);
        Assert.assertEquals(message.getBlocks().get(0).getWallet(), 49);
        Assert.assertArrayEquals(message.getBlocks().get(0).getPrev_sig(), new byte[]{0, 1, 2, 3, 4, 5, 6, 7});
        Assert.assertArrayEquals(message.getBlocks().get(0).getPuzzle(), new byte[]{12, 13, 14, 15, 16, 17, 18, 19});
        Assert.assertArrayEquals(message.getBlocks().get(0).getSig(), new byte[]{23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34});

        Assert.assertEquals(message.getBlocks().get(1).getSerial_number(), 305419896);
        Assert.assertEquals(message.getBlocks().get(1).getWallet(), 12648430);
        Assert.assertArrayEquals(message.getBlocks().get(1).getPrev_sig(), new byte[]{1, 2, 4, 8, 16, 32, 64, (byte)128});
        Assert.assertArrayEquals(message.getBlocks().get(1).getPuzzle(), new byte[]{0, 3, 6, 9, 12, 15, 18, 21});
        Assert.assertArrayEquals(message.getBlocks().get(1).getSig(), new byte[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120});

        System.out.println("All done !");

    }

}