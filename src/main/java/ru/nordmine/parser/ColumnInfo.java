package ru.nordmine.parser;

public class ColumnInfo {

    private String headerContains;
    private String saveAs;
    private boolean isDigital = false;
    private String xpath = null;
    private boolean enableCreate = true;

    public ColumnInfo(String headerContains, String saveAs) {
        this.headerContains = headerContains;
        this.saveAs = saveAs;
    }

    public ColumnInfo(String headerContains, String saveAs, String xpath) {
        this.headerContains = headerContains;
        this.saveAs = saveAs;
        this.xpath = xpath;
    }

    public ColumnInfo(String headerContains, String saveAs, String xpath, boolean isDigital) {
        this.headerContains = headerContains;
        this.saveAs = saveAs;
        this.isDigital = isDigital;
        this.xpath = xpath;
    }

    public String getHeaderContains() {
        return headerContains;
    }

    public void setHeaderContains(String headerContains) {
        this.headerContains = headerContains;
    }

    public String getSaveAs() {
        return saveAs;
    }

    public void setSaveAs(String saveAs) {
        this.saveAs = saveAs;
    }

    public boolean isDigital() {
        return isDigital;
    }

    public void setDigital(boolean isNumber) {
        this.isDigital = isNumber;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public boolean isEnableCreate() {
        return enableCreate;
    }

    public void setEnableCreate(boolean enableCreate) {
        this.enableCreate = enableCreate;
    }
}
