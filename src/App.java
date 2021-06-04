
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

class App {

    public static int currentCrawledPages = 0;
    public static int maxCrawledPages = 3; // change to 5000

    // ----------------------------------------------------------

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {

        DataBaseMaster dbMaster = new DataBaseMaster();

        Scanner sc = new Scanner(System.in); // System.in is a standard input stream
        System.out.print("Enter the number of threads to take part in the crawling process--> ");
        int numberOfThreads = sc.nextInt();
       // dbMaster.DeleteAllDocs("WebCrawler");
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ------------------------------------------- Mariem.... seeds
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// ---------------------------------------------------

        List<String> seeds = new ArrayList<String>();

        File Seedsfile = new File("E:\\2nd year- 2nd term\\Advanced programming\\ap_proj\\Search-Engine\\src\\Seeds.txt");
        Scanner SeedsSc = new Scanner(Seedsfile);

        while (SeedsSc.hasNextLine()) {
            seeds.add(SeedsSc.nextLine());
        }

        SeedsSc.close();

        // int i=0;
        // while (i<seeds.size()){
        // System.out.println(seeds.get(i));
        // i++;
        // }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Runnable obj1 = new webCrawler(numberOfThreads, currentCrawledPages, maxCrawledPages, seeds);
        crawling(numberOfThreads, obj1);

    }

    public static void crawling(int numberOfThreads, Runnable obj1) {
        for (int i = 0; i < numberOfThreads; i++) {
            Thread Arr = new Thread(obj1);
            Arr.setName(Integer.toString(i));
            System.out.println("Hello there");
            Arr.start();
        }
    }

}

class webCrawler implements Runnable {

    public int Num;
    private HashSet<String> links;

    int currentCrawledPages;
    int maxCrawledPages;
    String myUrl;
    List<String> seeds;

    boolean FirstCrawling = true;
    DataBaseMaster dbMaster = new DataBaseMaster();
    Vector<String> ignored_URLS= new Vector<String>();
    int seeds_size;
    int seeds_count;


    // boolean HasNoPriority=false;

    public webCrawler(int n, int currentCrawledPages, int maxCrawledPages, List<String> seeds) {
        Num = n;
        links = new HashSet<String>();

        this.currentCrawledPages = currentCrawledPages;
        this.maxCrawledPages = maxCrawledPages;
        // this.myUrl=Url;
        this.seeds = seeds;
        this.seeds_size=seeds.size();
        this.seeds_count=0;

        ///////// ---------------------------NOT
        ///////// SURE----------------------------------////////
        // int i=0;
        // while (i<seeds.size()){
        // links.add(seeds.get(i)); // store the links in the hash set at the beginning
        // before the crawling process
        // i++;
        // }

    }

    public void run() {

        // virtual size to know when does the seeds end and stop threading process



        while(seeds_size>0) {
            for (int i = 0; i < Num; i++) {
                if (Integer.parseInt(Thread.currentThread().getName()) == i) {
                    if(seeds_count<seeds.size()) { //actual size , and counter for seeds
                        myUrl = seeds.get(seeds_count);
                        seeds_count++;
                        getPageLinks(this.myUrl);
                    }

                    // System.out.println ("Thread "+ Thread.currentThread().getName() + "
                    // hellllo");
                }
                if (i == Num - 1)
                    FirstCrawling = false;
            }
            seeds_size-=Num;
        }
        // else ///////////////////////////////// loop on the hashset/////////////////////////////////////////
        // {
        // for (int i = 0; i < Num; i++) {
        // if (Integer.parseInt(Thread.currentThread().getName()) == i) {
        // myUrl= seeds.get(i); //////////////////////////////////I think this should be
        // hashset/////////////////////////////////////////////////////
        // getPageLinks(this.myUrl);
        // System.out.println ("Thread "+ Thread.currentThread().getName() + "
        // hellllo");
        // }
        // }
        // }
    }

    public void getPageLinks(String URL)  {

        try {
            // System.out.println(URL);
            Connection con = Jsoup.connect(URL).ignoreContentType(true); //////////// REMOOVVE
            Document document = con.get();

            // for the first URL to be saved
            if (!links.contains(URL)) {
                //check if this url is not in its host's robots file
                Vector<String> no_read_vector = new Vector<String>(1, 1);
                no_read_vector = robot_file(URL);
                for (int i = 0; i < no_read_vector.size(); i++) {
                    // if url contains any extension in the robots go out of the function
                    if (URL.contains(no_read_vector.get(i)))
                        return;
                }
                String title = document.select("title").first().text();
                AddToLinks(URL,title,document);

            }


            if (links.contains(URL) && !FirstCrawling) {
                    ///////////////////// mhtagenn hena n update l document bta3o w nkamel shoghl 3adyy/////////////////////////////
                    dbMaster.UpdateDocument(URL,document.toString());
                }


            Elements linksOnPage = document.select("a[href]");

            for (Element page : linksOnPage) {

                String PageLink = page.attr("abs:href");

                // 1- lesa f awl mara crawling aw msh awl mara 3adyy & the first visit for the
                // page

                if (!PageLink.equals(URL) && !links.contains(PageLink) &&  !ignored_URLS.contains(URL)) {

                    {
//                        if (!links.contains(PageLink)) {
//                            AddToLinks(PageLink, document);
//                        }
                        // System.out.println(links.size());
                        getPageLinks(PageLink);
                    }
                }

                // 2- In the recrawling & not the first visit for the page && the set is full
                // revisit first those with the famous domains -->
                else if (!PageLink.equals(URL) && links.contains(PageLink) && !FirstCrawling && links.size() >= maxCrawledPages &&!ignored_URLS.contains(URL) ) {

                    if (PageLink.contains(".com") || PageLink.contains(".net") || PageLink.contains(".org") || PageLink.contains(".co") || PageLink.contains(".us")) {
                        getPageLinks(PageLink);
                    }
                }
                // 3- In the recrawling & not the first visit for the page
                // revisit first those with which don't contain famous domain --> 3shan akhazen
                // urls gdeda w ela hafdal daymn bazor l urls l famous only fa hadkhol fe
                // infinite loop keda :(((
                // not sure KHALESSSSS BTW
                else if (!PageLink.equals(URL) && links.contains(PageLink) && !FirstCrawling&&!ignored_URLS.contains(URL)) {

                    if ((PageLink.contains(".com") || PageLink.contains(".net") || PageLink.contains(".org") || PageLink.contains(".co") || PageLink.contains(".us")) == false) {
                        getPageLinks(PageLink);
                    }
                }

                // 4- Stop and recrawl
                else if (links.size() >= maxCrawledPages) {
                    System.out.println("I'm here to be recrawled");
                    currentCrawledPages = 0;
                    FirstCrawling = false;
                    Indexer myind=new Indexer(Num);
                    // App.crawling(Num, this);
                }

                // getPageLinks(PageLink);
            }

        } catch (IOException e) {
            System.err.println("For '" + URL + "': " + e.getMessage());
        }
    }

    public void AddToLinks(String URL,String title, Document document) {
        synchronized (this) {
            System.out.println("I have the lock and I'm thread"+Thread.currentThread().getName());
            if (links.size() < maxCrawledPages) {

                String url_dup=dbMaster.found("Document",document.toString(),"WebCrawler");

                if (url_dup!=null){ /// duplicate documents and different URLs, then save one URL only
                    if (!links.contains(url_dup)) // to only make sure that this url already exists inside my list
                    {
                        links.add(url_dup);
                        currentCrawledPages++; /////////////// remove this counter//////////////////////////
                        System.out.println(URL + " my count= " + links.size());
                    }
                    else
                        ignored_URLS.add(URL);

                    System.out.println("Database already contains this document");
                    if(links.size() >= maxCrawledPages) { ////////////////////////////// hash set exceeds the 5000 links////////////////////////////// /////////////////////////////
                        System.out.println("I'm here to be recrawled");
                        currentCrawledPages = 0;
                        FirstCrawling = false;
                        Indexer myind=new Indexer(Num);
                       // App.crawling(Num, this);
                    }
                    return;
                }

                if (links.add(URL)) {

                    dbMaster.insertDocument(document.toString(),title,URL);
                    currentCrawledPages++; /////////////// remove this counter//////////////////////////
                    System.out.println(URL + " my count= " + links.size());

                    System.out.println("I left the lock "+Thread.currentThread().getName());
                }

            } if(links.size() >= maxCrawledPages) { ////////////////////////////// hash set exceeds the 5000 links////////////////////////////// /////////////////////////////
                System.out.println("I'm here to be recrawled");
                currentCrawledPages = 0;
                FirstCrawling = false;
                Indexer myind=new Indexer(Num);
                //App.crawling(Num, this);
            }
            return;
        }
    }

    // ------------------------------------ Robots.txt ---------------------------
    Vector<String> robot_file(String url) {
        int index = 0;
       // System.out.println("start :)");
        /// getting robot url through the host of the passed url
        URL url_temp = null;
        try {
            url_temp = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
       // System.out.println("HOST");
        //System.out.println(url_temp.getHost());
        System.out.println("robot url");
        // prepare the url of the robot: protocol+ host + file
        String robot_url = "https://" + url_temp.getHost() + "/robots.txt";
        System.out.println(robot_url);
        // vector to store the disallow extenions in it
        Vector<String> no_read_vector = new Vector<String>(1, 1);
        // read robots.txt file
        try (BufferedReader my_buffer = new BufferedReader(new InputStreamReader(new URL(robot_url).openStream()))) {
            String line = null;
            int m = 0;
            // FLAG: true if it is User-Agent: * otherwise we won't save the lines under it
            // so reset the flag
            // till we find another "User-Agent" to save disallow lines under it
            boolean user_agent = true;
            while ((line = my_buffer.readLine()) != null) {
                if (m == 0) {
                    System.out.println("i am in the loop");
                    m++;
                }
                index++;
     //           System.out.println("i am line ");
       //         System.out.println(index);
                // ignore any comment in the file
                if (line.startsWith("#") || line == "" || line == " ")
                    break;
                /// make sure that we don't follow any user agent => it must be *
                // each line contain uder agent
                if (line.contains("User-Agent") || line.contains("User-agent:")) {
         //           System.out.println("i am containing user agent");
                    // if all agents * =>> so we will store
                    if (line.equals("User-Agent: *") || line.equals("User-agent: *")) {
                        user_agent = true;
           //             System.out.println("i am user agent *********");
                    }
                    // if other user agents specified yahoo, google... set user_agent=false so:
                    // we don't store any line till you find another user agent line
                    else {
                        user_agent = false;
             //           System.out.println("i am yahoooooooooo");
            //         System.out.println(line);
                    }
                    // skip this iteration anyway because we don't store user-agent line we store
                    // lines after it
                    continue;
                }
                /// storing words that really we can not read + make sure it is your user agent
                /// == true
                if (user_agent && !(line.contains("User-Agent: *")) && !(line.contains("Sitemap:"))
                        && !(line.contains("Allow:"))) {
            //        System.out.println("i am NOT user agent line");
              //      System.out.println(line);
                    if (line.length() >= 9)
                        no_read_vector.add(line.substring(9));

                }
            }
            // System.out.println("i am out of the loop");
            //
            // for (int i = 0; i < no_read_vector.capacity(); i++) {
            // if (i == 0)
            // System.out.println("print vector");
            //
            // System.out.println(no_read_vector.get(i));
            //
            //
            // }
            System.out.println("vector done");
        } catch (IOException e) {
            System.out.println("throwing exception!!!!!!!!");
        }
        return no_read_vector;
    }

}
