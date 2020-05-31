import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;


public class searchServlet extends HttpServlet {

    String query;
    String result;
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String text = request.getParameter("mainBox");

        textProcessor tp = new textProcessor();


        //ADD SQL INJECTION PROTECTOR
        if (text.charAt(0) == '"' && text.charAt(text.length()-1) == '"')
        {
            query = "ama nshoof han3mel elzeft dh ezai";
        }
        else
        {
            query = tp.stemming(text);
            query = tp.removeStopwords(query);
        }

        search srch = new search();

        try {
            result = srch.searchExcute(query);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


/////////////////////////////////////
        response.setContentType("text/html");
        String page = "<!doctype html> <html> <body> <h1>" + result +" </h1> </body></html>";
        response.getWriter().println(page);
    }

}
