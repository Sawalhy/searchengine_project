import java.sql.*;
import java.util.ArrayList;

public class textSearch {

    Connection con;
    ArrayList<String> searchWords;

    public textSearch(ArrayList<String> input) {
        searchWords = input;
    }

    public String searchExcute() throws SQLException, ClassNotFoundException {
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

    public String ranker() throws SQLException {


        String sqlQuery = "SELECT URL,(q2.TermFreq * q1.IDF) AS TFIDF,q2.HFreq,q2.InTitle FROM (SELECT * FROM `wordsindex` WHERE Word = ";

        for (int i = 0; i < searchWords.size() ; i++) {
           sqlQuery = sqlQuery + '"' + searchWords.get(i) + '"';
           if(searchWords.size() -1 -i != 0)
           {
               sqlQuery = sqlQuery + " OR Word = ";
           }
        }

        sqlQuery = sqlQuery + ") q1 JOIN (SELECT * FROM wordsurlsindex WHERE TermFreq > 0) q2 ON q1.Word = q2.Word ORDER BY TFIDF DESC;";


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
                    "            <a href=\"#\">\n" +
                    "                <h3 class=\"resultTitle\">Website title</h3>\n" +
                    "                <div class=\"resultLink\">" +  result.get(j) + "</div>\n" +
                    "                <p class=\"resultSnippet\">SNIPPET.</p class=\"resultSnippet\">\n" +
                    "            </a>\n" +
                    "        </div>";
        }
        page = page +  " </div>\n" +
                "</body>\n" +
                "\n" +
                "</html>" ;

        return page;


    }
}
