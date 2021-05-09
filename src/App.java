import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;

import javax.lang.model.util.Elements;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.MongoClient;

public class App {
    public static void main(String[] args) throws InterruptedException {

        // Creating a Mongo client
        MongoClient mongo = new MongoClient("localhost", 27017);

        // Accessing the database
        MongoDatabase database = mongo.getDatabase("myDatabase");

        // Creating a collection
        database.createCollection("Search-Engine");
        System.out.println("Collection created successfully");

        // Retrieving a collection
        MongoCollection<Document> collection = database.getCollection("Search-Engine");
        System.out.println("Collection Search-Engine selected successfully");
        String text = "";
        String URL = "";
        Document document = new Document("text", text).append("URL", URL);

        // Inserting document into the collection
        collection.insertOne(document);
        System.out.println("Document inserted successfully");

        Scanner sc = new Scanner(System.in); // System.in is a standard input stream
        System.out.print("Enter first number- ");
        int a = sc.nextInt();

        Thread Arr[] = new Thread[a];
        for (int i = 0; i < a; i++) {
            Arr[i].setName(Integer.toString(i));
            Arr[i].start();
        }

        // new WebCrawler().getPageLinks("http://www.mkyong.com/",a);

    }
}

class webCrawler implements Runnable {

    public int Num;
    // hashiing
    private HashSet<String> links;

    public webCrawler(int n) {
        Num = n;
        links = new HashSet<String>();
    }

    public void run() {
        for (int i = 0; i < Num; i++) {
            if (Integer.parseInt(Thread.currentThread().getName()) == i) {

                getPageLinks("http://www.mkyong.com/");
            }

        }

    }

    public void getPageLinks(String URL) {
        // 4. Check if you have already crawled the URLs
        // (we are intentionally not checking for duplicate content in this example)
        if (!links.contains(URL)) {
            try {
                // 4. (i) If not add it to the index
                if (links.add(URL)) {
                    System.out.println(URL);
                }

                // 2. Fetch the HTML code
                org.jsoup.nodes.Document document = Jsoup.connect(URL).get();
                // 3. Parse the HTML to extract links to other URLs
                org.jsoup.select.Elements linksOnPage = document.select("a[href]");

                // 5. For each extracted URL... go back to Step 4.
                for (Element page : linksOnPage) {
                    getPageLinks(page.attr("abs:href"));
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }

        }

    }
}
