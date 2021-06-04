import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

//import DataBaseMaster;
public class Indexer {
    DataBaseMaster dbMaster = new DataBaseMaster();
    int DOCs_Num; // NO.OF DOCS IN DATABASE
    Vector<String> Spam_URLs = new Vector<String>();
    // List<org.bson.Document> list = new ArrayList<org.bson.Document>();
    Hashtable<String, List<org.bson.Document>> Indexer = new Hashtable<String, List<org.bson.Document>>();
    int numberOfThreads;

    Indexer(int numberOfThreads) {

       // dbMaster.DeleteAllDocs("Indexer");
        this.numberOfThreads = numberOfThreads;

        try {
            // reterive data from database
            List<org.bson.Document> Docs = dbMaster.retriveDocuments();

            List<org.bson.Document> DB_Indexers = dbMaster.retriveIndexes();
            // looping over the Words and urls and Save Them
            for (int i = 0; i < DB_Indexers.size(); i++) {
                String url = DB_Indexers.get(i).get("URL").toString();
                String title = DB_Indexers.get(i).get("title").toString();
                String iString = DB_Indexers.get(i).get("Word").toString();
                double TF = Double.valueOf(DB_Indexers.get(i).get("priority").toString());
                org.bson.Document Word_Value = new org.bson.Document("Word", iString).append("URL", url)
                        .append("title", title).append("priority", TF);
                List<org.bson.Document> doc = Indexer.get(iString);
                if (doc == null) {
                    doc = new ArrayList<>();
                }
                doc.add(Word_Value);
                Indexer.put(iString, doc);
            }

            // looping over the documents and urls and send them to function
            DOCs_Num = Docs.size();

            Runnable obj1 = new WebIndexer(DOCs_Num, Docs, numberOfThreads, Spam_URLs, Indexer);
            index_threads(numberOfThreads, obj1);

            System.out.println("Finally i have finished working with threads");

            // after the previuos loop we now completed our hash table with only TF
            // now update priority =TF*IDF -->> IDF= no.of docs in DB/ no.of docs contain
            // the word
            System.out.println("------------------------final priority");
            Set<String> keys = Indexer.keySet();
            Double IDF;
            for (String key : keys) {
                IDF = (double) (DOCs_Num / Indexer.get(key).size());
                for (int i = 0; i < Indexer.get(key).size(); i++) {

                    IDF = (Double) Indexer.get(key).get(i).get("priority") * IDF;
                    Indexer.get(key).get(i).append("priority", IDF);
                }
            }
            dbMaster.insertDocs(Indexer, Spam_URLs);
    
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public static void index_threads(int numberOfThreads, Runnable obj1) throws InterruptedException {
        Vector<Thread> threads = new Vector<Thread>();

        for (int i = 0; i < numberOfThreads; i++) {
            Thread myindexer = new Thread(obj1);
            myindexer.setName(Integer.toString(i));
            threads.add(myindexer);
            System.out.println("Hello there from indexer and thread #" + i);
            myindexer.start();
        }

        for (int i = 0; i < numberOfThreads; i++) {
            threads.get(i).join();
        }
    }
}

class WebIndexer implements Runnable {
    int DocumentNumber;
    List<org.bson.Document> Docs;
    int Document_size;
    int numberOfThreads;
    int Doc_count;
    DataBaseMaster dbMaster = new DataBaseMaster();
    Vector<String> Spam_URLs;
    Hashtable<String, List<org.bson.Document>> Indexer;

    WebIndexer(int DocumentNumber, List<org.bson.Document> Docs, int numberOfThreads, Vector<String> Spam_URLs,
            Hashtable<String, List<org.bson.Document>> Indexer) {
        this.DocumentNumber = DocumentNumber;
        this.Docs = Docs;
        Document_size = Docs.size();
        this.numberOfThreads = numberOfThreads;
        Doc_count = 0;
        this.Spam_URLs = Spam_URLs;
        this.Indexer = Indexer;
    }

    public void run() {

        while (Document_size > 0) {
            for (int i = 0; i < numberOfThreads; i++) {
                if (Doc_count < Docs.size()) {
                    String url = Docs.get(Doc_count).get("URL").toString();
                    System.out.println("Indexer and my URL is: " + url);
                    String doc = Docs.get(Doc_count).get("Document").toString();
                 String title;
                    try {
                    title = Docs.get(Doc_count).get("title").toString();
                      
                  } catch (Exception e) {
                     title = url;
                  }
                    Doc_count++;
                    indexing_process(doc, url, title);
                }

            }

            Document_size -= numberOfThreads;
        }
        System.out.println("i finshed all docs and I'm thread #" + Thread.currentThread().getName());

    }

    public void indexing_process(String Doc, String URL, String title) {
        // -----------------------------Indexer--------------------------------

        // TODO 1-Remove HTML Tags(Done)
        // 2-making array of unwanted text(is , are ,,,etc)(Done)
        // 3-make Stemmer(Done)
        // 4-make periority using TF-IDF
        // UNTIL NOWWWWWW !!!!

        // Remove Tags
        String str = Doc;
        str = str.replaceAll("<style([\\s\\S]+?)</style>", "");
        str = str.replaceAll("<script([\\s\\S]+?)</script>", "");
        str = str.replaceAll("\n", "");
        str = str.replaceAll("\\<[\s]*tag[^>]*>","");
        str = str.replaceAll("\\<.*?>", "");
        str = str.replaceAll("[0-9]", "");
        str = str.replaceAll("\\p{P}", "");
        str = str.toLowerCase();
        // Array of unwanted Text(finished)
        try {
            File StopWords = new File("src/StopWords.txt");
            Scanner Words = new Scanner(StopWords);

            while (Words.hasNextLine()) {
                str = str.replaceAll(Words.nextLine()+"\\s+", " ");
            }

            Words.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        // split the document to arr of string
        String[] words = str.split("\\s+");

        // calling stemmer function and updating the words
        Stemmer Stem = new Stemmer();
        for (int i = 0; i < words.length; i++) {
            Stem.add(words[i].toCharArray(), words[i].toCharArray().length);
            Stem.stem();
            // index is the word after stem
            String TheWord = Stem.toString();
            // update the array
            words[i] = TheWord;
            // System.out.print("hello i am in the indxer i am word " + words[i]);
        }
        // ---------------- start calculating priority -------------------------

        // put each word in words array in a map to count the duplicates in the doc(TF)
        // this is a new map for each document call the function but the hash table
        // global for all the documents
        Vector<String> Unique_words = new Vector<String>();
        Map<String, Double> word_TF_Map = new HashMap<String, Double>();
        for (String unique_word : words) {

            if (word_TF_Map.containsKey(unique_word)) {
                word_TF_Map.put(unique_word, word_TF_Map.get(unique_word) + 1.0);
            } else {
                word_TF_Map.put(unique_word, 1.0);
                Unique_words.add(unique_word);
            }
        }
        // now replace the count(TF) in the map with the normalized TF
        // normalized TF = no. of occurances of the word in the doc/no. of words in this
        // doc
        Double TF;
        for (String myword : Unique_words) {
            // System.out.println(myword);
            // System.out.println(word_TF_Map.get(myword));
            // System.out.println(Doc.length());
            // System.out.println(word_TF_Map.size());
            // System.out.println(Unique_words.size());
            word_TF_Map.put(myword, word_TF_Map.get(myword) / Doc.length());
            TF = word_TF_Map.get(myword);
            if (TF > 0.5) {
                // It's a SPAM store it in spam vector to delete them before adding to DB
                Spam_URLs.add(URL);
                dbMaster.deleteDocument(URL);
                System.out.println("Delete me ");
                // don't save more words of this url it will be deleted! so return
                return;
            }
            // if not a spam store the word in the hash table with its value ( Key :word ,
            // value: doc contains URL & priority)
            org.bson.Document Word_Value = new org.bson.Document("Word", myword).append("URL", URL)
                    .append("title", title).append("priority", TF);
            // check if this doc exists alrady in th DB
            List<org.bson.Document> doc = Indexer.get(myword);
            // IF this word is not stored before
            if (doc == null) {
                //System.out.println("----------------------NULL DOC----------");
                // if not create a new list for this word to store its URLs and priorities
                doc = new ArrayList<>();
                // add the first doc in its list
                doc.add(Word_Value);
                Indexer.put(myword, doc);
            } else {
                doc.add(Word_Value);
                Indexer.put(myword, doc);

            }
            // System.out.println("finished indexing and I'm thread
            // #"+Thread.currentThread().getName());

        }

    }

}
