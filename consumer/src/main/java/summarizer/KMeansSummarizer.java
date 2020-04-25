package summarizer;

import smile.clustering.KMeans;
import tools.TextTools;

import java.io.IOException;
import java.util.ArrayList;

public class KMeansSummarizer implements ISummarizer {

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
        return generateSummary(text, sentences, countPercent);
    }

    private String generateSummary(String text, ArrayList<String> sentences, int count) throws IOException{
        ArrayList<ArrayList<String>> sentencesStems = TextTools.stemmingSentences(sentences);
        text = text.replace("\r\n", " ");
        ArrayList<String> stems = TextTools.textStemming(text);

        ArrayList<String> uniqueStems = TextTools.getUniqueStems(stems);
        double[][] tf = TextTools.calcTf(uniqueStems, sentencesStems);
        double[] idf = TextTools.calcIdf(uniqueStems, sentencesStems);
        double[][] tfidf = TextTools.calcTfIdf(tf, idf);


        KMeans kMeans = new KMeans(tfidf, count, 20);
        double[][] centroids = kMeans.centroids();
        int[] y = kMeans.getClusterLabel();

        double[][] tfidfDifs = new double[centroids.length][tfidf.length];

        for(int i = 0; i < tfidfDifs.length; i++)
            for(int j = 0; j < tfidfDifs[i].length; j++){
                double sum = 0;
                for(int k = 0; k < tfidfDifs[i].length; k++)
                    sum += Math.pow(tfidf[j][k] - centroids[i][k], 2);

                tfidfDifs[i][j] = Math.sqrt(sum);
            }



        ArrayList<Integer> indexes = new ArrayList<>();
        for(int i = 0; i < tfidfDifs.length; i++){
            double min = Double.MAX_VALUE;
            int index = 0;
            for(int j = 0; j < tfidfDifs[i].length; j++)
                if(min > tfidfDifs[i][j] && !indexes.contains(j) && y[j] == i){
                    min = tfidfDifs[i][j];
                    index = j;
                }

            indexes.add(index);
        }

        StringBuilder sb = new StringBuilder();
        for (Integer index : indexes)
            sb.append(sentences.get(index)).append(" ");

        return sb.toString();
    }
}
