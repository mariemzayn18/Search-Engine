import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;

public class DataBaseMaster {

    MongoClient mongo;
    MongoDatabase database;
    Indexer MyIndxer = new Indexer();

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

    ///////////////////////////////// check for duplicate documents
    ///////////////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    public boolean found(String paramName, String Checking, String collectionname) {
        MongoCollection<Document> collection = database.getCollection(collectionname);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put(paramName, Checking);

        FindIterable<Document> cursor = collection.find(whereQuery).limit(1);
        MongoCursor<Document> iterator = cursor.iterator();

        if (iterator.hasNext()) {
            return true;
        } else
            return false;

    }

    public void deleteDocument(String URL) {

        MongoCollection<Document> collection = database.getCollection("WebCrawler");

        BasicDBObject document = new BasicDBObject();
        document.put("URL", URL);
        collection.deleteOne(document);

    }

    public void insertDocument(String documenString, String URL) {
        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        Document document = new Document("URL", URL).append("Document", documenString);
        // Inserting document into the collection
        collection.insertOne(document);
        System.out.println("Document inserted successfully");

    }

    // insert the final hasht able into data base
    public void insertDocs(Hashtable<String, List<Document>> Table) {
        MongoCollection<Document> collection = database.getCollection("Indexers");
        List<Document> docs = new ArrayList<Document>();
        for (String key : Table.keySet()) {
            Document document = new Document("Word", key);
            for (int i = 0; i < Table.get(key).size(); i++) {

                Document data = Table.get(key).get(i);
                String url = String.valueOf(data.get("URL"));
                // chcek if this url contains spam skip this url and don't insert it into DB
                if (MyIndxer.Spam_URLs.contains(url))
                    break;
                document.append("URL", url);
                // note didn't sort the list yet :(
                document.append("priority", data.get("priority"));
            }
            docs.add(document);
        }
        collection.insertMany(docs);

    }

    public List<Document> SearchIndex(String Index) {
        Stemmer Stem = new Stemmer();
        Stem.add(Index.toCharArray(), Index.toCharArray().length);
        String Search = Stem.toString();
        MongoCollection<Document> collection = database.getCollection("Indexers");
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("Word", Search);

        FindIterable<Document> cursor = collection.find(whereQuery);
        MongoCursor<Document> iterator = cursor.iterator();
        List<Document> docs = new ArrayList<Document>();
        try {
            while (iterator.hasNext()) {
                docs.add(iterator.next());

            }
            if (docs.size() == 0) {
                return null;
            }
            return docs;
        } catch (Exception e) {
            return null;
        }

    }

    public void UpdateDocument(String URL, String Document) {

        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        Document Updated = new Document("URL", URL).append("Document", Document);
        collection.findOneAndReplace(new Document("URL", URL), Updated);
        System.out.println("indexer Updated successfully");
        return;

    }

}