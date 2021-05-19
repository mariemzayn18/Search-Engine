import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import java.util.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClient;

class App {
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
        int TF = 0;
        Document document = new Document("text", text).append("URL", URL).append("TF", TF);

        // Inserting document into the collection
        collection.insertOne(document);
        System.out.println("Document inserted successfully");

        Scanner sc = new Scanner(System.in); // System.in is a standard input stream
        System.out.print("Enter Threads Number ");
        int a = sc.nextInt();

        Thread Arr[] = new Thread[a];
        for (int i = 0; i < a; i++) {
            Arr[i].setName(Integer.toString(i));
            Arr[i].start();
        }

        // new WebCrawler().getPageLinks("http://www.mkyong.com/",a);

       
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

                    try {
                        getPageLinks("http://www.mkyong.com/");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName() + "hellllo");
                }

            }

        }

        public void getPageLinks(String URL) throws MalformedURLException {
         
             // 4. Check if you have already crawled the URLs
            // (we are intentionally not checking for duplicate content in this example)
            if (!links.contains(URL)) {
            // check if this url is not in its host's robots file
            Vector<String> no_read_vector = new Vector<String>(1, 1);
                no_read_vector = robot_file( URL);
            for (int i = 0; i < no_read_vector.capacity(); i++)
            {
                // if url contains any extension in the robots go out of the function
                if( URL.contains( no_read_vector.get(i)))
                         return;
            }
            System.out.println("fine you can fetc :)" );

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
                        Vector<String> no_read_2 = new Vector<String>(1, 1);
                        no_read_2 = robot_file(page.attr("abs:href"));
                        for (int i = 0; i < no_read_2.capacity(); i++) {
                            if (page.attr("abs:href").contains(no_read_2.get(i)))
                                break;
                        }
                        // recursion: fetch each hyper link in the url 
                        getPageLinks(page.attr("abs:href"));
                    }
                } catch (IOException e) {
                    System.err.println("For '" + URL + "': " + e.getMessage());
                }

            }
        }
//------------------------------------ Robots.txt ------------------------------
Vector<String> robot_file ( String url )  {
    System.out.println("start :)" );
    /// getting robot url through the host of the passed url
    URL url_temp= null;
    try {
        url_temp = new URL(url );
    } catch (MalformedURLException e) {
        e.printStackTrace();
    }
    System.out.println("HOST" );
    System.out.println(url_temp.getHost()  );
    System.out.println("robot url" );
    // prepare the url of the robot: protocol+ host + file
    String robot_url ="https://"+ url_temp.getHost()+"/robots.txt"  ;
    System.out.println(  robot_url);
    // vector to store the disallow extenions in it
     Vector<String> no_read_vector = new Vector<String>(1, 1);
     // read robots.txt file
    try(BufferedReader my_buffer = new BufferedReader(
            new InputStreamReader(new URL( robot_url ). openStream()))) {
        String line = null;
        int m = 0;
        // FLAG: true if it is User-Agent: * otherwise we won't save the lines under it so reset the flag
        // till we find another "User-Agent" to save disallow lines under it
        boolean user_agent = true;
       while ((line = my_buffer.readLine()) != null) {
            if (m == 0) {
                System.out.println("i am in the loop");
                m++;
            }
            /// make sure that we don't follow any user agent => it must be *
            // each line contain uder agent 
            if (line.contains("User-Agent") || line.contains("User-agent:")) {
                System.out.println("i am containing user agent");
                // if all agents * =>>  so we will store
                if (line.equals("User-Agent: *") || line.equals("User-agent: *")) {
                    user_agent = true;
                    System.out.println("i am user agent *********");
                }
             // if other user agents specified yahoo, google... set user_agent=false so:
            // we don't store any line till you find another user agent line   
                else {
                    user_agent = false;
                    System.out.println("i am yahoooooooooo");
                    System.out.println(line);
                }
    //  skip this iteration anyway because we don't store user-agent line we store lines after it
                continue;
            }
            ///storing words that really we can not read + make sure it is your user agent == true
            if (user_agent && !(line.contains("User-Agent: *")) && !(line.contains("Sitemap:")) && !(line.contains("Allow:"))) {
                System.out.println("i am NOT user agent line");
                // System.out.println( line);
                no_read_vector.add(line.substring(9));

            }
        }
//           System.out.println("i am out of the loop");
//
//           for (int i = 0; i < no_read_vector.capacity(); i++) {
//               if (i == 0)
//                   System.out.println("print vector");
//
//               System.out.println(no_read_vector.get(i));
//
//
//           }
        System.out.println("vector done");
    }
    catch (IOException e) {
       System.out.println("throwing exception!!!!!!!!" );
         e.printStackTrace();
     }
     return no_read_vector;
 }

        
        
        public void indexer(org.jsoup.nodes.Document Doc) {
         
 // -----------------------------Indexer--------------------------------

        // TODO 1-Remove HTML Tags(Done)
        // 2-making array of unwanted text(is , are ,,,etc)
        // 3-make Stemmer(Done)
        // 4-make periority using TF-IDF
        // UNTIL NOWWWWWW !!!!

        // Remove Tags
        String str = Doc.toString();
        str = str.replaceAll("(<style.+?</style>)", "");
        str = str.replaceAll("(?s)<script.*?(/>|</script>)", "");
        str = str.replaceAll("(?s)<head.*?(/>|</head>)", "");
        str = str.replaceAll("(?s)<a.*?(/>|</a>)", "");
        //str = str.replaceAll("(?s)<li.*?(/>|</li>)", "");
        str = str.replaceAll("[0-9]", "");
        str = str.replaceAll("\\<.*?\\>", "");
        str = str.replaceAll("\\W+"," ");
        str = str.toLowerCase();
// Array of unwanted Text(unfinished)
        Set<String> stopWords = new HashSet<String>();
        stopWords.add("a");
        stopWords.add("an");
        stopWords.add("i");
        stopWords.add("is");
        stopWords.add("are");
        stopWords.add("he");
        stopWords.add("she");
        stopWords.add("it");
        stopWords.add("the");
        stopWords.add(";");
        stopWords.add(".");
        stopWords.add("+");


        for (String value : stopWords) {
            str = str.replaceAll(" "+value+" ", " ");

        }

        Stemmer Stem = new Stemmer();

        Stem.add(str.toCharArray(), str.toCharArray().length);

        // if (Character.isLetter((char) ch))
        // {
        // int j = 0;
        // while(true)
        // { ch = Character.toLowerCase((char) ch);
        // w[j] = (char) ch;
        // if (j < 500) j++;
        // ch = in.read();
        // if (!Character.isLetter((char) ch))
        // {
        // /* to test add(char ch) */
        // for (int c = 0; c < j; c++) s.add(w[c]);

        // /* or, to test add(char[] w, int j) */
        // /* s.add(w, j); */

        // s.stem();
        // { String u;

        // /* and now, to test toString() : */
        // u = s.toString();

        // /* to test getResultBuffer(), getResultLength() : */
        // /* u = new String(s.getResultBuffer(), 0, s.getResultLength()); */

        // System.out.print(u);
        // }
        // break;
        // }
        // }
        // }

        // }


        }

        
       
}
