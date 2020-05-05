import com.mongodb.DBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Blockchain {
    private static ArrayList<Block> blockchain = new ArrayList<Block>();
    private static MongoCollection<Document> collection;


    public static void init() {
        MongoClient mongoClient = MongoClients.create("mongodb+srv://Yuval:rq3vX11VmZOR6iho@silver-xb6ug.gcp.mongodb.net/test?retryWrites=true&w=majority");
        MongoDatabase database = mongoClient.getDatabase("Blockchain");
        collection = database.getCollection("blockchain");
    }

    public static void loadFromDB() {

    }

    public static void saveToDB(ArrayList<Block> newBlockcahin) {
        int commonBlockIndex = getlatestCommonBlock(newBlockcahin);
        for (int i = commonBlockIndex; i < newBlockcahin.size(); i++) {
            collection.findOneAndUpdate(newBlockcahin.get(i).toDocment(), newBlockcahin.get(i).toDocment());
        }
    }

    private static int getlatestCommonBlock(ArrayList<Block> newBlockcahin) {
        for (int i = 0; i < blockchain.size(); i++) {
            if (!newBlockcahin.get(i).equals(blockchain.get(i))) {
                return (i-1) > 0 ? i-1 : 0;
            }
        }
        return -1;
    }

    public static int update(ArrayList<Block> newBlockcahin) {
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

    public static Boolean isChainValid(ArrayList<Block> newBlockcahin) {
        // Ishai's job
        return true;
    }

    public ArrayList<Block> getBlocks() {
        return blockchain;
    }


}
