package summarizer;

public class Morphology {
    //Лемма
    private String base;
    //Морфология
    private String morphology;
    //Часть речи
    private String partOfSpeech;


    String getBase() {
        return base;
    }

    void setBase(String base) {
        this.base = base;
    }

    String getPartOfSpeech() {
        return partOfSpeech;
    }

    void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    String getMorphology() {
        return morphology;
    }

    void setMorphology(String morphology) {
        this.morphology = morphology;
    }
}
