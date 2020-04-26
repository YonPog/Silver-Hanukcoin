import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageParserTest {

    @Test
    public void toMessage() {

    }

    @Test
    public void All() {
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
         */

        byte[] b1 = {0, 0, 0, 1, (byte) 0xBE, (byte) 0xef, (byte) 0xbe, (byte) 0xef, 0, 0, 0, 2,
                9, 116, 101, 115, 116, 95, 110, 97, 109, 101, 7, 49, 46, 51, 46, 51, 46, 55, 0, 42, 0, 0, 0, 123,
                6, 115, 105, 108, 118, 101, 114, 7, 53, 46, 52, 46, 51, 46, 50, 2, 55, 0, 0, 3, (byte) 219};

        MessageParser parser = new MessageParser(b1);
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

    }

}