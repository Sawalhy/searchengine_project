import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

public class textProcessor {

    public String removeStopwords(String text) throws IOException {
        List<String> stopwords = Files.readAllLines(Paths.get("src/stopwords.txt"));

        ArrayList<String> allWords =
                Stream.of(text.toLowerCase().split(" "))
                        .collect(Collectors.toCollection(ArrayList<String>::new));
        allWords.removeAll(stopwords);

        String result = allWords.stream().collect(Collectors.joining(" "));


        return result;

    }

    public String stemming(String text) throws IOException {
        SnowballStemmer stemmer = new SnowballStemmer(ENGLISH);

        ArrayList<String> allWords = Stream.of(text.split(" ")).collect(Collectors.toCollection(ArrayList<String>::new));

        for (int i = 0; i < allWords.size() ; i++) {
            allWords.set(i, (String) stemmer.stem(allWords.get(i)));
        }

        String result = allWords.stream().collect(Collectors.joining(" "));

        return result;
    }

//might need to reimplement without merging

}