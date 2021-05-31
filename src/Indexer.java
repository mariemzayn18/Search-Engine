import java.io.FileReader;
import java.io.IOException;
import java.util.*;
public class Indexer {
    DataBaseMaster dbMaster = new DataBaseMaster();

    // List<org.bson.Document> list = new ArrayList<org.bson.Document>();
    Hashtable<String, List<org.bson.Document>> Indexer = new Hashtable<String, List<org.bson.Document>>();

    Indexer() {
        try {
            //reterive data from database
            List<org.bson.Document> Docs = dbMaster.retriveDocuments();
            //looping over the documents and urls and send them to function
            for (int i = 0 ; i < Docs.size();i++) {
                String url = Docs.get(i).get("URL").toString();
                System.out.println(url);
                String doc = Docs.get(i).get("Document").toString();
                indexing(doc, url);
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

        //split the document to arr of string 
        String[] words = str.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("[^\\w]", "");
        }

        //calling stemmer function and updating the words
        Stemmer Stem = new Stemmer();
        for (int i = 0; i < words.length; i++) {
            Stem.add(words[i].toCharArray(), words[i].toCharArray().length);
            Stem.stem();
            //index is the word after stem
            String index = Stem.toString();
            //update the array
            words[i] = index;
            org.bson.Document indexed = new org.bson.Document("Indexed", index).append("URL", URL).append("TF", 1);
            List<org.bson.Document> doc = Indexer.get(index);
            if (doc == null) {
                doc = new ArrayList<>();
            }
            doc.add(indexed);
            Indexer.put(Stem.toString(), doc);
        }

    }

}
