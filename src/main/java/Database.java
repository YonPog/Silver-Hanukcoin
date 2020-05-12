import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import javafx.util.Pair;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {
    private static ArrayList<Block> blockchain = new ArrayList<>();
    private static ConcurrentHashMap<Pair<String, Integer>, Node> nodes = new ConcurrentHashMap<>(); //host:port, node
    private static int blocksInFile = 0;
    private static final String NODES_FILE = "nodes.Silver";
    private static final String BLOCKCHAIN_FILE = "blocks.Silver";

    private static DBCollection collection;


    public static void initMongoDB() {
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.OFF);
//        MongoClient mongoClient = new MongoClient("mongodb+srv://Yuval:rq3vX11VmZOR6iho@silver-xb6ug.gcp.mongodb.net/test?retryWrites=true&w=majority");
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://Yuval:rq3vX11VmZOR6iho@silver-xb6ug.gcp.mongodb.net/test?retryWrites=true&w=majority"));
//        MongoDatabase database = mongoClient.getDatabase("Blockchain");
        DB database = mongoClient.getDB("Blockchain");
//        collection = database.getCollection("blockchain");
        collection = database.getCollection("blockchain");
    }

    public static void saveToMongoDB() {
        System.out.println("[*] saving to MongoDB");
        for (Block b : blockchain) {
//            collection.insertOne(b.toDocument());
            DBObject person = new BasicDBObject(b.toDocument());
            collection.insert(person);
        }
        System.out.println("[*] done!");
    }

    /**
     * Initializes the data with the saved nodes and blocks.
     */
    public static void init() throws Exception {
        // TODO
        initMongoDB();
        loadNodeList();
        loadBlockchain();
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
        while (stream.available() > 0) {
            blockchain.add(Block.parseBlock(stream));
        }
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

    public static void update(Block newBlock) throws IOException {
        blockchain.add(newBlock);
        saveBlockchain();
        System.out.println("[*] Mined a new block!");
    }

    public static ArrayList<Block> getBlocks() {
        return blockchain;
    }

    public static Block getLatestBlock() { return blockchain.get(0); }

    public static int getBlockchainLength() { return blockchain.size(); }

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
