import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class searchServlet extends HttpServlet {

    String body;
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String text = request.getParameter("mainBox");

        textProcessor tp = new textProcessor();


        //ADD SQL INJECTION PROTECTOR
        if (text.charAt(0) == '"' && text.charAt(text.length()-1) == '"')
        {
            body = "ama nshoof han3mel elzeft dh ezai";
        }
        else
        {
            body = tp.stemming(text);
            body = tp.removeStopwords(body);
        }

        response.setContentType("text/html");

        String page = "<!doctype html> <html> <body> <h1>" + body +" </h1> </body></html>";
        response.getWriter().println(page);
    }

}
