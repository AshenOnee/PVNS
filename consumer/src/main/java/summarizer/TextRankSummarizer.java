package summarizer;

import tools.TextTools;

import java.io.IOException;
import java.util.ArrayList;

public class TextRankSummarizer implements ISummarizer {

    @Override
    public String getSummary(String text, int count) throws IOException {
        ArrayList<String> sentences = TextTools.splitOnSentences(text);

        return generateSummary(sentences, count);
    }

    @Override
    public String getSummary(String text, double compression) throws IOException {
        ArrayList<String> sentences = TextTools.splitOnSentences(text);
        int count = (int)((sentences.size() + 1) * compression);

        return generateSummary(sentences, count);
    }

    @Override
    public String getSummary(String text, double compression, int count) throws IOException {
        ArrayList<String> sentences = TextTools.splitOnSentences(text);
        int countPercent = (int)((sentences.size() + 1) * compression);

        return generateSummary(sentences, countPercent < count ? countPercent : count);
    }


    private String generateSummary(ArrayList<String> sentences, int count) throws IOException{
        double[] weightVertex = calcVertexWeights(calcWeightEdges(TextTools.stemmingSentences(sentences)));
        ArrayList<String> selectedSentences = new ArrayList<>();
        for(int i = 0; i < count; i++){
            double max = 0;
            int index = 0;
            for(int j = 0; j < weightVertex.length; j++)
                if(weightVertex[j] >= max && !selectedSentences.contains(sentences.get(j))){
                    index = j;
                    max = weightVertex[j];
                }
            selectedSentences.add(sentences.get(index));
        }

        StringBuilder result = new StringBuilder();
        for(String sentence : sentences)
            if(selectedSentences.contains(sentence))
                result.append(sentence).append(" ");
        return result.toString();
    }

    private double[] calcVertexWeights(double[][] weightEdges){
        double d = 1;
        double dif = 0.001;
        double[] sumVertexEdgesWeights = calcSumVertexEdgesWeight(weightEdges);

        double[] vertexWeights = new double[weightEdges.length];
        for(int i = 0; i < vertexWeights.length; i++)
            vertexWeights[i] = 1.0;

        int count = 0;

        while(true) {
            boolean end = true;
            for (int i = 0; i < weightEdges.length; i++) {
                double sumI = 0.0;
                for (int j = 0; j < weightEdges[i].length; j++) {
                    sumI += (weightEdges[i][j] / (sumVertexEdgesWeights[j] + 1)) * vertexWeights[j];
                    count++;
                }

                double weight = (1.0 - d) + d * sumI;
                if (Math.abs(vertexWeights[i] - weight) > dif) end = false;

                vertexWeights[i] = weight;
            }

            if(end) break;
        }
        return vertexWeights;
    }

    private double[] calcSumVertexEdgesWeight(double[][] weightEdges){
        double[] sum = new double[weightEdges.length];
        for(int i = 0; i < weightEdges.length; i++)
            for(int j = 0; j < weightEdges[i].length; j++)
                sum[i] += weightEdges[i][j];
        return sum;
    }

    private double[][] calcWeightEdges(ArrayList<ArrayList<String>> sentencesStems){
        double[][] weightEdges = new double[sentencesStems.size()][sentencesStems.size()];
        for(int i = 0; i < sentencesStems.size(); i++)
            for(int j = 0; j < sentencesStems.size(); j++)
                weightEdges[i][j] = calcSimilarity(sentencesStems.get(i), sentencesStems.get(j));
        return weightEdges;
    }

    private double calcSimilarity(ArrayList<String> sentence1, ArrayList<String> sentence2){
        ArrayList<String> uniqueStems1 = TextTools.getUniqueStems(sentence1);
        ArrayList<String> uniqueStems2 = TextTools.getUniqueStems(sentence2);

        int countSimilar = 0;
        for(String stem1 : uniqueStems1)
            if(uniqueStems2.contains(stem1))
                countSimilar++;

        return ((double)countSimilar)/(uniqueStems1.size() + uniqueStems2.size());
    }
}