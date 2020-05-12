import java.util.ArrayList;
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
        public void run(){

        }
    }
}
