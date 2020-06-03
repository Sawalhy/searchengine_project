import java.io.*;
import java.io.File;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class TestScrap {
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, InterruptedException {
        long time = System.currentTimeMillis();
        int x=0;
        //////////// The file containing all the country extensions and their corresponding country  ///////////
        File myObj = new File("src/domains.txt");
        Scanner myReader = new Scanner(myObj);
        //////////  Create two different arrays, one containing the domains and one containing country names ////////
        ArrayList<String> domains = new ArrayList<String>();
        ArrayList<String> countries = new ArrayList<String>();
        boolean isLink = false;
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            if (data.isEmpty())continue;
            if(!isLink){
                domains.add(data);
            }else {
                countries.add(data);
            }
            isLink=!isLink;
        }
        myReader.close();
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////// connecting to our database//////////////////
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url1 = "jdbc:mysql://localhost:3306/searchindex?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        String user1 = "root";
        String pass1 = "";
        Connection con1 = DriverManager.getConnection(url1, user1, pass1);
        if (con1 != null) {
            System.out.println("connection successful");
        }
        ///////////////////////////////////////////////////////////
        /////////// Getting all the crawled URLS //////////////////
        String queryMain = "SELECT trialurl FROM trial;";
        var statMain = con1.prepareStatement(queryMain);
        ResultSet rmain = statMain.executeQuery();
        while (rmain.next()) {
            x++;
            String websiteLink = rmain.getString(1);
            //////// another query to check if this url was indexed before (for incremental update)//////////
            String checkQuery = "SELECT * FROM WORDSURLSINDEX WHERE URL= \""+websiteLink+"\";";
            var indexedBefore = con1.prepareStatement(checkQuery);
            ResultSet rBefore = indexedBefore.executeQuery();

            System.out.println(x+" "+websiteLink);

            if (rBefore.first()) continue;
            /////////////////////////////////////////////////////////////////////////////////////////////////
            Document doc;
            //// connecting to the crawled URL, and ignoring ones that throw exceptions /////////
            //// like URLs that were removed from the web and ones that block robots ///////////
            try {
                doc = Jsoup.connect(websiteLink).referrer("http://www.google.com").userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36").get();
            }catch (UnknownHostException e){
                continue;
            }
            catch (SocketTimeoutException e){
                continue;
            }
            catch (HttpStatusException e){
                continue;
            }

            ///////////////////////////////////////////////////////////////
            /// an algorithm to check for the location of websites from their extensions //////
            boolean hasLocation=false;
            String siteCountry = "";
            for (int i=0;i<249;i++){
                int indx= websiteLink.indexOf(domains.get(i));
                if (indx>=12){
                    String check = websiteLink.substring(indx,websiteLink.length());
                    if(check.length()==3) {
                        siteCountry = countries.get(i);
                        hasLocation = true;
                        break;
                    }
                    check = websiteLink.substring(indx,indx+4);
                    if(check.endsWith(".")||check.endsWith("/")){
                        siteCountry = countries.get(i);
                        hasLocation = true;
                        break;
                    }
                }
            }
            /////////////////////////////////////////////////////////////
            // The hashmaps that will contain the words and images objects //
            Map<String, Word> countMap = new HashMap<String, Word>();
            Map<String, Img> picsMap = new HashMap<String, Img>();
            //Get the actual text from the page, excluding the HTML and run the stemmer and remove stop words from it
            textProcessor cleaner = new textProcessor();

            if(doc.documentType()==null)continue;
            String title = doc.title().toLowerCase();
            String text = doc.body().text();
            title = cleaner.stemming(title);
            text = cleaner.removeStopwords(text);
            text = cleaner.stemming(text);

            String allH1 = doc.select("h1").text();
            allH1 = cleaner.removeStopwords(allH1);
            allH1 = cleaner.stemming(allH1);

            String allH2 = doc.select("h2").text();
            allH2 = cleaner.removeStopwords(allH2);
            allH2 = cleaner.stemming(allH2);

            String allH3 = doc.select("h3").text();
            allH3 = cleaner.removeStopwords(allH3);
            allH3 = cleaner.stemming(allH3);
            String date=null;
            String dateGet = doc.select("time").attr("datetime");
            if(!dateGet.isEmpty()&&dateGet.length()>=10)
                date=dateGet.substring(0,10);
            ///////////////////////////////////////////////////////////////////////

            //////////// --------- For images searching ------ ///////////////
            //////////// here we index all the images on the page with their keywords //////////////
            org.jsoup.select.Elements allImgLinks = (org.jsoup.select.Elements) doc.select("img");
            for (Element img : allImgLinks) {
                String url = img.attr("src");
                if (!url.startsWith("http"))continue;
                Img imgObj = picsMap.get(url);
                if (imgObj == null) {
                    imgObj = new Img();
                    imgObj.URL = url;
                    picsMap.put(url, imgObj);
                }
                String imgtext=img.attr("alt");
                imgtext=cleaner.removeStopwords(imgtext);
                imgtext=cleaner.stemming(imgtext);
                String[] description = imgtext.split("[^A-ZÃƒâ€¦Ãƒâ€žÃƒâ€“a-zÃƒÂ¥ÃƒÂ¤ÃƒÂ¶]+");
                imgObj.keyWords = description;
            }
            //////////////////////////////////////////////////////////////////////////////////////

            int totalWordsCount = 0;
            System.out.println(title);
            // Create BufferedReader so the words can be counted ///
            // If a word is in a header or in title, we edit these properties in its object ///
            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("[^A-ZÃƒâ€¦Ãƒâ€žÃƒâ€“a-zÃƒÂ¥ÃƒÂ¤ÃƒÂ¶]+");
                for (String word : words) {
                    if ("".equals(word)) {
                        continue;
                    }

                    Word wordObj = countMap.get(word);
                    if (wordObj == null) {
                        wordObj = new Word();
                        wordObj.word = word;
                        wordObj.count = 0;
                        countMap.put(word, wordObj);
                    }
                    totalWordsCount++;
                    wordObj.count++;
                }
            }

            reader.close();
            reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(allH1.getBytes(StandardCharsets.UTF_8))));
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("[^A-ZÃƒâ€¦Ãƒâ€žÃƒâ€“a-zÃƒÂ¥ÃƒÂ¤ÃƒÂ¶]+");
                for (String word : words) {
                    if ("".equals(word)) {
                        continue;
                    }
                    Word wordObj = countMap.get(word);
                    if (wordObj == null) {
                        wordObj = new Word();
                        wordObj.word = word;
                        wordObj.count = 0;
                        countMap.put(word, wordObj);
                        totalWordsCount++;
                        wordObj.count++;
                    }
                    wordObj.hFreq++;
                }
            }
            reader.close();

            reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(allH2.getBytes(StandardCharsets.UTF_8))));
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("[^A-ZÃƒâ€¦Ãƒâ€žÃƒâ€“a-zÃƒÂ¥ÃƒÂ¤ÃƒÂ¶]+");
                for (String word : words) {
                    if ("".equals(word)) {
                        continue;
                    }
                    Word wordObj = countMap.get(word);
                    if (wordObj == null) {
                        wordObj = new Word();
                        wordObj.word = word;
                        wordObj.count = 0;
                        countMap.put(word, wordObj);
                        totalWordsCount++;
                        wordObj.count++;
                    }
                    wordObj.hFreq++;
                }
            }
            reader.close();
            reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(allH3.getBytes(StandardCharsets.UTF_8))));
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("[^A-ZÃƒâ€¦Ãƒâ€žÃƒâ€“a-zÃƒÂ¥ÃƒÂ¤ÃƒÂ¶]+");
                for (String word : words) {
                    if ("".equals(word)) {
                        continue;
                    }
                    Word wordObj = countMap.get(word);
                    if (wordObj == null) {
                        wordObj = new Word();
                        wordObj.word = word;
                        wordObj.count = 0;
                        countMap.put(word, wordObj);
                        totalWordsCount++;
                        wordObj.count++;
                    }
                    wordObj.hFreq++;
                }
            }
            reader.close();
            reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(title.getBytes(StandardCharsets.UTF_8))));
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("[^A-ZÃƒâ€¦Ãƒâ€žÃƒâ€“a-zÃƒÂ¥ÃƒÂ¤ÃƒÂ¶]+");
                for (String word : words) {
                    if ("".equals(word)) {
                        continue;
                    }
                    Word wordObj = countMap.get(word);
                    if (wordObj == null) {
                        wordObj = new Word();
                        wordObj.word = word;
                        wordObj.count = 0;
                        countMap.put(word, wordObj);
                        totalWordsCount++;
                        wordObj.count++;
                    }
                    wordObj.inTitle = true;
                }
            }
            reader.close();
            ///////////////////////////////////////////////////////////////////////////////
            //////  Insert images into their table in the database //////////////////////
            for (Img image : picsMap.values()) {
                for (String word : image.keyWords) {
                    if(word==null||word=="")continue;
                    String query = "INSERT INTO imagesindex (ImgURL,SiteURL,RelatedWord) values ('" + image.URL
                            + "','" + websiteLink + "','" + word + "');";
                    var statement2 = con1.prepareStatement(query);
                    statement2.executeUpdate();
                }
            }
            /////    Insert words into the database //////////////////////
            for (Word word : countMap.values()) {
                if (word.count < 5) continue;
                String query1 = "SELECT * FROM WORDSINDEX WHERE WORD = '" + word.word + "';";
                var stat = con1.prepareStatement(query1);
                ResultSet r = stat.executeQuery();
                if (!r.first()) {
                    if(word.word.length()>25) continue;
                    String query2 = "INSERT INTO WORDSINDEX (Word,NumOfOccurrences,DocsContaining) values ('" + word.word
                            + "'," + word.count + "," + 1 + ");";
                    var statement2 = con1.prepareStatement(query2);
                    statement2.executeUpdate();

                } else {
                    String query4 = "SELECT * FROM WORDSINDEX WHERE WORD = '" + word.word + "';";
                    var statement4 = con1.prepareStatement(query4);
                    ResultSet rs = statement4.executeQuery();
                    int newNumOfOcc = 0;
                    int newDocsCont = 0;
                    if (rs.next()) {
                        newNumOfOcc = r.getInt(2);
                        newDocsCont = r.getInt(3);
                    }
                    String query3 = "UPDATE WORDSINDEX SET NumOfOccurrences = " + (word.count + newNumOfOcc) + ",DocsContaining=" + (newDocsCont + 1) +
                            " WHERE WORD='" + word.word + "';";
                    var statement3 = con1.prepareStatement(query3);
                    statement3.executeUpdate();
                }
                String query3;
                if (date!=null && hasLocation) {
                    query3 = "INSERT INTO wordsurlsindex (Word, URL, TermFreq, HFreq, InTitle, Location, Date, TotalWordsInDoc) VALUES " +
                            "('" + word.word + "',\"" + websiteLink + "\", " + word.count + "," + word.hFreq + ","
                            + word.inTitle + ",\"" + siteCountry + "\",\"" + date + "\"," + totalWordsCount + ");";
                }
                else if(date==null && hasLocation) {
                    query3 = "INSERT INTO wordsurlsindex (Word, URL, TermFreq, HFreq, InTitle, Location, TotalWordsInDoc) VALUES " +
                            "('" + word.word + "',\"" + websiteLink + "\", " + word.count + "," + word.hFreq + ","
                            + word.inTitle + ",\"" + siteCountry + "\"," + totalWordsCount + ");";
                }
                else if(date!=null && !hasLocation) {
                    query3 = "INSERT INTO wordsurlsindex (Word, URL, TermFreq, HFreq, InTitle, Date, TotalWordsInDoc) VALUES " +
                            "('" + word.word + "',\"" + websiteLink + "\", " + word.count + "," + word.hFreq + ","
                            + word.inTitle + ",\"" + date + "\"," + totalWordsCount + ");";
                }
                else {
                    query3 = "INSERT INTO wordsurlsindex (Word, URL, TermFreq, HFreq, InTitle, TotalWordsInDoc) VALUES " +
                            "('" + word.word + "',\"" + websiteLink + "\", " + word.count + "," + word.hFreq + ","
                            + word.inTitle + "," + totalWordsCount + ");";
                }
                var statement = con1.prepareStatement(query3);
                statement.executeUpdate();
            }
        }
        ////////////////////////////////////////////////////////////////
        time = System.currentTimeMillis() - time;

        System.out.println("Finished in " + (time/1000)/60 + " m "+ (time/1000)%60+" s");
    }

    public static class Img {
        String URL;
        String[] keyWords;
    }

    public static class Word implements Comparable<Word> {
        String word;
        int count;
        int hFreq;
        boolean inTitle = false;

        @Override
        public int hashCode() {
            return word.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return word.equals(((Word) obj).word);
        }

        @Override
        public int compareTo(Word b) {
            return b.count - count;
        }
    }
}