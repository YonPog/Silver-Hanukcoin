import com.mongodb.*;
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

    private static DBCollection collection;


    public static void initMongoDB() {
//        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
//        mongoLogger.setLevel(Level.OFF);
////        MongoClient mongoClient = new MongoClient("mongodb+srv://Yuval:rq3vX11VmZOR6iho@silver-xb6ug.gcp.mongodb.net/test?retryWrites=true&w=majority");
//        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://Yuval:rq3vX11VmZOR6iho@silver-xb6ug.gcp.mongodb.net/test?retryWrites=true&w=majority"));
////        MongoDatabase database = mongoClient.getDatabase("Blockchain");
//        DB database = mongoClient.getDB("Blockchain");
////        collection = database.getCollection("blockchain");
//        collection = database.getCollection("blockchain");
    }

    public static void saveToMongoDB() {
        System.out.println("[*] saving to MongoDB");
//        for (Block b : blockchain) {
//////            collection.insertOne(b.toDocument());
////            DBObject person = new BasicDBObject(b.toDocument());
////            collection.insert(person);
////        }
        System.out.println("[*] done!");
    }

    /**
     * Initializes the data with the saved nodes and blocks.
     */
    public static void init() throws Exception {
        //byte[] bytes = Utils.parseByteStr("00 00 00 00  00 00 00 00    54 45 53 54  5F 42 4C 4B    71 16 8F 29  D9 FE DF F9    BF 3D AE 1F  65 B0 8F 66    AB 2D B5 1E");
        //DataOutputStream writeStream = new DataOutputStream(new FileOutputStream(BLOCKCHAIN_FILE));
        //writeStream.write(bytes);
        //blocksInFile = 1;

        // TODO
        initMongoDB();
        loadNodeList();
        loadBlockchain();
    }

    public static void wipe() throws IOException {
        DataOutputStream writeStream = new DataOutputStream(new FileOutputStream(BLOCKCHAIN_FILE));
        byte[] bytes = Utils.parseByteStr("00 00 00 00  00 00 00 00    54 45 53 54  5F 42 4C 4B    71 16 8F 29  D9 FE DF F9    BF 3D AE 1F  65 B0 8F 66    AB 2D B5 1E");
        writeStream.write(bytes);
        blocksInFile = 1;
    }

    public static ConcurrentHashMap<Pair<String, Integer>, Node> getNodes() {
        return nodes;
    }

    public static Node getNode(Pair<String, Integer> pair) {
        return nodes.get(pair);
    }

    public static void setNode(Pair<String, Integer> pair, Node n) throws IOException {
        nodes.put(pair, n);
        saveNodeList();
    }

    public static void loadNodeList() throws Exception {
        DataInputStream stream = new DataInputStream(new FileInputStream(NODES_FILE));
        ArrayList<Node> nodeList = new MessageParser(stream).toMessage().getNodes();
        // fill up node list
        for (Node n : nodeList) {
            nodes.put(new Pair<>(n.getHost(), n.getPort()), n);
        }

    }

    /**
     * Saves the nodes to the file.
     */
    public static void saveNodeList() throws IOException {
        DataOutputStream stream = new DataOutputStream(new FileOutputStream(NODES_FILE));
        ArrayList<Node> nodeList = new ArrayList<>(nodes.values());
        Message msg = new Message(1, nodeList, new ArrayList<Block>());
        stream.write(msg.toBytes());
    }

    public static void loadBlockchain() throws IOException {
        DataInputStream stream = new DataInputStream(new FileInputStream(BLOCKCHAIN_FILE));
        int numblocks = 1;
        while (stream.available() > 0) {
            blockchain.add(Block.parseBlock(stream));
            ++numblocks;
        }
        blocksInFile = numblocks;
    }

    /**
     * Saves the blocks to the file.
     */
    public static void saveBlockchain() throws IOException {
        DataOutputStream writeStream = new DataOutputStream(new FileOutputStream(BLOCKCHAIN_FILE, true));
        int oldBlocks = blocksInFile;
        for (int i = oldBlocks; i < blockchain.size(); ++i) {
            writeStream.write(blockchain.get(i).toBytes());
        }
        blocksInFile = blockchain.size();

        // TODO
        saveToMongoDB();
    }

    public static int update(ArrayList<Block> newBlockchain) throws NoSuchAlgorithmException, IOException {
        if (!isUpdateNeeded(newBlockchain)) {
            System.out.println("[*] no change in the blockchain");
            return 0;
        }

        if (newBlockchain.size() > blockchain.size()) {
            blockchain = newBlockchain;
            saveBlockchain();
            return 1;
        }

        return 2;
    }

    public static void update(Block newBlock) throws IOException, NoSuchAlgorithmException {
        System.out.println(Arrays.toString(newBlock.toBytes()));
        if (!isValidContinuation(newBlock)){
            System.out.println("[!] -------------------------------------------------WRONG!");
            return;
        }
        blockchain.add(newBlock);
        saveBlockchain();
        System.out.println("[*] Mined a new block!");
    }

    public static ArrayList<Block> getBlocks() {
        return blockchain;
    }

    public static Block getLatestBlock() { return blockchain.get(blockchain.size() - 1); }

    public static int getBlockchainLength() { return blockchain.size(); }

    public static boolean isValidContinuation(Block newBlock) throws NoSuchAlgorithmException {
        ArrayList<Block> newBlockchain = new ArrayList<>(blockchain);
        newBlockchain.add(newBlock);
        return isUpdateNeeded(newBlockchain);

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
        //find until where the chains add up
        int lastCommon = findLastCommonBlock(newBlockchain);
        System.out.println("last common block " + blockchain.get(lastCommon));
        if (lastCommon < 0){
            System.out.println("wrong genesis");
            return false; //wrong genesis
        }

        if (blockchain.size() == newBlockchain.size()){ //we need to take the one with the lower puzzle
            System.out.println("chose other one because puzzle shorter");
            return false; //TODO
        }


        //now validate the next blocks
        for (int i = lastCommon+1 ; i < newBlockchain.size() ; ++i){
            Block newBlock = newBlockchain.get(i);
            byte[] digest = newBlock.calcMD5();

            //check if serial number is one more than previous one
            if (newBlock.getSerial_number() != newBlockchain.get(i - 1).getSerial_number() + 1) {
                return false;
            }

            //check if wallet number is different than the last one
            if (newBlock.getWallet() == newBlockchain.get(i - 1).getWallet()) {
                return false;
            }

            //check if signature matches up
            if (!Arrays.equals(Arrays.copyOfRange(digest, 0, 12), newBlock.getSig())) {
                // TODO temporary, for debugging purposes
                System.out.println(Arrays.toString(newBlock.toBytes()));
                System.out.println(Arrays.toString(newBlock.calcMD5()));
                return false;
            }

            //check if puzzle is solved
            int index = 15; //iterating from end to start
            int numZerosToCheck = newBlock.calcNZ();
            while (numZerosToCheck >= 8) {
                if (digest[index] != 0) {
                    System.out.println("wrong sig, " + index);
                    return false;
                }
                --index;
                numZerosToCheck -= 8;
            }

            //there are less than 8 bits to check
            if (numZerosToCheck > 0) { //there are bits left
                if ((digest[index] & ((1 << numZerosToCheck) - 1)) != 0) { //mask
                    System.out.println("wrong sig, ()" + index);
                    return false;
                }
            }
        }
        return true;
    }

    /*
    @pre blockchain.size() <= newBlockchain.size()
     */
    public static int findLastCommonBlock(ArrayList<Block> newBlockchain){
        for (int i = blockchain.size() - 1 ; i >= 0 ; --i){
            if (blockchain.get(i).equals(newBlockchain.get(i))){
                return i;
            }
        }
        return -1 ;
    }


}
