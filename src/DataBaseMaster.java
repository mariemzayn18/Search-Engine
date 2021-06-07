import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;

public class DataBaseMaster {

    MongoClient mongo;
    MongoDatabase database;
    // Indexer MyIndxer = new Indexer();

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
        it.close();
        return list;

    }
    public String RetreiveURL(String thread_name){

        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        Document iterDoc = collection.find(new BasicDBObject("threadname", thread_name)).sort(new BasicDBObject("_id", -1)).first();
        return iterDoc.get("URL").toString();
    }
    
    public int RetreiveThreadNum(String thread_name){

        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        Document iterDoc = collection.find().first();
        return Integer.parseInt(iterDoc.get("threadnum").toString());
    }

    public int DBCount(){
        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        long iterDoc = collection.count();
        return (int)iterDoc ;
        
    }
    

    public List<Document> retriveIndexes() {
        MongoCollection<Document> collection = database.getCollection("Indexers");
        FindIterable<Document> iterDoc = collection.find();
        MongoCursor<Document> it = iterDoc.iterator();
        List<Document> list = new ArrayList<>();

        while (it.hasNext()) {
            list.add((Document) (it.next()));
        }
        BasicDBObject document = new BasicDBObject();

        it.close();
        // Delete All documents from collection Using blank BasicDBObject
        collection.deleteMany(document);
        return list;

    }

    ///////////////////////////////// check for duplicate documents
    ///////////////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    public String found(String paramName, String Checking, String collectionname) {
        MongoCollection<Document> collection = database.getCollection(collectionname);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put(paramName, Checking);

        FindIterable<Document> cursor = collection.find(whereQuery).limit(1);
        MongoCursor<Document> iterator = cursor.iterator();

        if (iterator.hasNext()) {
            String URL = iterator.next().get("URL").toString();
            iterator.close();
            return URL;
        } else {
            iterator.close();
            return null;
        }

    }

    public void deleteDocument(String URL) {

        MongoCollection<Document> collection = database.getCollection("WebCrawler");

        BasicDBObject document = new BasicDBObject();
        document.put("URL", URL);
        collection.deleteOne(document);

    }

    public void insertDocument(String documenString, String URL,String Thread_name ,int Thread_num ) {
        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        Document document = new Document("URL", URL).append("Document", documenString)
        .append("threadname", Thread_name).append("threadnum",Thread_num);
        // Inserting document into the collection
        collection.insertOne(document);
        System.out.println("Document inserted successfully");

    }

    // insert the final hasht able into data base
    public void insertDocs(Hashtable<String, List<Document>> Table,Vector<String> Spam_URLs) {
        MongoCollection<Document> collection = database.getCollection("Indexers");
        List<Document> docs = new ArrayList<Document>();
        for (String key : Table.keySet()) {
            Document document = new Document("Word", key);
            List<Document> documents = new ArrayList<Document>();
            for (int i = 0; i < Table.get(key).size(); i++) {
                Document data = Table.get(key).get(i);
                Document URLS = new Document();
                String url = String.valueOf(data.get("URL"));
                String title = String.valueOf(data.get("title"));
                String TF = String.valueOf(data.get("TF"));
                String Doc = String.valueOf(data.get("Content"));
                String IDF = String.valueOf(data.get("IDF"));
                
                
                // chcek if this url contains spam skip this url and don't insert it into DB
                URLS.append("URL", url);
                URLS.append("title", title);
                // note didn't sort the list yet :(
                    URLS.append("TF", TF).append("IDF", IDF).append("Content", Doc);
                    documents.add(URLS);
                
            }
            
            document.append("URLS", documents);
            docs.add(document);
        }
        collection.insertMany(docs);
        System.out.println("hii after insert many");

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
            iterator.close();
            return docs;
        } catch (Exception e) {
            iterator.close();
            return null;
        }

    }

    public void DeleteAllDocs(String collectionname) {
        MongoCollection<Document> collection = database.getCollection(collectionname);
        BasicDBObject document = new BasicDBObject();

        // Delete All documents from collection Using blank BasicDBObject
        collection.deleteMany(document);
        return;
    }

    public void UpdateDocument(String URL, String Document,String Thread_name,int Thread_num) {

        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        Document Updated = new Document("URL", URL).append("Document", Document).append("threadname", Thread_name).append("threadnum",Thread_num); ;
        collection.findOneAndReplace(new Document("URL", URL), Updated);
        System.out.println("indexer Updated successfully");
        return;

    }

    public void close() {
        mongo.close();
    }
}