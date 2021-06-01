import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;

public class DataBaseMaster {

    MongoClient mongo;
    MongoDatabase database;

    DataBaseMaster() {

        try {
            // Creating a Mongo client
            mongo = new MongoClient("localhost", 27017);

            // Accessing the database
            database = mongo.getDatabase("myDatabase");

            // Creating collections
            database.createCollection("WebCrawler");

            database.createCollection("Indexer");

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println("Collections created successfully");

    }

    public List<Document> retriveDocuments() {
        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        FindIterable<Document> iterDoc = collection.find();
        MongoCursor<Document> it = iterDoc.iterator();
        List<Document> list = new ArrayList<>();

        while (it.hasNext()) {
            list.add((Document) (it.next()));
        }
        return list;

    }


///////////////////////////////// check for duplicate documents \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    public boolean found(String paramName, String Checking, String collectionname) {
        MongoCollection<Document> collection = database.getCollection(collectionname);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put(paramName, Checking);

        FindIterable<Document> cursor = collection.find(whereQuery);
        MongoCursor<Document> iterator = cursor.iterator();

        if (iterator.next() != null) {
            return true;
        } else
            return false;

    }

    public void insertDocument(String documenString, String URL) {
        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        Document document = new Document("Document", documenString).append("URL", URL);
        // Inserting document into the collection
        collection.insertOne(document);
        System.out.println("Document inserted successfully");

    }

  
    public void insertDocs(Hashtable<String, List<Document>> Table) {
        MongoCollection<Document> collection = database.getCollection("Indexers");
        List<Document> docs = new ArrayList<Document>();
        for (String key : Table.keySet()) {
            Document document = new Document("Word", key);
            for (int i = 0; i < Table.get(key).size(); i++) {
                Document data = Table.get(key).get(i);
                String url = String.valueOf(data.get("URL"));
                document.append("URL", url);
                document.append("TF", 1);
            }
            docs.add(document);
        }
        collection.insertMany(docs);

    }
}