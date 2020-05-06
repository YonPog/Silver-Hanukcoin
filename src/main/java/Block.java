import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import org.bson.Document;

public class Block {
    private int serial_number;
    private int wallet;
    private byte[] prev_sig; //8
    private byte[] puzzle; //8
    private byte[] sig; //12

    public Block(int serial_number, int wallet, byte[] prev_sig, byte[] puzzle, byte[] sig) {
        this.serial_number = serial_number;
        this.wallet = wallet;
        this.prev_sig = prev_sig;
        this.puzzle = puzzle;
        this.sig = sig;
    }

    @Override
    public String toString(){
        return String.format("<Block #%d from wallet #%d>\n", serial_number, wallet);
    }

    public String toString(boolean full) {
        if (full) {
            return String.format("<Block #%d from wallet #%d\n" +
                    "puzzle: %s\n" +
                    "signature: %s\n", serial_number, wallet, Arrays.toString(puzzle), Arrays.toString(sig));
        }

        return this.toString();
    }

    /**
     * @return The byte representation of the block to be sent, in a byte array.
     */
    public byte[] toBytes() {
        byte[] serNum = Utils.intToBytes(this.serial_number, 4);
        byte[] walletArray = Utils.intToBytes(this.wallet, 4);
        return Utils.concat(serNum, walletArray, prev_sig, puzzle, sig);
    }

    public int getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(int serial_number) {
        this.serial_number = serial_number;
    }

    public int getWallet() {
        return wallet;
    }

    public void setWallet(int wallet) {
        this.wallet = wallet;
    }

    public byte[] getPrev_sig() {
        return prev_sig;
    }

    public void setPrev_sig(byte[] prev_sig) {
        this.prev_sig = prev_sig;
    }

    public byte[] getPuzzle() {
        return puzzle;
    }

    public void setPuzzle(byte[] puzzle) {
        this.puzzle = puzzle;
    }

    public byte[] getSig() {
        return sig;
    }

    public void setSig(byte[] sig) {
        this.sig = sig;
    }

    public int calcNZ() {
        return (int) (21 + Math.floor(Utils.log(2, this.serial_number)));
    }

    public Document toDocment() {
        return new Document("serial_number", this.getSerial_number())
                .append("wallet", this.getWallet())
                .append("prev_sig", this.getPrev_sig())
                .append("puzzle", this.getPuzzle())
                .append("sig", this.getSig());
    }

    public byte[] calcSig() throws NoSuchAlgorithmException {
        byte[] serNum = Utils.intToBytes(this.serial_number, 4);
        byte[] walletArray = Utils.intToBytes(this.wallet, 4);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(Utils.concat(serNum, walletArray, prev_sig, puzzle));
        return md5;
    }

    public Boolean equals(Block other){
        return this.serial_number == other.serial_number &&
                this.wallet == other.wallet &&
                Arrays.equals(this.prev_sig, other.prev_sig) &&
                Arrays.equals(this.puzzle, other.puzzle) &&
                Arrays.equals(this.sig, other.sig);
    }


}
