package summarizer;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import tools.TextTools;

import java.io.IOException;
import java.util.ArrayList;

public class LSASummarizer implements ISummarizer {
    private ArrayList<String> sentences;

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
        text = text.replace("\r\n", " ");
        text = text.replace("\n", " ");
        ArrayList<String> stems = TextTools.textStemming(text);
        ArrayList<ArrayList<String>> sentencesStems = TextTools.stemmingSentences(sentences);

        ArrayList<String> uniqueStems = TextTools.getUniqueStems(stems);
        double[][] A = generateMatrix(uniqueStems, sentencesStems);

        SingularValueDecomposition singular = new SingularValueDecomposition(new Matrix(A));

        double[] s = singular.getSingularValues();
        Matrix V = singular.getV();

        double[] weights = new double[V.getRowDimension()];

        for(int k = 0; k < V.getColumnDimension(); k++){
            for(int i = 0; i < s.length; i++)
                weights[k] += Math.pow(V.get(i, k), 2) * Math.pow(s[i], 2);

            weights[k] = Math.sqrt(weights[k]);
        }

        int[] indexes1 = new int[weights.length];

        for(int i = 0; i < indexes1.length; i++)
            indexes1[i] = i;

        for (int i = 0; i < weights.length; i++){
            double max = weights[i];
            int index = i;
            for (int j = i + 1; j < weights.length; j++)
                if(max < weights[j]){
                    max = weights[j];
                    index = j;
                }

            double tmp = weights[i];
            weights[i] = weights[index];
            weights[index] = tmp;

            int tmp2 = indexes1[i];
            indexes1[i] = indexes1[index];
            indexes1[index] = tmp2;
        }

        int[] indexes = new int[count];
        System.arraycopy(indexes1, 0, indexes, 0, indexes.length);

        for (int i = 0; i < indexes.length; i++){
            double min = indexes[i];
            int index = i;
            for (int j = i + 1; j < indexes.length; j++)
                if(indexes[j] < min){
                    min = indexes[j];
                    index = j;
                }

            int tmp = indexes[i];
            indexes[i] = indexes[index];
            indexes[index] = tmp;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int index : indexes) stringBuilder.append(sentences.get(index)).append(" ");

        return stringBuilder.toString();
    }


    private double[][] generateMatrix(ArrayList<String> uniqueStems, ArrayList<ArrayList<String>> sentencesStems){
        double[][] A = new double[uniqueStems.size()][sentencesStems.size()];
        for (int i = 0; i < sentencesStems.size(); i++)
            for (int j = 0; j < sentencesStems.get(i).size(); j++)
                for (int k = 0; k < uniqueStems.size(); k++)
                    if(sentencesStems.get(i).get(j).equals(uniqueStems.get(k))){
                        A[k][i]++;
                        break;
                    }

        return A;
    }
}
