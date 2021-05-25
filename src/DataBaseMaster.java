import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.Iterator;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;

public class DataBaseMaster {

    MongoClient mongo;
    MongoDatabase database;

    DataBaseMaster() {
        // Creating a Mongo client
        mongo = new MongoClient("localhost", 27017);

        // Accessing the database
        database = mongo.getDatabase("myDatabase");

        // Creating a collection

        try {
            database.createCollection("Documents");
            database.createCollection("Indexers");

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        // database.createCollection("Search-Engine");
        System.out.println("Collection created successfully");

    }

    public String[] retriveDocument(String collectionname) {
        MongoCollection<Document> collection = database.getCollection(collectionname);
        FindIterable<Document> iterDoc = collection.find();
        String[] Documents = {};
        Iterator it = iterDoc.iterator();
        int i = 0;
        while (it.hasNext()) {
            Documents[i] = (it.next()).toString();
        }
        return Documents;
    }

    public boolean found(String document) {
        MongoCollection<Document> collection = database.getCollection("Documents");
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("Document", document);

        FindIterable<Document> cursor = collection.find(whereQuery);
        if (cursor != null) {
            return false;
        }
        return true;

    }

    public void insertDocument(String documenString) {
        MongoCollection<Document> collection = database.getCollection("Documents");
        Document document = new Document("Document", documenString);
        // Inserting document into the collection
        collection.insertOne(document);
        System.out.println("Document inserted successfully");

    }

    public void insertDocument(String Indexed, float TF) {
        MongoCollection<Document> collection = database.getCollection("Indexers");
        Document document = new Document("Index", Indexed).append("TF", TF);
        // Inserting document into the collection
        collection.insertOne(document);
        System.out.println("indexer inserted successfully");

    }

}