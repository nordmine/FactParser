package ru.nordmine.text.generator;

public class SentencePool {

    private String[] sentences;
    private int sentenceIndex = 0;

    public SentencePool(String[] sentences) {
        this.sentences = sentences;
    }

    public String next() {
        String sentence = sentences[sentenceIndex % sentences.length];
        sentenceIndex++;
        return sentence;
    }

    public String nextWithValue(String value) {
        return next().replace("value", value);
    }
}
