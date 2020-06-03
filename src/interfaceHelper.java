import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;

public class interfaceHelper {
    public String GetTitle(String url) throws IOException {
        final Document document= Jsoup.connect(url).get();
        Document doc= Jsoup.parse(document.html());
        String title= doc.title();
        return title;
    }

    public String getParagraph(String url, String word) throws IOException {
        File f= new File("paragraph.txt");
        if(!f.exists()){
            f.createNewFile();
        }
        PrintWriter pw= new PrintWriter(f);
        Document doc = Jsoup.connect(url).get();
        Document doc2= Jsoup.parse(doc.html());
        Elements paragraphs = doc2.select("p");
        for(Element p : paragraphs) {
            pw.println(p.text());
        }
        pw.close();
        BufferedReader br = new BufferedReader(new FileReader(f));

        String st;
        String phrase = null;
        String phrase2=null;
        while ((st = br.readLine()) != null) {
            //System.out.println(st);
            if (st.contains(word)) {
                int counter=0;
                while (counter<2) {
                    if (counter == 0) {
                        phrase = st;
                        counter++;
                        st = br.readLine();
                    } else if (counter == 1) {
                        phrase2 = st;
                        counter++;
                    }
                }
                return phrase+phrase2;
            }
            st = br.readLine();
        }
        return phrase+phrase2;

    }


}
