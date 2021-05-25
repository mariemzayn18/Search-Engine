import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import org.jsoup.nodes.Document;

public class Indexer {

    public void indexer(Document Doc) {

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
