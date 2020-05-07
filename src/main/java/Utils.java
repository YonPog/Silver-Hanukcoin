import java.util.ArrayList;
import java.util.Arrays;

public class Utils {
    /**
     * @param data At least 1 byte long.
     * @return Unsigned integer value of data in big endian
     */
    public static int bytesToInt(byte[] data){
        int n = data.length;
        int ret = data[n - 1] & 0xFF;
        for (int i = n - 2; i >= 0; i--) {
            // Masking to cancel sign extension.
            ret |= (data[i] & 0xFF) << (8 * (n - i - 1));
        }

        return ret;
    }

    /**
     * @param n      A positive integer to convert to a byte array.
     * @param buffer The length of the expected array (number of bytes). Must be positive.
     * @return The big endian byte array of length buffer representing n.
     */
    public static byte[] intToBytes(int n, int buffer) {
        byte[] ret = new byte[buffer];
        for (int i = buffer - 1; i >= 0; --i) {
            ret[i] = (byte) (n & 0xFF);
            n = n >> 8;
        }

        return ret;
    }

    private static void copyInto(byte[] arr, byte[] base, int offset) {
        for (int i = 0; i < arr.length && i + offset < base.length; ++i) {
            base[offset + i] = arr[i];
        }
    }

    /**
     * @param arrays Variable number of byte arrays.
     * @return The concatenation of all the arrays into one array.
     */
    public static byte[] concat(byte[]... arrays) {
        if (arrays.length == 0) {
            return new byte[0];
        }
        int sumLengths = 0;
        for (byte[] arr : arrays) {
            sumLengths += arr.length;
        }
        byte[] merged = Arrays.copyOf(arrays[0], sumLengths);
        int offset = 0;
        for (byte[] arr : arrays) {
            copyInto(arr, merged, offset);
            offset += arr.length;
        }
        return merged;
    }

    public static double log(double base, double logNumber) {
        return Math.log(logNumber) / Math.log(base);
    }

    /**
     * @param s A byte string of format like "AB CD EF 0A 6D"
     * @return The corresponding byte array
     */
    public static byte[] parseByteStr(String s) {
        ArrayList<Byte> a = new ArrayList<Byte>();
        for (String hex : s.split("\\s+")) {
            byte b = (byte) Integer.parseInt(hex, 16);
            a.add(b);
        }
        byte[] result = new byte[a.size()];
        for (int i = 0; i < a.size(); i++) {
            result[i] = a.get(i);
        }
        return result;
    }

}
