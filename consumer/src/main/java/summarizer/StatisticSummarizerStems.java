package summarizer;

import tools.TextTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class StatisticSummarizerStems implements ISummarizer {

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

    private String generateSummary(ArrayList<String> originalSentences, int count) throws IOException{
        ArrayList<ArrayList<String>> stems = TextTools.stemmingSentences(originalSentences);

        LinkedHashMap<String, Integer> countStems = getCountStems(stems);
        double[] weights = new double[originalSentences.size()];

        int index = 0;
        for(ArrayList<String> sentenceStems : stems){
            for(String stem : sentenceStems)
                weights[index] += countStems.get(stem);

            weights[index] /= sentenceStems.size();
            index++;
        }


        ArrayList<String> selectedSentences = new ArrayList<>();
        for(int i = 0; i < count; i++){
            double max = 0;
            String str = "";
            for(int j = 0; j < weights.length; j++)
                if(weights[j] >= max)
                    if(!selectedSentences.contains(originalSentences.get(j))){
                        max = weights[j];
                        str = originalSentences.get(j);
                    }

            selectedSentences.add(str);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for(String sentence : originalSentences)
            if(selectedSentences.contains(sentence)) stringBuilder.append(sentence).append(" ");

        return stringBuilder.toString();
    }



    private LinkedHashMap<String, Integer> getCountStems(ArrayList<ArrayList<String>> stems){
        LinkedHashMap<String, Integer> countStems = new LinkedHashMap<>();
        for(ArrayList<String> sentenceStems : stems)
            for (String stem : sentenceStems)
                countStems.put(stem, countStems.containsKey(stem) ? countStems.get(stem) + 1 : 1);

        return countStems;
    }
}
