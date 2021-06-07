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
    public static int maxCrawledPages = 5000; // change to 5000
    public static List<String> seeds;
    public static boolean FirstCrawling = true;

    // ----------------------------------------------------------

    App( List<String> seeds ){
        this.seeds = seeds;
    }
    public static void main(String[] args) throws InterruptedException, FileNotFoundException {

        DataBaseMaster dbMaster = new DataBaseMaster();

        Scanner sc = new Scanner(System.in); // System.in is a standard input stream
        System.out.print("Enter the number of threads to take part in the crawling process--> ");
        int numberOfThreads = sc.nextInt();
       //  dbMaster.DeleteAllDocs("WebCrawler");

       List<String> seeds = new ArrayList<String>();


        File Seedsfile = new File("src/Seeds.txt");
        Scanner SeedsSc = new Scanner(Seedsfile);

        while (SeedsSc.hasNextLine()) {
            seeds.add(SeedsSc.nextLine());
        }

        SeedsSc.close();
        new App(seeds);
        HashSet<String> links=new HashSet<String>();

        while (true) {
            start_crawl_process(numberOfThreads,links);
            Indexer myind = new Indexer(numberOfThreads);
           // System.out.println("I'm here tanyyyyy");
        }
    }

  static void start_crawl_process(int numberOfThreads, HashSet<String> links) throws InterruptedException {
     // System.out.println("WELCOMMME");

      Vector<Thread> threads= new Vector<Thread>();
      Vector<Boolean> stopThreads= new Vector<Boolean>();
      for (int i=0;i<numberOfThreads;i++)
          stopThreads.add(false);


      Runnable obj1 = new webCrawler(numberOfThreads, currentCrawledPages, maxCrawledPages, seeds,threads,stopThreads,links, FirstCrawling);
      crawling(numberOfThreads, obj1,threads,stopThreads);
      //System.out.println("FROM CRAWLER --> Finally i have finished working with threads");


  }
    public static void crawling(int numberOfThreads, Runnable obj1,Vector<Thread> threads, Vector<Boolean> stopThreads) throws InterruptedException {


        for (int i = 0; i < numberOfThreads; i++) {
            if (!stopThreads.get(i))
                {
                    Thread mycrawler = new Thread(obj1);
                    mycrawler.setName(Integer.toString(i));
                    threads.add(mycrawler);
                  //  System.out.println("Hello there I'm in crawler and I'm thread #"+i);
                    mycrawler.start();
            }
        }

        for(int i=0;i<numberOfThreads;i++) {
            threads.get(i).join();
            if(i==(numberOfThreads-1))
                FirstCrawling=false;

        }
    }

}

class webCrawler implements Runnable {

    public int Num;
     HashSet<String> links;

    int currentCrawledPages;
    int maxCrawledPages;
    String myUrl;
    List<String> seeds;
    Vector<Thread> threads;

    boolean FirstCrawling;
    DataBaseMaster dbMaster = new DataBaseMaster();
    Vector<String> ignored_URLS= new Vector<String>();
    int seeds_size;
    int seeds_count;
    Vector<Boolean> stopThreads;
    boolean stopme=false;
// to store each host (key) with vector of robot.txt disallowed words (value)
    Map<String, Vector<String>> Robot_Map = new HashMap<String, Vector<String>>();

    public webCrawler(int n, int currentCrawledPages, int maxCrawledPages, List<String> seeds,Vector<Thread> threads, Vector<Boolean> stopThreads, HashSet<String> links,boolean FirstCrawling) {
        Num = n;
        this.links = links;

        this.currentCrawledPages = currentCrawledPages;
        this.maxCrawledPages = maxCrawledPages;
        this.seeds = seeds;
        this.seeds_size=seeds.size();
        this.seeds_count=0;
        this.threads=threads;
        this.stopThreads=stopThreads;
        this.FirstCrawling=FirstCrawling;



    }

    public void run() {

        // virtual size to know when does the seeds end and stop threading process
        while(seeds_size>0) {
                for (int i = 0; i < Num; i++) {
                    if (!stopThreads.get(i)) {
                        if(seeds_count<seeds.size()) { //actual size , and counter for seeds
                        myUrl = seeds.get(seeds_count);
                        seeds_count++;
                        getPageLinks(this.myUrl);
                    }
            }
                }
            seeds_size-=Num;
        }

    }

    public void getPageLinks(String URL)  {
        if (!stopThreads.get(Integer.valueOf(Thread.currentThread().getName()))){
        try {
            Connection con = Jsoup.connect(URL).ignoreContentType(true); //////////// REMOOVVE
            Document document = con.get();

            // for the first URL to be saved
            if (!links.contains(URL)) 
            {
               
                //--------------------------start robot part ------------------      
                URL url_temp = null;
                try
                 {
                    url_temp = new URL(URL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                if ( Robot_Map.get(url_temp.getHost()) == null)
                {
                      robot_file(URL);
                    //  System.out.println("##******ONLY ONE TIME");
                }
                String normalized_url1=""; // ened with /
                String normalized_url2="";// ended with ?
                if ( ! URL.endsWith("/"))
                normalized_url1= URL+"/";
             else if ( ! URL.endsWith("?"))
                normalized_url2= URL+"?";
                else 
                {
                    normalized_url1=URL;
                    normalized_url2=URL;
                }
               
                for (int i = 0; i < Robot_Map.get(url_temp.getHost()).size(); i++)
                 {
                    if ( Robot_Map.get(url_temp.getHost()).get(i).endsWith("*"))
                    {
                        String temp_robot = Robot_Map.get(url_temp.getHost()).get(i);    
                        StringBuffer sb= new StringBuffer(temp_robot);   
                        sb.deleteCharAt(sb.length()-1);  
                        if (URL.contains( sb) || normalized_url1.contains( sb) ||normalized_url2.contains( sb))
                        {
                          //  System.out.println("#11#############################################################################################################################################Hello there i am not allowed ########################################################################");
                            ignored_URLS.add(URL);
                            return;
                        } 
                                                
                    }
                    else
                    {
                        String temp_robot2=Robot_Map.get(url_temp.getHost()).get(i);  
                         
                    if (URL.contains(temp_robot2 ) || normalized_url1.contains(temp_robot2) 
                    || normalized_url2.contains(temp_robot2+"?") ||
                    URL.contains(temp_robot2+"/") || URL.contains(temp_robot2+"?") )
                    {
                       // System.out.println("#222############################################################################################Hello there i am not allowed #######################################################################################################3");
                        ignored_URLS.add(URL);
                        return;
                    }

                    } 
                }
          //-------------------------- end robot part ------------------     
                
                AddToLinks(URL,document);
        
            }


           else if (links.contains(URL) && !FirstCrawling) {
                dbMaster.UpdateDocument(URL,document.toString(),Thread.currentThread().getName(),Num);
            }

           // to make sure that I haven't been stopped from the AddToLinks function
            if (!stopThreads.get(Integer.valueOf(Thread.currentThread().getName()))){

            Elements linksOnPage = document.select("a[href]");

            for (Element page : linksOnPage) {

                String PageLink = page.attr("abs:href");

                // 1- lesa f awl mara crawling aw msh awl mara 3adyy & the first visit for the
                // page

                if (!PageLink.equals(URL) && !links.contains(PageLink) &&  !ignored_URLS.contains(URL)) {

                        getPageLinks(PageLink);
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
                    currentCrawledPages = 0;
                    for(int i=0;i<Num;i++)
                        stopThreads.set(i, true);
                    return;
                }

            }
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("For '" + URL + "': " + e.getMessage());
        }}
        else
            return;

    }

    public void AddToLinks(String URL, Document document) throws InterruptedException {
        synchronized (this) {
         //   System.out.println("I have the lock and I'm thread"+Thread.currentThread().getName());
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

                  //  System.out.println("Database already contains this document");
                    if(links.size() >= maxCrawledPages) { ////////////////////////////// hash set exceeds the 5000 links////////////////////////////// /////////////////////////////
                        currentCrawledPages = 0;
                        for(int i=0;i<Num;i++)
                            stopThreads.set(i, true);
                        return;

                    }
                    return;
                }

                if (links.add(URL)) {

                    dbMaster.insertDocument(document.toString(),URL,Thread.currentThread().getName(),Num );
                    currentCrawledPages++; /////////////// remove this counter//////////////////////////
                    System.out.println(URL + " my count= " + links.size());

                 //   System.out.println("I left the lock "+Thread.currentThread().getName());
                }

            } if(links.size() >= maxCrawledPages) { ////////////////////////////// hash set exceeds the 5000 links////////////////////////////// /////////////////////////////
                currentCrawledPages = 0;
                for(int i=0;i<Num;i++)
                    stopThreads.set(i, true);
                    return;
            }
            return;
        }
    }


      // ------------------------------------ Robots.txt ---------------------------
   void robot_file(String url) {
    /// getting robot url through the host of the passed url
    URL url_temp = null;
    try {
        url_temp = new URL(url);
    } catch (MalformedURLException e) {
        e.printStackTrace();
    }
    System.out.println("robot url");
    // prepare the url of the robot: protocol+ host + file
    String robot_url = url_temp.getProtocol()+"://" + url_temp.getHost() + "/robots.txt";
    System.out.println(robot_url);
    Vector<String> no_read_vector = new Vector<String>();
    Robot_Map.put(url_temp.getHost(), no_read_vector);
    // vector to store the disallow extenions in it

    // read robots.txt file
    try (BufferedReader my_buffer = new BufferedReader(new InputStreamReader(new URL(robot_url).openStream()))) {
        String line = null;
        // FLAG: true if it is User-Agent: * otherwise we won't save the lines under it
        // so reset the flag
        // till we find another "User-Agent" to save disallow lines under it
        boolean user_agent = true;
        while ((line = my_buffer.readLine()) != null) {
            /// make sure that we don't follow any user agent => it must be *
            // each line contain uder agent
            if (line.contains("User-Agent") || line.contains("User-agent:")) {
                // if all agents * =>> so we will store
                if (line.equals("User-Agent: *") || line.equals("User-agent: *")) {
                    user_agent = true;
                 //                System.out.println("i am user agent *********");
                }
                // if other user agents specified yahoo, google... set user_agent=false so:
                // we don't store any line till you find another user agent line
                else {
                    user_agent = false;
                }
                // skip this iteration anyway because we don't store user-agent line we store
                // lines after it
                continue;
            }
            /// storing words that really we can not read + make sure it is your user agent
            /// == true  User-agent: *
            if (user_agent && !(line.contains("User-Agent: *")) &&!(line.contains("User-agent: *")) && !(line.contains("Sitemap:"))
                    && !(line.contains("Allow:"))) {
                if (line.length() > 10)
                   Robot_Map.get(url_temp.getHost()).add(line.substring(10));

            }
        }
    
      //  System.out.println("vector done");
//        for ( int i=0; i<  Robot_Map.get(url_temp.getHost()).size(); i ++)
//        {
//            System.out.println( Robot_Map.get(url_temp.getHost()).get(i));
//        }

    } catch (IOException e) {
        System.out.println("throwing exception!!!!!!!!");
    }
    
}

}
/////////////