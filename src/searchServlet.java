import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;


public class searchServlet extends HttpServlet {

    String query;
    ArrayList<String> result;
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String text = request.getParameter("mainBox");
        textProcessor tp = new textProcessor();


        if (text.charAt(0) == '"' && text.charAt(text.length()-1) == '"')
        {
            query = "ama nshoof han3mel elzeft dh ezai";
        }
        else
        {
            query = tp.stemming(text);
            query = tp.removeStopwords(query);
        }

        search srch = new search(query);

        try {
            result = srch.searchExcute();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


/////////////////////////////////////
        response.setContentType("text/html");
        String page = "<!doctype html> <html> <body> <h1>";
        for (int i = 0; i < result.size() ; i++) {
            page = page + result.get(i) + "<br>";
        }
        page = page +  " </h1> </body></html>" ;
        response.getWriter().println(page);
    }

}
