package ru.nordmine.common;

public class SentenceInfo {

    private String sentence;
    private String form;

    public SentenceInfo(String sentence, String form) {
        this.sentence = sentence;
        this.form = form;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }
}
