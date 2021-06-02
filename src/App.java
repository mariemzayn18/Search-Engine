
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

        Scanner sc = new Scanner(System.in); // System.in is a standard input stream
        System.out.print("Enter the number of threads to take part in the crawling process--> ");
        int numberOfThreads = sc.nextInt();

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

    // boolean HasNoPriority=false;

    public webCrawler(int n, int currentCrawledPages, int maxCrawledPages, List<String> seeds) {
        Num = n;
        links = new HashSet<String>();

        this.currentCrawledPages = currentCrawledPages;
        this.maxCrawledPages = maxCrawledPages;
        // this.myUrl=Url;
        this.seeds = seeds;

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
        // if (FirstCrawling) //uncomment this
        {
            for (int i = 0; i < Num; i++) {
                if (Integer.parseInt(Thread.currentThread().getName()) == i) {
                    /////////////////////////////// feh haga hena msh sa777 mafrod lama yrga3
                    /////////////////////////////// myrg3sh mn l awal
                    /////////////////////////////// tanyy///////////////////////////
                    myUrl = seeds.get(i);
                    getPageLinks(this.myUrl);
                    // System.out.println ("Thread "+ Thread.currentThread().getName() + "
                    // hellllo");
                }
                if (i == Num - 1)
                    FirstCrawling = false;
            }
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


                // LinksDocuments.add(); /////////////add my document:(((((( --> donnee
                // w check lw hwa msh duplicate link firstttt --> done

//                if (dbMaster.found("Document",document.toString(),"WebCrawler")) /// duplicate documents and different URLs, then save one URL only
//                {
//                    return;
//                }
//                if (links.add(URL)) {
//                    currentCrawledPages++;
//                    //dbMaster.insertDocument(document.toString(),URL);
//                    System.out.println(URL + " my count= " + links.size());
//                }
                AddToLinks(URL,document);

            }
            //// try catch


            if (links.contains(URL) && !FirstCrawling) {
                if (links.size() >= 200) {
                    ///////////////////// mhtagenn hena n update l document bta3o/////////////////////////////

                    return;
                }

                else {
                    ///////////////////// mhtagenn hena n update l document bta3o w nkamel shoghl 3adyy/////////////////////////////

                }
            }

            Elements linksOnPage = document.select("a[href]");

            for (Element page : linksOnPage) {

                String PageLink = page.attr("abs:href");

                // 1- lesa f awl mara crawling aw msh awl mara 3adyy & the first visit for the
                // page

                if (!PageLink.equals(URL) && !links.contains(PageLink)) {
                    //////////////////////////////// to be checked/////////////////////////////////////////
                    // if (PageLink.contains(".com")||PageLink.contains(".net")||
                    //////////////////////////////// PageLink.contains(".org")||
                    //////////////////////////////// PageLink.contains(".co") ||
                    //////////////////////////////// PageLink.contains(".us"))
                    {
                        if (!links.contains(PageLink)) {
                            AddToLinks(PageLink, document);
                        }
                        // System.out.println(links.size());
                        getPageLinks(PageLink);
                    }
                }

                // 2- In the recrawling & not the first visit for the page && the set is full
                // ///////////////////////////////// added condition for recrawling// //////////////////////////////////////
                // revisit first those with the famous domains -->
                else if (!PageLink.equals(URL) && links.contains(PageLink) && !FirstCrawling && links.size() >= 200) {

                    if (PageLink.contains(".com") || PageLink.contains(".net") || PageLink.contains(".org") || PageLink.contains(".co") || PageLink.contains(".us")) {
                        getPageLinks(PageLink);
                    }
                }
                // 3- In the recrawling & not the first visit for the page
                // revisit first those with which don't contain famous domain --> 3shan akhazen
                // urls gdeda w ela hafdal daymn bazor l urls l famous only fa hadkhol fe
                // infinite loop keda :(((
                // not sure KHALESSSSS BTW
                else if (!PageLink.equals(URL) && links.contains(PageLink) && !FirstCrawling) {

                    if ((PageLink.contains(".com") || PageLink.contains(".net") || PageLink.contains(".org") || PageLink.contains(".co") || PageLink.contains(".us")) == false) {
                        getPageLinks(PageLink);
                    }
                }

                // 4- Stop and recrawl
                else if (links.size() >= 200) {
                    System.out.println("I'm here to be recrawled");
                    currentCrawledPages = 0;
                    FirstCrawling = false;
                    // Indexer MYindexer = new Indexer();
                    App.crawling(Num, this);


                }

                // getPageLinks(PageLink);
            }

        } catch (IOException e) {
            System.err.println("For '" + URL + "': " + e.getMessage());
        }
    }

    public void AddToLinks(String URL, Document document) {
        synchronized (this) {
            System.out.println("I have the lock and I'm "+Thread.currentThread().getName());
            if (links.size() < 200) {
//            if (dbMaster.found("Document",document.toString(),"WebCrawler")) /// duplicate documents and different URLs, then save one URL only
//            {
//                return;
//            }

                if (links.add(URL)) {
                    // LinksDocuments.add(document);
                    //   dbMaster.insertDocument(document.toString(),URL);
                    currentCrawledPages++; /////////////// remove this counter//////////////////////////
                    System.out.println(URL + " my count= " + links.size());

                    if (currentCrawledPages >= 200) {
                        System.out.println("I'm here to be recrawled");
                        currentCrawledPages = 0;
                        FirstCrawling = false;
                        App.crawling(Num, this);
                    } else{
                        System.out.println("I left the lock "+Thread.currentThread().getName());
                        return;

                    }

                }

            } else if (links.size() >= 200) { ////////////////////////////// hash set exceeds the 5000 links////////////////////////////// /////////////////////////////
                System.out.println("I'm here to be recrawled");
                currentCrawledPages = 0;
                FirstCrawling = false;
                // Thread.currentThread().stop();
                App.crawling(Num, this);
            }
        }
    }

    // ------------------------------------ Robots.txt ---------------------------
    Vector<String> robot_file(String url) {
        int index = 0;
        System.out.println("start :)");
        /// getting robot url through the host of the passed url
        URL url_temp = null;
        try {
            url_temp = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        System.out.println("HOST");
        System.out.println(url_temp.getHost());
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
                System.out.println("i am line ");
                System.out.println(index);
                // ignore any comment in the file
                if (line.startsWith("#") || line == "" || line == " ")
                    break;
                /// make sure that we don't follow any user agent => it must be *
                // each line contain uder agent
                if (line.contains("User-Agent") || line.contains("User-agent:")) {
                    System.out.println("i am containing user agent");
                    // if all agents * =>> so we will store
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
                    // skip this iteration anyway because we don't store user-agent line we store
                    // lines after it
                    continue;
                }
                /// storing words that really we can not read + make sure it is your user agent
                /// == true
                if (user_agent && !(line.contains("User-Agent: *")) && !(line.contains("Sitemap:"))
                        && !(line.contains("Allow:"))) {
                    System.out.println("i am NOT user agent line");
                    System.out.println(line);
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
            e.printStackTrace();
        }
        return no_read_vector;
    }

}
