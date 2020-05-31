
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class crawler implements Runnable {
    private final long startTime;
    private static Database db;
    public crawler() {
        this.startTime = System.currentTimeMillis();
    }

    private void crawl(Set<URL> urls) throws IOException, SQLException {
        if (!urls.isEmpty()) {
            try {
                for (URL url : urls) {
                    boolean Columns = db.selectDB(url.toString(), "trial", "trialurl");
                    if (!Columns) {
                        System.out.println("time= " + (System.currentTimeMillis() - this.startTime) + " connected to " + url);
                        String urlText = url.toString();
                        char[] ch = new char[urlText.length()];
                        int count = 0;
                        int size = 0;
                        for (int j = 0; j < urlText.length(); j++) {
                            ch[j] = urlText.charAt(j);
                            if (ch[j] == '/' && count < 3) {
                                size++;
                                count++;
                            } else if (ch[j] != '/' && count < 3) {
                                size++;
                            } else {
                                break;
                            }

                        }
                        char[] homeUrlChar2 = new char[size];
                        for (int i = 0; i < size; i++) {
                            homeUrlChar2[i] = ch[i];

                        }
                        String homeURL = new String(homeUrlChar2);
                        BufferedReader in = null;
                        if(count==3) {
                            in= new BufferedReader(new InputStreamReader(new URL(homeURL + "robots.txt").openStream()));
                        }else if(count==2){
                            in= new BufferedReader(new InputStreamReader(new URL(homeURL + "/robots.txt").openStream()));
                        }
                        String line = null;
                        Boolean dontCrawl = false;
                        while ((line = in.readLine()) != null) {
                            if (line.equals("User-agent: *")) {
                                String line2 = in.readLine();
                                while (line2.contains("Disallow: ")) {
                                    String toRemove = "Disallow: ";
                                    int index = line2.indexOf(toRemove);
                                    line2 = line2.substring(0, index) + line2.substring(index + toRemove.length(), line2.length());
                                    boolean isFound1 = db.selectDB(url.toString(), "trial", "trialurl");
                                    if ((url.toString()).contains(line2) || isFound1) {
                                        dontCrawl = true;
                                    }
                                    line2 = in.readLine();
                                }
                                break;
                            }
                            break; //added now
                        }
                        Set<URL> newURLS = new HashSet<>();
                        if (!dontCrawl) {
                            final Document document = Jsoup.connect(url.toString()).get();
                            final Elements linksOnPage = document.select("a[href]");
                            for (final Element element : linksOnPage) {
                                final String urlContent = element.attr("abs:href");
                                final URL discoveredURL = new URL(urlContent);
                                newURLS.add(discoveredURL);
                            }
                            synchronized (db) {
                                for (URL url2 : newURLS) {
                                    boolean isFound = db.selectDB(url2.toString(), "newlinkstrial", "newlinksurl");
                                    if (!isFound) {

                                        db.InsertintoDB(url2.toString(), "newlinkstrial", "newlinksurl");
                                    }
                                }
                            }

                            if (!(db.selectDB(url.toString(), "trial", "trialurl"))) {
                                synchronized (db) {
                                    db.InsertintoDB(url.toString(), "trial", "trialurl");
                                    db.deleteDB(url.toString(), "newlinkstrial", "newlinksurl");
                                }
                            }
                        }
                    }

                }


            } catch (final Exception | Error error) {

            }
            Set<URL> newUrls=db.selectSome("newlinkstrial","newlinksurl");
            synchronized (db) {
                for (URL url1 : newUrls) {
                    db.deleteDB(url1.toString(), "newlinkstrial", "newlinksurl");
                }
            }
            if(!newUrls.isEmpty()) {
                crawl(newUrls);
            }
        }
    }

    public static class Database {
        private Connection conn;
        final String DATABASE_NAME="spiderdb";
        final String USER_NAME="root";
        final String USER_PASSWORD="turtledove";
        final String DRIVER="com.mysql.jdbc.Driver";
        public Connection openConnection() throws ClassNotFoundException {
            if(conn==null){
                String url="jdbc:mysql://localhost:3306/spiderdb?useUnicode=true&useJDBCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
                String dbName=DATABASE_NAME;
                String driver=DRIVER;
                String userName=USER_NAME;
                String password=USER_PASSWORD;
                try{
                    Class.forName(driver);
                    this.conn=DriverManager.getConnection(url,userName,password);
                    System.out.println("Successful connection to database");
                }catch (SQLException e){
                    System.out.println("Failed to connect to database");
                }

            }
            return conn;
        }
        public void closeConnection() throws ClassNotFoundException, SQLException {
            if (this.conn!=null){
                String url="jdbc:mysql://localhost:3306/spiderdb?useUnicode=true&useJDBCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
                String dbName=DATABASE_NAME;
                String driver=DRIVER;
                String userName=USER_NAME;
                String password=USER_PASSWORD;
                Class.forName(driver);
                DriverManager.getConnection(url,userName,password).close();
            }
        }
        public void InsertintoDB(String url,String tableName,String columnName) throws SQLException{

            String query = "INSERT INTO `" + tableName + "`(`" + columnName + "`) VALUES (" + "'" + url + "'" + ")";
            var statement = this.conn.prepareStatement(query);
            statement.executeUpdate();

        }
        public  void deleteDB(String url,String tableName,String columnName) throws SQLException {

            String query = "DELETE FROM `" + tableName + "` WHERE `" + columnName + "`='" + url + "'";
            var statement = this.conn.prepareStatement(query);
            statement.executeUpdate();
        }
        public  boolean selectDB(String url, String tableName, String columnName) throws SQLException{

            String query = "SELECT  `" + columnName + "` FROM `" + tableName + "` WHERE `" + columnName + "`= '" + url + "'";
            var statement = this.conn.prepareStatement(query);
            ResultSet mySQLIResult = statement.executeQuery(query);
            return mySQLIResult.next();

        }
        public  Set<URL> selectSome(String tableName, String columnName) throws SQLException, MalformedURLException {
            synchronized (db) {
                String query = "SELECT `" + columnName + "` FROM `" + tableName + "` ORDER BY 1  limit 2";
                var statement = this.conn.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery(query);
                Set<URL> newSet = new HashSet<>();
                while(resultSet.next()) {
                    String urlString = resultSet.getString(columnName);
                    URL newUrl = new URL(urlString);
                    newSet.add(newUrl);
                }
                return newSet;
            }
        }
        public  Set<URL> select(String tableName, String columnName, int number) throws SQLException, MalformedURLException {
            synchronized (db) {
                String query = "SELECT `" + columnName + "` FROM `" + tableName + "` ORDER BY 1  limit "+number;
                var statement = this.conn.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery(query);
                Set<URL> newSet = new HashSet<>();
                while(resultSet.next()) {
                    String urlString = resultSet.getString(columnName);
                    URL newUrl = new URL(urlString);
                    newSet.add(newUrl);
                }
                return newSet;
            }
        }

    }


    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
        final URL spider=new URL("https://www.footballhistory.org/");
        final URL spider1=new URL("https://bleacherreport.com/");
        final URL spider2=new URL("https://www.espn.com/");
        final URL spider3=new URL("https://www.independent.co.uk/sport/football");
        final URL spider4=new URL("https://www.thesun.co.uk/sport/football/");
        final URL spider6=new URL("https://www.standard.co.uk/sport/football");
        final URL spider7=new URL("https://metro.co.uk/sport/football/");
        final URL spider8=new URL("https://www.football365.com/");
        final URL spider9=new URL("https://www.telegraph.co.uk/football/");

        db= new Database();
        db.openConnection();
        boolean j = db.selectDB(spider.toString(),"newlinkstrial","newlinksurl");
        if (!j) {
            db.InsertintoDB(spider.toString(), "newlinkstrial", "newlinksurl");
        }
        boolean b = db.selectDB(spider1.toString(),"newlinkstrial","newlinksurl");
        if(!b) {
            db.InsertintoDB(spider1.toString(), "newlinkstrial", "newlinksurl");
        }
        boolean c = db.selectDB(spider2.toString(),"newlinkstrial","newlinksurl");
        if(!c) {
            db.InsertintoDB(spider2.toString(), "newlinkstrial", "newlinksurl");
        }
        boolean d = db.selectDB(spider3.toString(),"newlinkstrial","newlinksurl");
        if(!d) {
            db.InsertintoDB(spider3.toString(), "newlinkstrial", "newlinksurl");
        }
        boolean e = db.selectDB(spider4.toString(),"newlinkstrial","newlinksurl");
        if(!e) {
            db.InsertintoDB(spider4.toString(), "newlinkstrial", "newlinksurl");
        }
        boolean f = db.selectDB(spider6.toString(),"newlinkstrial","newlinksurl");
        if(!f) {
            db.InsertintoDB(spider6.toString(), "newlinkstrial", "newlinksurl");
        }
        boolean g = db.selectDB(spider7.toString(),"newlinkstrial","newlinksurl");
        if(!g) {
            db.InsertintoDB(spider7.toString(), "newlinkstrial", "newlinksurl");
        }
        boolean h = db.selectDB(spider8.toString(),"newlinkstrial","newlinksurl");
        if(!h) {
            db.InsertintoDB(spider8.toString(), "newlinkstrial", "newlinksurl");
        }
        boolean i = db.selectDB(spider9.toString(),"newlinkstrial","newlinksurl");
        if(!i) {
            db.InsertintoDB(spider9.toString(), "newlinkstrial", "newlinksurl");
        }
        Runnable webSpider=new crawler();
        Scanner sc= new Scanner(System.in); //System.in is a standard input stream.
        System.out.print("Enter the number of threads you wish to have (from 1 to 5) in the first run: ");
        int numOfThreads= sc.nextInt();
        sc.close();
        Thread t[]= new Thread[numOfThreads];
        for (int k=0;k<numOfThreads ;k++){
            t[k]=new Thread(webSpider);
            t[k].setName(Integer.toString(k));
            t[k].start();
        }
        for (int l=0 ;l<numOfThreads ; l++){
            if(t[l]!=null){
                t[l].join();
            }
        }
        db.closeConnection();
    }


    @Override
    public void run() {
        try {
            Set<URL> startUrls = db.selectSome("newlinkstrial","newlinksurl");
            synchronized (db) {
                for (URL url : startUrls) {
                    db.deleteDB(url.toString(),"newlinkstrial","newlinksurl");
                }
            }
            if (!startUrls.isEmpty()) {
                crawl(startUrls);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}

