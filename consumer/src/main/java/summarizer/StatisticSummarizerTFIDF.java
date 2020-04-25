package summarizer;

import tools.TextTools;

import java.io.IOException;
import java.util.ArrayList;

public class StatisticSummarizerTFIDF implements ISummarizer {

    @Override
    public String getSummary(String text, int count) throws IOException {
        ArrayList<String> sentences = TextTools.splitOnSentences(text);
        return generateSummary(text, sentences, count);
    }

    @Override
    public String getSummary(String text, double compression) throws IOException {
        ArrayList<String> sentences = TextTools.splitOnSentences(text);
        int count = (int)((sentences.size() + 1) * compression);
        return generateSummary(text, sentences, count);
    }

    @Override
    public String getSummary(String text, double compression, int count) throws IOException {
        ArrayList<String> sentences = TextTools.splitOnSentences(text);
        int countPercent = (int)((sentences.size() + 1) * compression);
        return generateSummary(text, sentences, countPercent < count ? countPercent : count);
    }

    private String generateSummary(String text, ArrayList<String> originalSentences, int count) throws IOException{
        ArrayList<ArrayList<String>> sentencesStems = TextTools.stemmingSentences(originalSentences);
        ArrayList<String> stems = TextTools.textStemming(text.replace("\r\n", " "));

        ArrayList<String> uniqueStems = TextTools.getUniqueStems(stems);
        double[][] tf = TextTools.calcTf(uniqueStems, sentencesStems);
        double[] idf = TextTools.calcIdf(uniqueStems, sentencesStems);
        double[][] tfidf = TextTools.calcTfIdf(tf, idf);

        double[] sentenceWeight = new double[tfidf.length];
        for (int i = 0; i < tfidf.length; i++)
            for (int j = 0; j < tfidf[i].length; j++)
                if (tfidf[i][j] > 0.0)
                    sentenceWeight[i] += tfidf[i][j];



        ArrayList<String> selectedSentences = new ArrayList<>();
        for(int i = 0; i < count; i++){
            double max = 0;
            String str = "";

            for(int j = 0; j < sentenceWeight.length; j++)
                if(sentenceWeight[j] >= max)
                    if(!selectedSentences.contains(originalSentences.get(j))){
                        max = sentenceWeight[j];
                        str = originalSentences.get(j);
                    }

            selectedSentences.add(str);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for(String sentence : originalSentences)
            if(selectedSentences.contains(sentence)) stringBuilder.append(sentence).append(" ");

        return stringBuilder.toString();
    }
}
