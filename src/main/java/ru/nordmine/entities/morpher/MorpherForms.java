package ru.nordmine.entities.morpher;

import com.google.common.base.Objects;

import javax.persistence.*;

@Entity
@Table(name = "word_forms")
public class MorpherForms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String singleImen;
    private String singleRod;
    private String singleDat;
    private String singleVin;
    private String singleTvor;
    private String singlePred;

    private String multipleImen;
    private String multipleRod;
    private String multipleDat;
    private String multipleVin;
    private String multipleTvor;
    private String multiplePred;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSingleImen() {
        return singleImen;
    }

    public void setSingleImen(String singleImen) {
        this.singleImen = singleImen;
    }

    public String getSingleRod() {
        return singleRod;
    }

    public void setSingleRod(String singleRod) {
        this.singleRod = singleRod;
    }

    public String getSingleDat() {
        return singleDat;
    }

    public void setSingleDat(String singleDat) {
        this.singleDat = singleDat;
    }

    public String getSingleVin() {
        return singleVin;
    }

    public void setSingleVin(String singleVin) {
        this.singleVin = singleVin;
    }

    public String getSingleTvor() {
        return singleTvor;
    }

    public void setSingleTvor(String singleTvor) {
        this.singleTvor = singleTvor;
    }

    public String getSinglePred() {
        return singlePred;
    }

    public void setSinglePred(String singlePred) {
        this.singlePred = singlePred;
    }

    public String getMultipleImen() {
        return multipleImen;
    }

    public void setMultipleImen(String multipleImen) {
        this.multipleImen = multipleImen;
    }

    public String getMultipleRod() {
        return multipleRod;
    }

    public void setMultipleRod(String multipleRod) {
        this.multipleRod = multipleRod;
    }

    public String getMultipleDat() {
        return multipleDat;
    }

    public void setMultipleDat(String multipleDat) {
        this.multipleDat = multipleDat;
    }

    public String getMultipleVin() {
        return multipleVin;
    }

    public void setMultipleVin(String multipleVin) {
        this.multipleVin = multipleVin;
    }

    public String getMultipleTvor() {
        return multipleTvor;
    }

    public void setMultipleTvor(String multipleTvor) {
        this.multipleTvor = multipleTvor;
    }

    public String getMultiplePred() {
        return multiplePred;
    }

    public void setMultiplePred(String multiplePred) {
        this.multiplePred = multiplePred;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("singleImen", singleImen)
                .add("singleRod", singleRod)
                .add("singleDat", singleDat)
                .add("singleVin", singleVin)
                .add("singleTvor", singleTvor)
                .add("singlePred", singlePred)
                .add("multipleImen", multipleImen)
                .add("multipleRod", multipleRod)
                .add("multipleDat", multipleDat)
                .add("multipleVin", multipleVin)
                .add("multipleTvor", multipleTvor)
                .add("multiplePred", multiplePred)
                .toString();
    }

    public String getByCode(String code) {
        String result = null;
        if (code.equals("Д")) {
            result = this.singleDat;
        }
        if (code.equals("П")) {
            result = this.singlePred;
        }
        if (code.equals("Р")) {
            result = this.singleRod;
        }
        if (code.equals("В")) {
            result = this.singleVin;
        }
        if (code.equals("МВ")) {
            result = this.multipleVin;
        }
        return result;
    }
}
