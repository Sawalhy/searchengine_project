import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class phraseSearch {
    Connection con;
    ArrayList<String> searchWords;

    public phraseSearch(ArrayList<String> input) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/searchindex?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        String user = "root";
        String pass = "";
        con = DriverManager.getConnection(url, user, pass);
        searchWords = input;
    }

    public String mostSig()
    {
        String sqlQuery = "SELECT Word FROM `wordsindex` WHERE Word = \"" ;

        for (int i = 0; i < searchWords.size() ; i++) {
            sqlQuery = sqlQuery + '"' + searchWords.get(i) + '"';
            if (searchWords.size() - 1 - i != 0) {
                sqlQuery = sqlQuery + " AND Word = ";
            }
        }


        sqlQuery = sqlQuery + "\" ORDER BY `IDF` DESC LIMIT 1\"";

        //Excute


        return sqlQuery;
    }






}
