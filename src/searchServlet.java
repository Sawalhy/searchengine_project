import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class searchServlet extends HttpServlet {
    String query;
    ArrayList<String> result;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ////////////////////////////////////////////////////////////////////////////////////////////////////GET REQUEST PARAMETERS
        String text = request.getParameter("mainBox");
        String textORimage = request.getParameter("button");
        String country = request.getParameter("country");

        //////////////////////////////////////////////////////////////////////////////////////////////////
        if(text == null || text.equals(""))
        {
            if(country.equals("none"))
            {
                country = "Egypt";
            }
            trends trnd = new trends();
            String trendsPage= null;
            try {
                trendsPage = trnd.trendsPage(country);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            response.setContentType("text/html");
            response.getWriter().println(trendsPage) ;
            return;
        }
       ////////////////////////////////////////////////////////////////////////////////////////////////////
        textProcessor tp = null;
        try {
            tp = new textProcessor();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        /////////////////////////////////////////////////////////////////////////////////

        try {
            tp.trendDetect(text,country);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        try {
            tp.storeQuery(text);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        text = text + " ";//ensure that text won't be empty
        if (text.charAt(0) == '"' && text.charAt(text.length()-1) == '"')
        {
            query = "ama nshoof han3mel elzeft dh ezai";
        }
        else
        {
            query = tp.stemming(text);
            query = tp.removeStopwords(query);
        }
        ArrayList<String> searchWords = Stream.of(query.split(" ")).collect(Collectors.toCollection(ArrayList<String>::new));

        String page = null;

        if (textORimage.equals("Search"))
        {
            textSearch srch = new textSearch(searchWords,country);
            try {
                page = srch.searchExcute();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else if(textORimage.equals("ImageSearch"))
        {
            imageSearch srch = new imageSearch(searchWords);
            try {
                page = srch.searchExcute();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

///////////////////////////////////// INTERFACE
        response.setContentType("text/html");
        response.getWriter().println(page) ;
    }

}
