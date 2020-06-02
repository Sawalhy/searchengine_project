import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class searchServlet extends HttpServlet {
    String query;
    ArrayList<String> result;
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String text = request.getParameter("mainBox");
        String textORimage = request.getParameter("button");
        textProcessor tp = new textProcessor();



        /////////////////////////////////////////////////////////////////////////////////    Trend detection
        String strArray[] = text.split(" ");


        InputStream inputStreamNameFinder = new FileInputStream("en-ner-person.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStreamNameFinder);
        NameFinderME nameFinder = new NameFinderME(model);
        Span nameSpans[] = nameFinder.find(strArray);




        //////////////////////////////////////////////////////////////////////////////////


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
            textSearch srch = new textSearch(searchWords);
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


        for(Span s: nameSpans)
            page = page + (s.toString());


        response.getWriter().println(page);
    }

}
