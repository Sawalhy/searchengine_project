import java.sql.*;
import java.util.ArrayList;

public class search {

    Connection con;
    String searchWord;

    public search(String input) {
        searchWord = input;
    }

    public ArrayList<String> searchExcute() throws SQLException, ClassNotFoundException {
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

    public ArrayList<String> ranker() throws SQLException {
        String sqlQuery = "SELECT URL,(q2.TermFreq * q1.IDF) AS TFIDF,q2.HFreq,q2.InTitle FROM (SELECT * FROM `wordsindex` WHERE Word =  \"sun\" ) q1 JOIN (SELECT * FROM wordsurlsindex WHERE TermFreq > 0) q2 ON q1.Word = q2.Word ORDER BY TFIDF DESC;";
        PreparedStatement statement = con.prepareStatement(sqlQuery);
        ResultSet set = statement.executeQuery();

        ArrayList<String> result = new ArrayList<>();
        int i = 0;
        while (set.next()) {
            result.add(i, set.getString("URL"));
            i++;
        }
        return result;


    }
}
