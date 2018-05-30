package com.atomic.tools.db;

import com.atomic.tools.db.lettercase.LetterCase;
import com.atomic.tools.db.lettercase.WithLetterCase;

/**
 * 一种表示连接到数据库和字母用例的信息的源
 */
public class SourceWithLetterCase extends Source implements WithLetterCase {

    private final LetterCase tableLetterCase;

    private final LetterCase columnLetterCase;

    private final LetterCase primaryKeyLetterCase;

    public SourceWithLetterCase(String url, String user, String password,
                                LetterCase tableLetterCase, LetterCase columnLetterCase, LetterCase primaryKeyLetterCase) {

        super(url, user, password);
        this.tableLetterCase = tableLetterCase;
        this.columnLetterCase = columnLetterCase;
        this.primaryKeyLetterCase = primaryKeyLetterCase;
    }

    @Override
    public LetterCase getColumnLetterCase() {
        return columnLetterCase;
    }

    @Override
    public LetterCase getPrimaryKeyLetterCase() {
        return primaryKeyLetterCase;
    }

    @Override
    public LetterCase getTableLetterCase() {
        return tableLetterCase;
    }
}
