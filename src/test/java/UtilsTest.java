import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void bytesToInt() {
        Assert.assertArrayEquals(Utils.intToBytes(0, 2), new byte[] {0, 0});
        Assert.assertArrayEquals(Utils.intToBytes(1, 2), new byte[]{0, 0x1});
        Assert.assertArrayEquals(Utils.intToBytes(4660, 2), new byte[]{0x12, 0x34});

        byte[] test4 = Utils.intToBytes(Utils.bytesToInt(new byte[] {0, 0, 0x25, 0x3, 0, 0x4}),6);
        Assert.assertArrayEquals(test4, new byte[] {0, 0, 0x25, 0x3, 0, 0x4});
    }

    @Test
    public void intToBytes() {
        int test1 = Utils.bytesToInt(Utils.intToBytes(1337, 10));
        Assert.assertEquals(test1, 1337);
    }
}