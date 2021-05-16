import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Vector;
import javax.lang.model.util.Elements;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
 

class App {
    public static void main(String[] args) throws InterruptedException {

        Scanner sc = new Scanner(System.in); // System.in is a standard input stream
        System.out.print("Enter first number- ");

        int a = sc.nextInt();
        Runnable obj1 = new webCrawler( a );

        for (int i = 0; i < a; i++) {
            Thread Arr = new Thread(obj1);
            Arr.setName(Integer.toString(i));

            Arr.start();
        }

        //   new WebCrawler().getPageLinks("http://www.mkyong.com/",a);

    }
}

class webCrawler implements Runnable {

    public int Num;
    // hashiing
    private HashSet<String> links;

    public webCrawler( int n) {
        Num =n;
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
                System.out.println (Thread.currentThread().getName() + "hellllo");
            }

        }

    }

    public void getPageLinks(String URL) throws MalformedURLException {
        //4. Check if you have already crawled the URLs
        //(we are intentionally not checking for duplicate content in this example)
        Vector<String> no_read = new Vector<String>(1, 1);
      //  int size =no_read.capacity();
        if (!links.contains(URL)) {
            no_read= robot_file(  URL);
            for ( int i =0; i< no_read.capacity() ; i++)
            {
                if (  URL.contains(no_read.get(i)))
                    break;
            }

            try {
                //4. (i) If not add it to the index
                if (links.add(URL)) {
                    System.out.println(URL);
                }

                //2. Fetch the HTML code
                Document document = Jsoup.connect(URL).get();
                //3. Parse the HTML to extract links to other URLs
                Elements linksOnPage = document.select("a[href]");

                //5. For each extracted URL... go back to Step 4.
                for (Element page : linksOnPage) {
                    Vector<String> no_read_2= new Vector<String>(1, 1);
                    no_read_2= robot_file(page.attr("abs:href"));
                    for ( int i =0; i< no_read_2.capacity() ; i++)
                    {
                        if ( page.attr("abs:href").contains(no_read_2.get(i)))
                            break;
                    }
                    getPageLinks(page.attr("abs:href"));
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }

        }
    }

    Vector<String> robot_file ( String url ) throws MalformedURLException {
        Vector<String> v = new Vector<String>(1, 1);
         URL url1= new URL(url );
        String ss= url1.getHost()  + "/robots.txt";
        try(BufferedReader my_buffer = new BufferedReader(
             
                
                new InputStreamReader(new URL(ss).openStream()))) {
            String line = null;
            String never_read_me= null;


            while((line = my_buffer.readLine()) != null) {
                System.out.println(line);
                if ( line.contains("User-Agent: *"))
                {
                    System.out.println(" i am in user agent");
                    line = my_buffer.readLine();
                    while(line != "User-Agent: *" && line != "")
                    {
                        System.out.println("i am the line "+line);
                        never_read_me =   line.substring(10);
                        System.out.println("never_read_me "+never_read_me);
                        v.add( never_read_me);
                    }
                    line = my_buffer.readLine();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
      //  System.out.println("robot finished");
        return v;
    }
 //boolean can_read ( stri)
}
















