import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class NameGender extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String name = request.getParameter("Name");
        String gender = request.getParameter("Gender");

        queryProcessor qp = new queryProcessor(name);

        String test = qp.springCleaning();


        String message = test;

        response.setContentType("text/html");

        String page = "<!doctype html> <html> <body> <h1>" + message +" </h1> </body></html>";
        response.getWriter().println(page);
    }

}
