package ru.nordmine.services.old;

import ru.nordmine.parser.ColumnInfo;

import java.util.List;

public interface InfoboxParserService {

    void parseInfobox(String primaryField, List<ColumnInfo> allowedRows);
}
