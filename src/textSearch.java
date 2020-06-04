import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class textSearch {

    Connection con;
    ArrayList<String> searchWords;
    String country;

    public textSearch(ArrayList<String> input,String countri) {
        searchWords = input;
        country = countri;
    }

    public String searchExcute() throws SQLException, ClassNotFoundException, IOException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/searchindex?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        String user = "root";
        String pass = "";
        con = DriverManager.getConnection(url, user, pass);

        if (con != null) {
            return ranker();
        }
        return null;
    }

    public String ranker() throws SQLException, IOException {


        String sqlQuery = "SELECT SUM(TFIDF * IF(STRCMP(loc,\"" + country +"\"), 0.01, 1) * DATEDIFF(Date,CURRENT_DATE()) ) AS Score,URL,Date FROM (SELECT URL,(q2.TermFreq * q1.IDF) AS TFIDF,q2.HFreq,q2.InTitle,q2.Location AS loc,q2.Date FROM (SELECT * FROM `wordsindex` WHERE Word = ";

        for (int i = 0; i < searchWords.size() ; i++) {
           sqlQuery = sqlQuery + '"' + searchWords.get(i) + '"';
           if(searchWords.size() -1 -i != 0)
           {
               sqlQuery = sqlQuery + " OR Word = ";
           }
        }

        sqlQuery = sqlQuery + ") q1 JOIN (SELECT * FROM wordsurlsindex WHERE TermFreq > 0) q2 ON q1.Word = q2.Word) main GROUP BY URL ORDER BY `Score` DESC;";


        PreparedStatement statement = con.prepareStatement(sqlQuery);
        ResultSet set = statement.executeQuery();

        ArrayList<String> result = new ArrayList<>();

        int i = 0;
        while (set.next()) {
            result.add(i, set.getString("URL"));
            i++;
        }

        if(result.size() == 0)
        {
            result.add("<h1>No results found</h1>");
        }

        interfaceHelper iHelp = new interfaceHelper();

        String page = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "\n" +
                "<head>\n" +
                "    <title>Search results</title>\n" +
                "    <link rel=\"stylesheet\" href=\"searched.css\">\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "    <img src=\"https://i.ibb.co/7XV6FQK/logo.png\" class=\"main-logo\" alt=\"logo1\" width=\"200px\" height=\"70px\">\n" +
                "    <div class=\"searchResults\">\n";
        for (int j = 0; j < result.size() ; j++) {
            page = page + "<div class=\"Result\">\n" +
                    "            <a href=\""+ result.get(j) +" \">\n" +
                    "                <h3 class=\"resultTitle\"> Title </h3>\n" +  //+ iHelp.GetTitle(result.get(j) ) +
                    "                <div class=\"resultLink\">" +  result.get(j) + "</div>\n" +
                    "                <p class=\"resultSnippet\"> Snippet .</p class=\"resultSnippet\">\n" + //+ iHelp.getParagraph(result.get(j),searchWords.get(0)) +
                    "            </a>\n" +
                    "        </div>";
        }
        page = page +  " </div>\n" +
                "</body>\n" +
                "\n" +
                "</html>" ;

        con.close();

        return page;


    }
}
