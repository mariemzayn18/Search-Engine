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

    public FindIterable<Document> retriveDocuments() {

        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        return collection.find();

    }

    public Document found(String paramName, String Checking, String collectionname) {
        MongoCollection<Document> collection = database.getCollection(collectionname);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put(paramName, Checking);

        FindIterable<Document> cursor = collection.find(whereQuery);
        MongoCursor<Document> iterator = cursor.iterator();

        if (cursor != null) {
            return iterator.next();
        }
        return null;

    }

    public void insertDocument(String documenString, String URL) {
        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        Document document = new Document("Document", documenString).append("URL", URL);
        // Inserting document into the collection
        collection.insertOne(document);
        System.out.println("Document inserted successfully");

    }

    public void insertDocument(String Indexed, float TF, String URL) {
        MongoCollection<Document> collection = database.getCollection("Indexers");
        Document document = new Document("Index", Indexed).append("URL", URL).append("TF", TF);
        // check if exist already(if yes just append it)
        Document found = found("Index", Indexed, "Indexers");
        if (found != null) {
            found.append("Document", document);
            collection.updateOne(new Document("Index", Indexed), found);
        }
        // Inserting document into the collection
        collection.insertOne(document);
        System.out.println("indexer inserted successfully");

    }

    public void insertDocs(Hashtable<String, List<Document>> Table) {
        MongoCollection<Document> collection = database.getCollection("Indexers");
        Enumeration<String> enumeration = Table.keys();
        List<Document> docs = new ArrayList<Document>();
        // iterate using enumeration object
        while (enumeration.hasMoreElements()) {

            String key = enumeration.nextElement();
            Document document = new Document("Word", key);
            for (int i = 0; i < Table.get(key).size(); i++) {
                String data = Table.get(key).get(i).toString();
                int indexTF = data.indexOf("TF");
                int url = data.indexOf("URL");
                document.append("URL", url);
                document.append("TF", indexTF);
            }
            docs.add(document);
        }

    }
}