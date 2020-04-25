package summarizer;

import java.io.IOException;

public interface ISummarizer {
    /**
     * Генерирует реферат по заданному исходному тексту
     * @param text исходный текст
     * @param count количество предложений
     * @return реферат исходного текста
     */
    String getSummary(String text, int count) throws IOException;

    /**
     * Генерирует реферат по заданному исходному тексту
     * @param text исходный текст
     * @param compression степень сжатия текста
     * @return реферат исходного текста
     */
    String getSummary(String text, double compression) throws IOException;

    /**
     * Генерирует реферат по заданному исходному тексту
     * @param text исходный текст
     * @param compression степень сжатия текста
     * @param count максимальное количество предложений
     * @return реферат исходного текста
     */
    String getSummary(String text, double compression, int count) throws IOException;
}
