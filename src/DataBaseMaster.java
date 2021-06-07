import org.bson.BsonDocument;
import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import java.lang.reflect.Array;
import java.util.*;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;

import javax.print.Doc;

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

            database.createCollection("Threads_URLS");

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
        if(collection.count()!=0){ //replace the previous seeds{
            BasicDBObject document = new BasicDBObject();
            document.put("URL", URL);
            collection.deleteOne(document);

             }

    }

    public void insertDocument(String documenString, String URL,String Thread_name ,int Thread_num ) {
        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        Document document = new Document("URL", URL).append("Document", documenString)
                .append("threadname", Thread_name).append("threadnum",Thread_num);
        // Inserting document into the collection
        collection.insertOne(document);
        System.out.println("Document inserted successfully");

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
        if(collection.count()>0){
        BasicDBObject document = new BasicDBObject();

        // Delete All documents from collection Using blank BasicDBObject
        collection.deleteMany(document);
        }
        return;
    }

    public void UpdateDocument(String URL, String Document,String Thread_name,int Thread_num) {

        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        Document Updated = new Document("URL", URL).append("Document", Document).append("threadname", Thread_name).append("threadnum",Thread_num); ;
        collection.findOneAndReplace(new Document("URL", URL), Updated);
      //  System.out.println("document has been Updated successfully and its URL is "+URL);
        return;

    }

    public void UpdateDocument_forThreads(String URL, String Thread_name,int Thread_num) {

        MongoCollection<Document> collection = database.getCollection("Threads_URLS");
        Document Updated = new Document("URL", URL).append("threadname", Thread_name).append("threadnum",Thread_num); ;
        collection.findOneAndReplace(new Document("URL", URL), Updated);
        System.out.println(URL+ " from database");
      //  System.out.println("document has been Updated successfully");
        return;

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

                // chcek if this url contains spam skip this url and don't insert it into DB
                URLS.append("URL", url);
                URLS.append("title", title);
                // note didn't sort the list yet :(
                URLS.append("priority", data.get("priority"));
                documents.add(URLS);

            }

            document.append("URLS", documents);
            docs.add(document);
        }
        collection.insertMany(docs);
        System.out.println("hii after insert many");

    }





    public void insertDocument_ForInterruption(LinkedList<String>links) {
        MongoCollection<Document> collection = database.getCollection("Threads_URLS");

        List<Document> docs = new ArrayList<Document>();
        List<Document> finalIsa=new ArrayList<Document>();

        try {

        if(links.size()>0){
        for (int i = 0; i < links.size(); i++) {
            Document URLS = new Document();
            String url = links.get(i);
                URLS.append("URL", url);
                docs.add(URLS);

        }
            Document yaraaabbbb=new Document().append("URLS",docs);
            yaraaabbbb.append("MyId",1);

            collection.findOneAndReplace(new Document("MyId", 1),yaraaabbbb);
        }}catch (IndexOutOfBoundsException | NullPointerException e){

        }
    }


    public LinkedList<String> InterruptedURLs(){

        MongoCollection<Document> collection = database.getCollection("Threads_URLS");
        FindIterable<Document> iterDoc = collection.find();
        MongoCursor<Document> it = iterDoc.iterator();
        List<String> list = new ArrayList<>();
        LinkedList<String> InterruptedLinks= new LinkedList<String>();


        while (it.hasNext()) {
            InterruptedLinks.add((String) ((Document)it.next().get("URLS")).get("URL"));

        }
        it.close();


//
//     for (int i=0;i<list.get(0).size();i++){
//         String url=list.get(0).get(i).toString();
//         InterruptedLinks.add(url);
//     }

        return InterruptedLinks;
    }


    public String RetreiveURL(String thread_name){

        MongoCollection<Document> collection = database.getCollection("Threads_URLS");
        Document iterDoc = collection.find(new BasicDBObject("threadname", thread_name)).sort(new BasicDBObject("_id", -1)).first();
        return iterDoc.get("URL").toString();
    }

    public int RetreiveThreadNum(String thread_name){

        MongoCollection<Document> collection = database.getCollection("Threads_URLS");
        Document iterDoc = collection.find().first();
        return Integer.parseInt(iterDoc.get("threadnum").toString());
    }

    public int DBCount(){
        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        long iterDoc = collection.count();
        return (int)iterDoc ;

    }

    public int DBCount_int(){
        MongoCollection<Document> collection = database.getCollection("Threads_URLS");
        long iterDoc = collection.count();
        return (int)iterDoc ;

    }

    public LinkedList<String> getLinksForRecrawling(){
        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        FindIterable<Document> iterDoc = collection.find();
        MongoCursor<Document> it = iterDoc.iterator();
        LinkedList<String> list = new LinkedList<>();


        while (it.hasNext()) {
            list.add((String) (it.next()).get("URL"));
        }
        it.close();
        return list;
    }

    public void close() {
        mongo.close();
    }
}