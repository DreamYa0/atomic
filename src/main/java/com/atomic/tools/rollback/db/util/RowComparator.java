package com.atomic.tools.rollback.db.util;


import com.atomic.tools.rollback.db.Row;
import com.atomic.tools.rollback.db.Value;

import java.util.Comparator;
import java.util.List;

public enum RowComparator implements Comparator<Row> {

    INSTANCE;

    private static int compare(Value[] values1, Value[] values2) {
        if (values1.length == values2.length) {
            for (int index = 0; index < values1.length; index++) {
                Value value1 = values1[index];
                Value value2 = values2[index];
                Object object1 = value1.getValue();
                Object object2 = value2.getValue();
                if (object1 == null && object2 != null) {
                    return 1;
                }
                if (object1 != null && object2 == null) {
                    return -1;
                }
                if (object1 instanceof Comparable && object2 instanceof Comparable) {
                    @SuppressWarnings("unchecked")
                    Comparable<Object> comparable1 = Comparable.class.cast(object1);
                    int compare = comparable1.compareTo(object2);
                    if (compare != 0) {
                        return compare;
                    }
                }
            }
        }
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int compare(Row row1, Row row2) {
        Value[] pksValues1 = row1.getPksValues();
        Value[] pksValues2 = row2.getPksValues();
        int compare = compare(pksValues1, pksValues2);
        if (compare != 0) {
            return compare;
        }
        List<Value> valuesList1 = row1.getValuesList();
        List<Value> valuesList2 = row2.getValuesList();
        Value[] values1 = valuesList1.toArray(new Value[valuesList1.size()]);
        Value[] values2 = valuesList2.toArray(new Value[valuesList2.size()]);
        return compare(values1, values2);
    }
}
