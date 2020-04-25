package summarizer;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import tools.TextTools;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class StatisticSummarizer implements ISummarizer {

    private LuceneMorphology instance;

    public StatisticSummarizer() throws IOException {
        instance = new RussianLuceneMorphology();
    }

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

        ArrayList<Sentence> sentences = new ArrayList<>();
        for(String sentence : originalSentences){
            Sentence newSentence = new Sentence(sentence);
            newSentence.sentenceWords = GetSentenceLemmas(sentence);
            sentences.add(newSentence);
        }

        LinkedHashMap<String, Integer> countLemmas = GetCountLemmas(sentences);

        for(Sentence sentence : sentences)
            for (Word word : sentence.sentenceWords) {
                int max = 0;
                for (Morphology morphology : word.getLemmas())
                    if(countLemmas.get(morphology.getBase()) > max)
                        max = countLemmas.get(morphology.getBase());

                double weight = max;
                sentence.sentenceWeight += weight;
            }

        for(Sentence sentence : sentences)
            sentence.sentenceWeight /= sentence.sentenceWords.size();


        ArrayList<String> selectedSentences = new ArrayList<>();
        for(int i = 0; i < count; i++){
            double max = 0;
            String str = "";
            for(Sentence sentence : sentences)
                if(sentence.sentenceWeight >= max)
                    if(!selectedSentences.contains(sentence.originalSentence)){
                        max = sentence.sentenceWeight;
                        str = sentence.originalSentence;
                    }

            selectedSentences.add(str);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for(Sentence sentence : sentences)
            if(selectedSentences.contains(sentence.originalSentence)) stringBuilder.append(sentence.originalSentence).append(" ");

        return stringBuilder.toString();
    }

    private Word GetWordLemma(String word){
        word = word.toLowerCase();
        Word resultWord = new Word(word);
        if (instance.checkString(word)) {
            List<String> info = instance.getMorphInfo(word);
            for(String lemm : info){
                Morphology morphology = new Morphology();

                String[] mas = lemm.split("\\|");
                morphology.setBase(mas[0]);

                mas[1] = mas[1].replaceFirst(" ", "\n");
                mas = mas[1].split("\n");

                mas[1] = mas[1].replaceFirst(" ", "\n");
                mas = mas[1].split("\n");
                morphology.setPartOfSpeech(mas[0]);

                if(mas.length > 1)
                    morphology.setMorphology(mas[1]);

                resultWord.getLemmas().add(morphology);
            }
            return resultWord;
        }
        else return null;
    }

    private ArrayList<Word> GetSentenceLemmas(String sentence) throws IOException {
        Tokenizer source = new StandardTokenizer();
        source.setReader(new StringReader(sentence));
        TokenStream tokenStream = new StandardFilter(source);
        RussianAnalyzer russianAnalyzer = new RussianAnalyzer();
        tokenStream = new StopFilter(tokenStream, russianAnalyzer.getStopwordSet());

        CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();

        ArrayList<Word> list = new ArrayList<>();
        while (tokenStream.incrementToken()) {
            Word word = GetWordLemma(termAttr.toString());
            if(word != null) list.add(word);
        }

        tokenStream.end();
        tokenStream.close();
        return list;
    }



    private LinkedHashMap<String, Integer> GetCountLemmas(ArrayList<Sentence> sentences){
        LinkedHashMap<String, Integer> lemmas = new LinkedHashMap<>();
        for(Sentence sentence : sentences)
            for (Word word : sentence.sentenceWords)
                for (Morphology morphology : word.getLemmas())
                    lemmas.put(morphology.getBase(), lemmas.containsKey(morphology.getBase()) ? lemmas.get(morphology.getBase()) + 1 : 1);

        return lemmas;
    }
}
