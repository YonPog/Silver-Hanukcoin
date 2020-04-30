import java.util.Arrays;

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
        return String.format("<Block #%d from wallet #%d>", serial_number, wallet);
    }

    public String toString(boolean full){
        if (full){
            return String.format("<Block #%d from wallet #%d\n" +
                                        "puzzle: %s\n" +
                                        "signature: %s", serial_number, wallet, Arrays.toString(puzzle), Arrays.toString(sig));
        }

        return this.toString();
    }

    public byte[] toBytes(){
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
}
