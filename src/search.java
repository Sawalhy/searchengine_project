import java.sql.*;

public class search {

    Connection con;

    public void/*Array of strings corresponding to the*/ searchExcute() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/searchindex?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        String user = "root";
        String pass = "";
        con = DriverManager.getConnection(url,user,pass);

        if (con != null)
        {

        }

    }

    public void ranker() throws SQLException {
        String sqlQuery = "SELECT * FROM `wordsindex`";
        PreparedStatement statement = con.prepareStatement(sqlQuery);
        ResultSet set = statement.executeQuery();
        set.next();
        set.getString("Word");
    }


}
