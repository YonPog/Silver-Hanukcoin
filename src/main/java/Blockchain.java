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
    }

    public static void loadFromDB() {
        //TODO
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
        if (!isChainValid(newBlockcahin)) {
            System.out.println("Received invalid blockchain!");
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

    public static boolean isChainValid(ArrayList<Block> newBlockcahin) throws NoSuchAlgorithmException {
        //if chain is empty, take the next one
        if  (blockchain.size() == 0 || blockchain.size() == newBlockcahin.size()){
            return true;
        }

        //first, check the new chain is longer.
        if (newBlockcahin.size() <= blockchain.size()) {
            return false;
        }
        //check that the chains match up
        if (blockchain.size() > 0 && (!blockchain.get(blockchain.size()-1).equals(newBlockcahin.get(blockchain.size() -1))) ) {
            return false;
        }
        //now validate the next blocks
        for (int i = blockchain.size() ; i < newBlockcahin.size() ; ++i){
            Block newBlock = newBlockcahin.get(i);
            byte[] digest = newBlock.calcSig();

            //check if serial number is one more than previous one
            if (newBlock.getSerial_number() != newBlockcahin.get(i-1).getSerial_number() + 1) {
                return false;
            }

            //check if wallet number is different than the last one
            if (newBlock.getWallet() == newBlockcahin.get(i-1).getWallet()) {
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
