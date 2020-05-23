import opennlp.tools.stemmer.snowball.SnowballStemmer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

public class queryProcessor {

    String text;

    public queryProcessor(String searchText)
    {
        text = searchText;
    }


    public String springCleaning() throws IOException {
        SnowballStemmer stemmer = new SnowballStemmer(ENGLISH);

        text = (String) stemmer.stem(text);

        text = removeStopwords(text);


        return text;
    }

    public String removeStopwords(String query) throws IOException {
        List<String> stopwords = Files.readAllLines(Paths.get("stopwords.txt"));

        ArrayList<String> allWords =
                Stream.of(query.toLowerCase().split(" "))
                        .collect(Collectors.toCollection(ArrayList<String>::new));
        allWords.removeAll(stopwords);

        String result = allWords.stream().collect(Collectors.joining(" "));


        //assertEquals(result, target);
        return result;

    }

}

