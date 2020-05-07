import javafx.util.Pair;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class Database {
    private static ArrayList<Block> blockchain = new ArrayList<>();
    private static ConcurrentHashMap<Pair<String, Integer>, Node> nodes = new ConcurrentHashMap<>(); //host:port, node
    private static int blocksInFile = 0;
    private static final String NODES_FILE = "nodes.Silver";
    private static final String BLOCKCHAIN_FILE = "blocks.Silver";


    public static void init() throws Exception {
        loadNodelist();
        loadBlockchain();
    }

    public static ConcurrentHashMap<Pair<String, Integer>, Node> getNodes(){
        return nodes;
    }

    public static Node getNode(Pair<String, Integer> pair){
        return nodes.get(pair);
    }

    public static void setNode(Pair<String, Integer> pair, Node n) throws IOException {
        nodes.put(pair, n);
        saveNodelist();
    }

    public static void loadNodelist() throws Exception {
        DataInputStream stream = new DataInputStream(new FileInputStream(NODES_FILE));
        ArrayList<Node> nodeList = new MessageParser(stream).toMessage().getNodes();
        //fill up node list
        for (Node n : nodeList){
            n.setNew(false);
            nodes.put(new Pair<>(n.getHost(), n.getPort()), n);
        }

    }

    public static void saveNodelist() throws IOException {
        DataOutputStream stream = new DataOutputStream(new FileOutputStream(NODES_FILE));
        ArrayList<Node> nodeList = new ArrayList<>(nodes.values());
        Message msg = new Message(1, nodeList, new ArrayList<Block>());
        stream.write(msg.toBytes());
    }

    public static void loadBlockchain() throws IOException {
        DataInputStream stream = new DataInputStream(new FileInputStream(BLOCKCHAIN_FILE));
        while (stream.available() > 0){
            blockchain.add(Block.parseBlock(stream));
        }
    }

    public static void saveBlockchain(ArrayList<Block> newBlockcahin) throws IOException {
        DataOutputStream writeStream = new DataOutputStream(new FileOutputStream(BLOCKCHAIN_FILE, true));
        int oldBlocks = blocksInFile;
        for (int i = oldBlocks ; i < blockchain.size() ; ++i){
            writeStream.write(blockchain.get(i).toBytes());
        }
        blocksInFile = blockchain.size();
    }

    public static int update(ArrayList<Block> newBlockcahin) throws NoSuchAlgorithmException, IOException {
        if (!isUpdateNeeded(newBlockcahin)) {
            System.out.println("[*] no change in the blockchain");
            return 0;
        }

        if (newBlockcahin.size() > blockchain.size()) {
            blockchain = newBlockcahin;
            saveBlockchain(newBlockcahin);
            return 1;
        }

        return 2;
    }

    public static ArrayList<Block> getBlocks() {
        return blockchain;
    }

    public static boolean isUpdateNeeded(ArrayList<Block> newBlockchain) throws NoSuchAlgorithmException {
        //if chain is empty, take the next one
        if (blockchain.size() == 0 && newBlockchain.size() > 0){
            return true;
        }

        //to prevent errors
        if (newBlockchain.size() == 0 || blockchain.size() == newBlockchain.size()){
            return false;
        }

        //first, check the new chain is longer.
        if (newBlockchain.size() <= blockchain.size()) {
            return false;
        }
        //check that the chains match up
        if (!blockchain.get(blockchain.size() - 1).equals(newBlockchain.get(blockchain.size() - 1))) {
            return false;
        }
        //now validate the next blocks
        for (int i = blockchain.size() ; i < newBlockchain.size() ; ++i){
            Block newBlock = newBlockchain.get(i);
            byte[] digest = newBlock.calcSig();

            //check if serial number is one more than previous one
            if (newBlock.getSerial_number() != newBlockchain.get(i-1).getSerial_number() + 1) {
                return false;
            }

            //check if wallet number is different than the last one
            if (newBlock.getWallet() == newBlockchain.get(i-1).getWallet()) {
                return false;
            }

            //check if signature matches up
            if (!Arrays.equals(digest, newBlock.getSig())) {
                return false;
            }

            //check if puzzle is solved
            int index = 15; //iterating from end to start
            int numZerosToCheck = newBlock.calcNZ();
            while (numZerosToCheck >= 8) {
                if (digest[index] != 0) {
                    return false;
                }
                --index;
                numZerosToCheck -= 8;
            }

            //there are less than 8 bits to check
            if (numZerosToCheck > 0) { //there are bits left
                if ((digest[index] & ((1 << numZerosToCheck) - 1)) != 0) { //mask
                    return false;
                }
            }
        }
        return true;
    }



}
