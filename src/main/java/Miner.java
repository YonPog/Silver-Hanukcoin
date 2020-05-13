import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public class Miner extends Thread{
    private int maxThreads;
    private ConcurrentLinkedQueue<Block> solutions = new ConcurrentLinkedQueue<>();
    private ArrayList<SolverThread> threads = new ArrayList<>();
    private Block lastBlock;
    private Server server;
    public AtomicBoolean lastBlockIsOurs;
    public AtomicBoolean blockchainChanged;


    public Miner(int maxThreads, Server server){
        this.server = server;
        lastBlock = Database.getLatestBlock(); //get the latest block
        this.maxThreads = maxThreads;
        //checking if last block is ours
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.format("[!] ERROR no such algorithm MD5\nDetails:\n%s", e.toString());
            return;
        }
        byte[] nameSig = Arrays.copyOfRange(md5.digest(Main.NAME.getBytes()), 0, 4);
        int wallet = Utils.bytesToInt(nameSig);
        this.lastBlockIsOurs = new AtomicBoolean(lastBlock.getWallet() == wallet);
        //done!

        this.blockchainChanged = new AtomicBoolean(false);
        for (int i = 0; i < maxThreads; ++i) {
            SolverThread st = new SolverThread(lastBlock, i); // make sure serial number seeds are ok.
            threads.add(st);
        }
    }

    public void updateBlock(Block newBlock){
        if (newBlock.getSerial_number() > lastBlock.getSerial_number()){
            lastBlock = newBlock;
        }
        //refresh all solver threads.
        refresh();
    }

    private void refresh(){
        for (SolverThread t : threads){
            System.out.println("killed " + t.toString());
            t.kill();
        }
        threads.clear();
        //wait until next block is mined...
        while (lastBlockIsOurs.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("[!] ERROR waiting\nDetails:\n" + e.toString() + "\n");
            }
        }
        //now init new threads
        System.out.println("[*] Mining a new block: " + lastBlock.toString());
        for (int i = 0 ; i < maxThreads ; ++i){
            SolverThread st = new SolverThread(lastBlock, i); // make sure serial number seeds are ok.
            st.start();
            threads.add(st);
        }
    }

    @Override
    public void run() {

        while (lastBlockIsOurs.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("[!] ERROR waiting\nDetails:\n" + e.toString() + "\n");
            }
        }

        System.out.println("[*] initialized miner and started mining on new block: " + lastBlock.toString());
        for (SolverThread st : threads){
            st.start();
        }


        while (true) {
            //check if anyone has solved the riddle.
            if (!solutions.isEmpty()) {
                Block candidate = solutions.remove();
                server.parseSolvedPuzzle(candidate);
            }

            //check if blockchain changed
            if (blockchainChanged.compareAndSet(true, false)) {
                lastBlock = Database.getLatestBlock();
                refresh();
            }

        }
    }

    private class SolverThread extends Thread{
        private Block lastBlock;
        private final long seed;
        private AtomicBoolean alive;

        public SolverThread(Block lastBlock, long seed){
            this.lastBlock = lastBlock;
            this.seed = seed;
            alive = new AtomicBoolean(true);
        }

        public void kill(){
            alive.set(false);
        }

        public void setLastBlock(Block newBlock){
            this.lastBlock = newBlock;
        }

        @Override
        public void run() {
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                System.out.format("[!] ERROR no such algorithm MD5\nDetails:\n%s", e.toString());
                return;
            }
            Random generator = new Random(seed);
            int serial = lastBlock.getSerial_number() + 1;
            byte[] nameSig = Arrays.copyOfRange(md5.digest(Main.NAME.getBytes()), 0, 4);
            int wallet = Utils.bytesToInt(nameSig);
            byte[] prevSig = Arrays.copyOfRange(lastBlock.getSig(), 0, 8);

            byte[] puzzle = new byte[8];
            Outer:
            while (this.alive.get()) {
//                ++tries;
//                if (tries % 1000000 == 0){
//                    System.out.println("1000000 tries on " + this.toString());
//                    tries = 0;
//                }

                while (lastBlockIsOurs.get()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("[!] ERROR waiting\nDetails:\n" + e.toString() + "\n");
                    }
                }
                generator.nextBytes(puzzle);
                Block b = new Block(serial, wallet, prevSig, puzzle, new byte[12]);
                byte[] hash;
                try {
                    hash = b.calcMD5(); //why isnt the digest cut down? TODO
                } catch (NoSuchAlgorithmException e) {
                    System.out.format("[!] ERROR no such algorithm MD5\nDetails:\n%s", e.toString());
                    return;
                }
                //check if puzzle is solved
                int index = 15; //iterating from end to start
                int numZerosToCheck = b.calcNZ();
                while (numZerosToCheck >= 8) {
                    if (hash[index] != 0) {
                        continue Outer;
                    }
                    --index;
                    numZerosToCheck -= 8;
                }
                //there are less than 8 bits to check
                if (numZerosToCheck > 0) { //there are bits left
                    if ((hash[index] & ((1 << numZerosToCheck) - 1)) != 0) { //mask
                        continue;
                    }
                }
                b.setSig(Arrays.copyOfRange(hash, 0, 12));
                // if got here, solution is valid
//                try {
//                    if (!Database.isValidContinuation(b)){
//                        continue;
//                    }
//                } catch (NoSuchAlgorithmException e) {
//                    System.out.println("no such algorithm");
//                }

                solutions.add(b);
                break;

            }
        }
    }
}
