import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class searchServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String text = request.getParameter("mainBox");

        queryProcessor qp = new queryProcessor(text);


        String message = text;

        response.setContentType("text/html");

        String page = "<!doctype html> <html> <body> <h1>" + message +" </h1> </body></html>";
        response.getWriter().println(page);
    }

}
