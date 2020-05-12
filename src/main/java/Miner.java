import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public class Miner extends Thread{
    private int maxThreads;
    private ConcurrentLinkedQueue<Block> solutions;
    private ArrayList<SolverThread> threads;
    private Block lastBlock;
    private Server server;
    public AtomicBoolean lastBlockIsOurs;
    public AtomicBoolean blockchainChanged;


    public Miner(int maxThreads, Server server){
        this.server = server;
        lastBlock = Database.getLatestBlock(); //get the latest block
        this.maxThreads = maxThreads;
        this.lastBlockIsOurs.set(false);
        this.blockchainChanged.set(false);
        for (int i = 0 ; i < maxThreads ; ++i){
            SolverThread st = new SolverThread(lastBlock, i); // make sure serial number seeds are ok.
            st.start();
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
            t.kill();
        }
        threads.clear();
        //wait until next block is mined...
        if (lastBlockIsOurs.get()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("[!] ERROR waiting\nDetails:\n" + e.toString() + "\n");
            }
        }
        //now init new threads
        for (int i = 0 ; i < maxThreads ; ++i){
            SolverThread st = new SolverThread(lastBlock, i); // make sure serial number seeds are ok.
            st.start();
            threads.add(st);
        }
    }

    @Override
    public void run(){

        while (true){
            //check if anyone has solved the riddle.
            if (!solutions.isEmpty()){
                Block candidate = solutions.remove();
                server.parseSolvedPuzzle(candidate);
            }

            //check if blockchain changed
            if (blockchainChanged.compareAndSet(true, false)){
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
            alive.set(true);
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
            byte[] nameSig = Arrays.copyOfRange(md5.digest("Silver".getBytes()), 0, 4);
            int wallet = Utils.bytesToInt(nameSig);
            byte[] prevSig = Arrays.copyOfRange(lastBlock.getSig(), 0, 8);

            byte[] puzzle = new byte[8];
            Outer:
            while (this.alive.get()) {
                if (lastBlockIsOurs.get()) {
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
                    hash = b.calcSig();
                } catch (NoSuchAlgorithmException e) {
                    System.out.format("[!] ERROR no such algorithm MD5\nDetails:\n%s", e.toString());
                    return;
                }
                int NZ = b.calcNZ();
                int index = 15;
                while (NZ >= 8) {
                    if (hash[index] != 0) {
                        continue Outer;
                    }
                    index--;
                    NZ -= 8;
                }

                if (NZ > 0) {
                    if ((hash[index] & ((1 << NZ) - 1)) != 0) {
                        continue;
                    }
                }

                // if got here, solution is valid
                solutions.add(new Block(serial, wallet, prevSig, puzzle, Arrays.copyOfRange(hash, 0, 12)));

            }
        }
    }
}
