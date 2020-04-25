package summarizer;

import java.util.ArrayList;

class Sentence {
    String originalSentence;
    Double sentenceWeight;
    ArrayList<Word> sentenceWords;


    Sentence(String originalSentence){
        this.originalSentence = originalSentence;
        sentenceWeight = 0.0;
        sentenceWords = new ArrayList<>();
    }
}
