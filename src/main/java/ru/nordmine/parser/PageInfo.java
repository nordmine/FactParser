package ru.nordmine.parser;

import java.util.ArrayList;
import java.util.List;

public class PageInfo {

    private String address;
    private List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
    private String primaryField;
    private String tableXPath = "//TABLE[@class='wikitable']";
    private String headerXPath = "TBODY/TR/TH";
    private String rowXPath = "TBODY/TR";

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<ColumnInfo> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnInfo> columns) {
        this.columns = columns;
    }

    public String getPrimaryField() {
        return primaryField;
    }

    public void setPrimaryField(String primaryField) {
        this.primaryField = primaryField;
    }

    public String getTableXPath() {
        return tableXPath;
    }

    public void setTableXPath(String tableXPath) {
        this.tableXPath = tableXPath;
    }

    public String getHeaderXPath() {
        return headerXPath;
    }

    public void setHeaderXPath(String headerXPath) {
        this.headerXPath = headerXPath;
    }

    public String getRowXPath() {
        return rowXPath;
    }

    public void setRowXPath(String rowXPath) {
        this.rowXPath = rowXPath;
    }
}
