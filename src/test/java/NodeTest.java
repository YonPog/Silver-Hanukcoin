import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeTest {

    @Test
    public void toBytes() {
        /*
        name = easter_egg_for_tal
        host = 1.1.1.1
        port = 73
        last_seen_ts = 626572800
         */

        Node n1 = new Node("easter_egg_for_tal", "1.1.1.1", 73, 626572800);
        byte[] b1 = {18, 101, 97, 115, 116, 101, 114, 95, 101, 103, 103, 95, 102, 111, 114, 95, 116, 97, 108,
                    7, 49, 46, 49, 46, 49, 46, 49,
                    0, 73,
                    0x25, 0x58, (byte) 0xBE, 0};

        Assert.assertArrayEquals(n1.toBytes(), b1);

        /*
        name = 5p3c!4l_@ar4a@ters
        host = 123.2.3.4
        port = 11235
        last_seen_ts = 945720000
         */

        Node n2 = new Node("5p3c!4l_@ar4a@ters", "123.2.3.4", 11235, 945720000);
        byte[] b2 = {18, 53, 112, 51, 99, 33, 52, 108, 95, 64, 97, 114, 52, 97, 64, 116, 101, 114, 115,
                    9, 49, 50, 51, 46, 50, 46, 51, 46, 52,
                    0x2B, (byte)0xE3,
                    0x38, 0x5E, (byte)0x8A, (byte)0xC0};

        Assert.assertArrayEquals(n2.toBytes(), b2);
    }

}