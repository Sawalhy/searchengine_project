import java.sql.*;
import java.util.ArrayList;

public class imageSearch {

    Connection con;
    ArrayList<String> searchWords;

    public imageSearch(ArrayList<String> input) {
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


        String sqlQuery = "SELECT imagesindex.ImgURL,IDF FROM (`imagesindex` JOIN wordsindex ON RelatedWord = wordsindex.Word) WHERE RelatedWord = ";

        for (int i = 0; i < searchWords.size() ; i++) {
            sqlQuery = sqlQuery + '"' + searchWords.get(i) + '"';
            if(searchWords.size() -1 -i != 0)
            {
                sqlQuery = sqlQuery + " OR Word = ";
            }
        }


        PreparedStatement statement = con.prepareStatement(sqlQuery);
        ResultSet set = statement.executeQuery();

        ArrayList<String> result = new ArrayList<>();

        int i = 0;
        while (set.next()) {
            result.add(i, set.getString("imgURL"));
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
                "    <title>Title of the textSearch result</title>\n" +
                "    <link rel=\"stylesheet\" href=\"searched.css\">\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "    <img src=\"https://i.ibb.co/7XV6FQK/logo.png\" class=\"main-logo\" alt=\"logo1\" width=\"200px\" height=\"70px\">\n" +

                "    <div class=\"imageResults\">";


        for (int j = 0; j < result.size() ; j++) {
            page = page + "<div class=\"imageResult\">\n" +
                    "            <a href=\"#\">\n" +
                    "                <img class=\"resultImage\" src=\"" +  result.get(j) + "\" alt=\"search result\">\n" +
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
