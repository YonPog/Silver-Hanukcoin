public class Utils {
    /**
     * @param data At least 1 byte long.
     * @return Unsigned integer value of data in big endian.
     */
    public static int bytesToInt(byte[] data){
        int n = data.length;
        int ret = data[n - 1] & 0xFF;
        for (int i = n - 2; i >= 0; i--){
            // Masking to cancel sign extension.
            ret |= (data[i] & 0xFF) << (8 * (n - i - 1));
        }

        return ret;
    }

    public static byte[] intToBytes(int n, int buffer){
        byte[] ret = new byte[buffer];
        for (int i = buffer - 1 ; i >= 0 ; --i){
            ret[i] = (byte) (n & 0xFF);
            n = n >> 8;
        }

        return ret;
    }

}
