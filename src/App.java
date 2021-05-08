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

    }
}

class webCrawler implements Runnable {

    public int Num;

    public void run() {
        for (int i = 0; i < Num; i++) {
            if (Integer.parseInt(Thread.currentThread().getName()) == i) {

            }

        }

    }

}
