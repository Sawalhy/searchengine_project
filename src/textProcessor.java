import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.util.Span;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

public class textProcessor {

    Connection con;


    public textProcessor() throws ClassNotFoundException {
        //////////////////////////////////////////////////////////////////////////////////////////////////////CONNECT TO DATABASE
        Class.forName("com.mysql.cj.jdbc.Driver");

        String url = "jdbc:mysql://localhost:3306/searchindex?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        String user = "root";
        String pass = "";
        try {
            con = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String removeStopwords(String text) throws IOException {
        List<String> stopwords = Files.readAllLines(Paths.get("stopwords.txt"));

        ArrayList<String> allWords =
                Stream.of(text.toLowerCase().split(" "))
                        .collect(Collectors.toCollection(ArrayList<String>::new));
        allWords.removeAll(stopwords);

        String result = allWords.stream().collect(Collectors.joining(" "));


        return result;

    }

    public String stemming(String text) throws IOException {
        SnowballStemmer stemmer = new SnowballStemmer(ENGLISH);

        ArrayList<String> allWords = Stream.of(text.split(" ")).collect(Collectors.toCollection(ArrayList<String>::new));

        for (int i = 0; i < allWords.size() ; i++) {
            allWords.set(i, (String) stemmer.stem(allWords.get(i)));
        }

        String result = allWords.stream().collect(Collectors.joining(" "));

        return result;
    }


    public void storeQuery(String text) throws ClassNotFoundException, SQLException {

        ////////////////////////////////////////////////////////////////////////////////// Save

        String sqlQuery = "INSERT INTO `queryhistory` (`queryText`) VALUES ('" + text +  "');";
        Statement stat = con.createStatement();

        stat.executeUpdate(sqlQuery);


    }

    public void trendDetect(String text,String country) throws IOException, SQLException {
        /////////////////////////////////////////////////////////////////////////////////    Trend detection

        String[] tokens = text.split(" ");


        InputStream inputStreamNameFinder = new FileInputStream("en-ner-person.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStreamNameFinder);
        NameFinderME nameFinder = new NameFinderME(model);
        Span nameSpans[] = nameFinder.find(tokens);




        Statement stat = con.createStatement();
        for(Span s: nameSpans)
        {
            String sqlQuery = "INSERT INTO `personsearchhistory` (`searchCountry`, `personName`) VALUES ('" + country +"', '" + (tokens[s.getStart()]).toLowerCase() + " " + (tokens[s.length()-1]).toLowerCase() +    "');";
            stat.executeUpdate(sqlQuery);
        }

    }


}
