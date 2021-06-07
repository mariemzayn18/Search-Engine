import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.util.*;


class App {

    public static int currentCrawledPages = 0;
    public static int maxCrawledPages = 5000; // change to 5000
    public static List<String> seeds;
    public static boolean FirstCrawling = true;
    private static DataBaseMaster dbMaster=new DataBaseMaster();

    // ----------------------------------------------------------

    public static void main(String[] args) throws InterruptedException, FileNotFoundException,ConcurrentModificationException{


        Scanner sc = new Scanner(System.in); // System.in is a standard input stream
        System.out.print("Enter the number of threads to take part in the crawling process--> ");
        int numberOfThreads = sc.nextInt();
       // dbMaster.DeleteAllDocs("WebCrawler");
        //   dbMaster.DeleteAllDocs("Indexers");


        File Seedsfile = new File("E:\\2nd year- 2nd term\\Advanced programming\\ap_proj\\Search-Engine\\src\\Seeds.txt");
        Scanner SeedsSc = new Scanner(Seedsfile);


        LinkedList<String> links=new LinkedList<>();

        int count=dbMaster.DBCount();
        if(count<maxCrawledPages && count>0){ //7sali interruption
            System.out.println("Interrupted");
            links=dbMaster.InterruptedURLs();
        }
        else { // crawl 3adii
            while (SeedsSc.hasNextLine()) {
                System.out.println("Recrawl");
                links.add(SeedsSc.nextLine());
            }

        }

        System.out.println(links.size());
        SeedsSc.close();

        while (true) {
            start_crawl_process(numberOfThreads,links);
            Indexer myind = new Indexer(numberOfThreads);
        }
    }


    static void start_crawl_process(int numberOfThreads, LinkedList<String> links) throws InterruptedException {
        // System.out.println("WELCOMMME");

        Vector<Thread> threads= new Vector<Thread>();
        Vector<Boolean> stopThreads= new Vector<Boolean>();
        for (int i=0;i<numberOfThreads;i++)
            stopThreads.add(false);

        if(!FirstCrawling){ //for recrawling, save in the linkedlist the URLS from the first crawling
            links=dbMaster.getLinksForRecrawling();
        }


        lock mylock=new lock();
        for (int i = 0; i < numberOfThreads; i++) {
            if (!stopThreads.get(i))
            {
                Thread mycrawler = new webCrawler(numberOfThreads, currentCrawledPages,links, maxCrawledPages, threads,stopThreads, FirstCrawling,mylock);
                mycrawler.setName(Integer.toString(i));
                threads.add(mycrawler);
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


class lock{}

class webCrawler extends Thread{

    public int Num;
    LinkedList<String> links;

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
    int counter;
    lock mylock;

    // to store each host (key) with vector of robot.txt disallowed words (value)
    Map<String, Vector<String>> Robot_Map = new HashMap<String, Vector<String>>();

    public webCrawler(int n, int currentCrawledPages, LinkedList<String> links, int maxCrawledPages, Vector<Thread> threads, Vector<Boolean> stopThreads,boolean FirstCrawling,lock mylock) {
        Num = n;
        this.links = links;

        this.currentCrawledPages = currentCrawledPages;
        this.maxCrawledPages = maxCrawledPages;
        this.seeds = seeds;
        this.threads=threads;
        this.stopThreads=stopThreads;
        this.counter=0;
        this.FirstCrawling=FirstCrawling;
        this.mylock=mylock;

    }

    public void run() {
        while(links.size()>0 &&(!stopThreads.get(Integer.valueOf(Thread.currentThread().getName())))) {
            try{
                String Url=null;
                // synchronized(mylock){
                Url=links.getFirst();
                links.remove(Url);
                // }
                //if(Url!=null)
                getPageLinks(Url);

            }catch (IndexOutOfBoundsException e){

            }
        }
    }

    public void getPageLinks(String URL)  {
        if (!stopThreads.get(Integer.valueOf(Thread.currentThread().getName()))){
            try {

                Document document = Jsoup.connect(URL).get();

                // FOR RECRAWLING VISIT THOSE WITH THE FAMOUS DOMAINS ONLY AND UPDATE THEIR DOCUMENTS
                if (!FirstCrawling){
                    if (URL.contains(".com") || URL.contains(".net") || URL.contains(".org") || URL.contains(".co") || URL.contains(".us")) {
                        dbMaster.UpdateDocument(URL, document.toString(), Thread.currentThread().getName(), Num);
                        return;
                    }
                }

//                //--------------------------start robot part ------------------
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



                if(!ignored_URLS.contains(URL)){
                    AddToLinks(URL,document);
                    Elements linksOnPage = document.select("a[href]");


                    for (Element page : linksOnPage) {
                        String PageLink = page.attr("abs:href");

                        if(!PageLink.equals(URL)&& !links.contains(URL)) {
                            links.add(PageLink);
                        }
                    }
                    //  synchronized (links) {
                    dbMaster.insertDocument_ForInterruption(links);
                    // }
                }
            } catch (IOException | InterruptedException |IllegalArgumentException err) {
                System.err.println("For '" + URL + "': " + err.getMessage());
            }}
        else
            return;

    }

    public void AddToLinks(String URL, Document document) throws InterruptedException {
        int count=dbMaster.DBCount();
        //   System.out.println("I have the lock and I'm thread"+Thread.currentThread().getName());
        if (count< maxCrawledPages) {

            synchronized (this.mylock) {
                if(dbMaster.found("Document",document.toString(),"WebCrawler")==null) {
                    dbMaster.insertDocument(document.toString(), URL, Thread.currentThread().getName(), Num);
                    System.out.println(URL + " my count= " + count);
                }
                else
                    return;
            }
        }

        if(count >= maxCrawledPages) {
            currentCrawledPages = 0;
            for(int i=0;i<Num;i++)
                stopThreads.set(i, true);
            return;
        }
        return;
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
        // System.out.println("robot url");
        // prepare the url of the robot: protocol+ host + file
        String robot_url = url_temp.getProtocol()+"://" + url_temp.getHost() + "/robots.txt";
        //System.out.println(robot_url);
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
