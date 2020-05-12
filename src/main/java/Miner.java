import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Miner extends Thread{
    private int maxThreads;
    private ConcurrentLinkedQueue<Block> solutions;
    private ArrayList<SolverThread> threads;
    private Block lastBlock;


    public Miner(int maxThreads){
        this.maxThreads = maxThreads;
        //init threads...
    }

    public void updateBlock(Block newBlock){
        if (newBlock.getSerial_number() > lastBlock.getSerial_number()){
            lastBlock = newBlock;
        }
        refresh();
    }

    public void refresh(){
        for (SolverThread t : threads){
            t.setLastBlock(lastBlock);
        }
    }

    @Override
    public void run(){

    }

    private class SolverThread extends Thread{
        private Block lastBlock;
        private final int seed;

        public SolverThread(Block lastBlock, int seed){
            this.lastBlock = lastBlock;
            this.seed = seed;
        }

        public void setLastBlock(Block newBlock){
            this.lastBlock = newBlock;
        }

        @Override
        public void run(){

        }
    }
}
