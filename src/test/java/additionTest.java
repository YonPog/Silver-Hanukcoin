import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class additionTest {

    @Test
    public void add() {
        Assert.assertEquals(3, addition.add(1, 2));
    }
}