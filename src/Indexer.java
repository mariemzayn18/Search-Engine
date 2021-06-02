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

    Indexer() {
        try {
            // reterive data from database
            List<org.bson.Document> Docs = dbMaster.retriveDocuments();

            List<org.bson.Document> DB_Indexers = dbMaster.retriveIndexes();
            // looping over the Words and urls and Save Thme
            System.out.println(DB_Indexers.size());
            for (int i = 0; i < DB_Indexers.size(); i++) {
                String url = DB_Indexers.get(i).get("URL").toString();
                String iString = DB_Indexers.get(i).get("Word").toString();
                int TF = Integer.valueOf(DB_Indexers.get(i).get("priority").toString());
                org.bson.Document Word_Value = new org.bson.Document("Word", iString).append("URL", url).append("priority",
                TF);
                List<org.bson.Document> doc = Indexer.get(iString);
                if (doc == null) {
                    doc = new ArrayList<>();
                }
                doc.add(Word_Value);
                Indexer.put(iString, doc);
            }
            // looping over the documents and urls and send them to function
            DOCs_Num = Docs.size();
            for (int i = 0; i < Docs.size(); i++) {
                String url = Docs.get(i).get("URL").toString();
                System.out.println(url);
                String doc = Docs.get(i).get("Document").toString();
                indexing(doc, url);
            }
            // after the previuos loop we now completed our hash table with only TF
            // now update priority =TF*IDF -->> IDF= no.of docs in DB/ no.of docs contain
            // the word
            Set<String> keys = Indexer.keySet();
            Integer IDF;
            for (String key : keys) {
                IDF = DOCs_Num / Indexer.get(key).size();
                for (int i = 0; i < Indexer.get(key).size(); i++) {

                    IDF = (Integer) Indexer.get(key).get(i).get("priority") * IDF;
                    Indexer.get(key).get(i).append("priority", IDF);
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        dbMaster.insertDocs(Indexer);

    }

    public void indexing(String Doc, String URL) {
        // -----------------------------Indexer--------------------------------

        // TODO 1-Remove HTML Tags(Done)
        // 2-making array of unwanted text(is , are ,,,etc)(Done)
        // 3-make Stemmer(Done)
        // 4-make periority using TF-IDF
        // UNTIL NOWWWWWW !!!!

        // Remove Tags
        String str = Doc;
        str = str.replaceAll("(<style.+?</style>)", "");
        str = str.replaceAll("(?s)<script.*?(/>|</script>)", "");
        str = str.replaceAll("(?s)<head.*?(/>|</head>)", "");
        str = str.replaceAll("(?s)<a.*?(/>|</a>)", "");
        str = str.replaceAll("[0-9]", "");
        str = str.replaceAll("\\<.*?\\>", "");
        str = str.replaceAll("\\W+", " ");
        str = str.toLowerCase();
        // Array of unwanted Text(finished)
        FileReader fr;
        try {
            fr = new FileReader("src/StopWords.txt");
            int i;
            while ((i = fr.read()) != -1)
                str = str.replaceAll(" " + i + " ", " ");
            fr.close();

        } catch (IOException e) {
            System.out.println(e);
        }

        // split the document to arr of string
        String[] words = str.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("[^\\w]", "");
        }

        // calling stemmer function and updating the words
        Stemmer Stem = new Stemmer();
        for (int i = 0; i < words.length; i++) {
            Stem.add(words[i].toCharArray(), words[i].toCharArray().length);
            Stem.stem();
            // index is the word after stem
            String TheWord = Stem.toString();
            // update the array
            words[i] = TheWord;
            System.out.print("hello i am in the indxer i am word " + words[i]);
        }
        // ---------------- start calculating priority -------------------------

        // put each word in words array in a map to count the duplicates in the doc(TF)
        // this is a new map for each document call the function but the hash table
        // global for all the documents
        Map<String, Double> word_TF_Map = new HashMap<String, Double>();
        Vector<String> Unique_words = new Vector<String>();
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
            word_TF_Map.put(myword, word_TF_Map.get(myword) / Doc.length());
            TF = word_TF_Map.get(myword);
            if (TF > 0.5) {
                // It's a SPAM store it in spam vector to delete them before adding to DB
                Spam_URLs.add(URL);
                System.out.println("Delete me ");
                // don't save more words of this url it will be deleted! so return
                return;
            }
            // if not a spam store the word in the hash table with its value ( Key :word ,
            // value: doc contains URL & priority)
            org.bson.Document Word_Value = new org.bson.Document("Word", myword).append("URL", URL).append("priority",
                    TF);
            // check if this doc exists alrady in th DB
            List<org.bson.Document> doc = Indexer.get(myword);
            if (doc == null) {
                // if not create a new list for this word to store its URLs and priorities
                doc = new ArrayList<>();
            } else if (!doc.contains(Word_Value)) {
                // add this url and priority to list of the word
                doc.add(Word_Value);
                // add to the hash table "indexer"
                Indexer.put(myword, doc);

            }

        }

    }
}
