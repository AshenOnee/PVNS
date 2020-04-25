package tools;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.io.StringReader;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;

public class TextTools {
    public static ArrayList<String> splitOnSentences(String text) {
        ArrayList<String> sentences = new ArrayList<>();
        text = text.replace("\r\n", " ");
        text = text.replace("\n", " ");

        BreakIterator iterator = BreakIterator.getSentenceInstance(new Locale("ru"));
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String sentence = text.substring(start,end);

            boolean f = false;
            char[] chars = sentence.toCharArray();
            for (char aChar : chars)
                if (Character.isAlphabetic(aChar)) {
                    f = true;
                    break;
                }
            if(sentence.length() > 0 && f)
                sentences.add(sentence);
        }
        return sentences;
    }

    public static ArrayList<String> getUniqueStems(ArrayList<String> stems){
        ArrayList<String> uniqueStems = new ArrayList<>();
        for(String stem : stems)
            if(!uniqueStems.contains(stem)) uniqueStems.add(stem);

        return uniqueStems;
    }

    public static double[][] calcTf(ArrayList<String> uniqueStems, ArrayList<ArrayList<String>> sentencesStems){
        double[][] tf = new double[sentencesStems.size()][uniqueStems.size()];

        for(int i = 0; i < sentencesStems.size(); i++)
            for (int j = 0; j < sentencesStems.get(i).size(); j++)
                for(int k = 0; k < uniqueStems.size(); k++)
                    if(sentencesStems.get(i).get(j).equals(uniqueStems.get(k))){
                        tf[i][k] += 1.0;
                        break;
                    }

        for(int i = 0; i < sentencesStems.size(); i++)
            for (int j = 0; j < uniqueStems.size(); j++)
                tf[i][j] = tf[i][j]/sentencesStems.get(i).size();

        return tf;
    }

    public static double[] calcIdf(ArrayList<String> uniqueStems, ArrayList<ArrayList<String>> sentencesStems){
        double[] idf = new double[uniqueStems.size()];

        for(int i = 0; i < uniqueStems.size(); i++){
            int count = 0;
            for (ArrayList<String> sentenceStems : sentencesStems)
                for (String stem : sentenceStems)
                    if (stem.equals(uniqueStems.get(i))) {
                        count++;
                        break;
                    }
            idf[i] = Math.log(((double) sentencesStems.size()) / count);
        }

        return idf;
    }

    public static double[][] calcTfIdf(double[][] tf, double[] idf){
        double[][] tfidf = new double[tf.length][idf.length];
        for(int i = 0; i < tf.length; i++)
            for(int j = 0; j < tf[i].length; j++)
                tfidf[i][j] = tf[i][j] * idf[j];

        return tfidf;
    }

    public static ArrayList<ArrayList<String>> stemmingSentences(ArrayList<String> sentences) throws IOException {
        ArrayList<ArrayList<String>> list = new ArrayList<>();
        for(String sentence : sentences){
            list.add(textStemming(sentence));
        }
        return list;
    }

    public static ArrayList<String> textStemming(String text) throws IOException {
        ArrayList<String> result = new ArrayList<>();

        RussianAnalyzer russianAnalyzer = new RussianAnalyzer();
        Tokenizer source = new StandardTokenizer();
        source.setReader(new StringReader(text));
        TokenStream tokenStream = new StandardFilter(source);
        tokenStream = new StopFilter(tokenStream, russianAnalyzer.getStopwordSet());
        tokenStream = new LowerCaseFilter(tokenStream);

        CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        PorterStemmer porterStemmer  = new PorterStemmer();

        while (tokenStream.incrementToken()) {
            porterStemmer.setCurrent(termAttr.toString());
            porterStemmer.stem();
            result.add(porterStemmer.getCurrent());
        }

        tokenStream.end();
        tokenStream.close();

        return result;
    }


}
