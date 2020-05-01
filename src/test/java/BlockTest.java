import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class BlockTest {

    @Test
    public void toBytes() {
        // NOT VALID BLOCKS, JUST TESTS
        /*
            serial_number = 13
            wallet = 43981
            prev_sig = {6, 7, 8, 9, 10, 11, 12, 13}
            puzzle = {14, 16, 18, 20, 22, 24, 26, 28}
            sig = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}
         */

        Block b1 = new Block(13, 43981, new byte[]{6, 7, 8, 9, 10, 11, 12, 13},
                new byte[]{14, 16, 18, 20, 22, 24, 26, 28},
                new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});

        byte[] arr1 = {0, 0, 0, 13, 0, 0, (byte) 0xAB, (byte) 0xCD, 6, 7, 8, 9, 10, 11, 12, 13,
                14, 16, 18, 20, 22, 24, 26, 28, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

        Assert.assertArrayEquals(arr1, b1.toBytes());
    }
}