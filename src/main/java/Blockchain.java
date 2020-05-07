import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class Blockchain {
    private static ArrayList<Block> blockchain = new ArrayList<>();
    private static MongoCollection<Document> collection;


    public static void init() {
        MongoClient mongoClient = MongoClients.create("mongodb+srv://Yuval:rq3vX11VmZOR6iho@silver-xb6ug.gcp.mongodb.net/test?retryWrites=true&w=majority");
        MongoDatabase database = mongoClient.getDatabase("Blockchain");
        collection = database.getCollection("blockchain");
        //add genesis block!
        byte[] puzzle =  Utils.parseByteStr("71 16 8F 29  D9 FE DF F9");
        byte[] sig = Utils.parseByteStr("BF 3D AE 1F  65 B0 8F 66 AB 2D B5 1E");
        // Now update with genesis...!
        ArrayList<Block> genesis = new ArrayList<>();
        genesis.add(new Block(0, 0, "TEST_BLK".getBytes(), puzzle, sig));
        //Blockchain.saveToDB(genesis);
    }

    public static void loadFromDB() {
        // TODO
    }

    public static void saveToDB(ArrayList<Block> newBlockcahin) {
        for (int i = blockchain.size() ; i < newBlockcahin.size() ; ++i) {
            collection.findOneAndUpdate(newBlockcahin.get(i).toDocument(), newBlockcahin.get(i).toDocument());
        }
    }

    private static int getlatestCommonBlock(ArrayList<Block> newBlockcahin) { //TODO useless
        return newBlockcahin.size() - blockchain.size() - 1;
    }

    public static int update(ArrayList<Block> newBlockcahin) throws NoSuchAlgorithmException {
        if (!isUpdateNeeded(newBlockcahin)) {
            System.out.println("[*] no change in the blockchain");
            return 0;
        }

        if (newBlockcahin.size() > blockchain.size()) {
            blockchain = newBlockcahin;
            saveToDB(newBlockcahin);
            return 1;
        }

//        if (newBlockcahin.get(newBlockcahin.size() - 1).getPuzzle() is greater than blockchain.get(blockchain.size() - 1).getPuzzle()) {
//            blockchain = newBlockcahin;
//            saveToDB();
//            return 2;
//        }

        return 2;
    }

    public ArrayList<Block> getBlocks() {
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
