package com.atomic.tools.rollback.db.util;

import com.atomic.tools.rollback.db.Change;
import com.atomic.tools.rollback.db.ChangeType;
import com.atomic.tools.rollback.db.Row;

import java.util.Comparator;

public enum ChangeComparator implements Comparator<Change> {

    /**
     * 单例模式
     */
    INSTANCE;

    /** {@inheritDoc} */
    @Override
    public int compare(Change change1, Change change2) {
        ChangeType changeType1 = change1.getChangeType();
        ChangeType changeType2 = change2.getChangeType();
        int compare = changeType1.compareTo(changeType2);
        if (compare != 0) {
            return compare;
        }
        String dataName1 = change1.getDataName();
        String dataName2 = change2.getDataName();
        compare = dataName1.compareTo(dataName2);
        if (compare != 0) {
            return compare;
        }
        Row row1 = change1.getRowAtStartPoint();
        Row row2 = change2.getRowAtStartPoint();
        if (row1 == null) {
            row1 = change1.getRowAtEndPoint();
        }
        if (row2 == null) {
            row2 = change2.getRowAtEndPoint();
        }
        return RowComparator.INSTANCE.compare(row1, row2);
    }
}
