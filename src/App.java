import org.jsoup.Jsoup;
import java.util.*;

public class App {
    public static void main(String[] args) throws InterruptedException {

        Scanner sc = new Scanner(System.in); // System.in is a standard input stream
        System.out.print("Enter first number- ");
        int a = sc.nextInt();

        Thread Arr[] = new Thread[a];
        for (int i = 0; i < a; i++) {
            Arr[i].setName(Integer.toString(i));
            Arr[i].start();
        }

     //   new WebCrawler().getPageLinks("http://www.mkyong.com/",a);

    }
}

class webCrawler implements Runnable {

    public int Num;
// hashiing

public webCrawler( int n) {
    Num =n;
   // links = new HashSet<String>();
}
    public void run() {
        for (int i = 0; i < Num; i++) {
            if (Integer.parseInt(Thread.currentThread().getName()) == i) {

                getPageLinks("http://www.mkyong.com/");
            }

        }

    }

    public void getPageLinks(String URL) {
        //4. Check if you have already crawled the URLs 
        //(we are intentionally not checking for duplicate content in this example)
       // if (!links.contains(URL)) {
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
                    getPageLinks(page.attr("abs:href"));
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        
    }

}
