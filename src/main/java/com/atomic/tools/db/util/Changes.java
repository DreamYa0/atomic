package com.atomic.tools.db.util;

import com.atomic.tools.db.Change;
import com.atomic.tools.db.Row;
import com.atomic.tools.db.Value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Changes {

    private Changes() {
        // Empty
    }

    /**
     * Returns the indexes of the modified columns.
     * @param change The change.
     * @return The indexes.
     */
    public static Integer[] getIndexesOfModifiedColumns(Change change) {
        List<Integer> indexesList = new ArrayList<>();
        Row rowAtStartPoint = change.getRowAtStartPoint();
        Row rowAtEndPoint = change.getRowAtEndPoint();
        if (rowAtStartPoint != null && rowAtEndPoint != null) {
            List<Value> valuesListAtStartPoint = rowAtStartPoint.getValuesList();
            List<Value> valuesListAtEndPoint = rowAtEndPoint.getValuesList();
            Iterator<Value> iteratorAtEndPoint = valuesListAtEndPoint.iterator();
            int index = 0;
            for (Value valueAtStartPoint : valuesListAtStartPoint) {
                Value valueAtEndPoint = iteratorAtEndPoint.next();
                Object objectAtStartPoint = valueAtStartPoint.getValue();
                Object objectAtEndPoint = valueAtEndPoint.getValue();

                if ((objectAtStartPoint == null && objectAtEndPoint != null) ||
                        (objectAtStartPoint != null && !objectAtStartPoint.equals(objectAtEndPoint))) {

                    indexesList.add(index);
                }
                index++;
            }
        } else if (rowAtStartPoint != null) {
            List<Value> valuesListAtStartPoint = rowAtStartPoint.getValuesList();
            int index = 0;
            for (Value valueAtStartPoint : valuesListAtStartPoint) {
                if (valueAtStartPoint.getValue() != null) {
                    indexesList.add(index);
                }
                index++;
            }
        } else {
            List<Value> valuesListAtEndPoint = rowAtEndPoint.getValuesList();
            int index = 0;
            for (Value valueAtEndPoint : valuesListAtEndPoint) {
                if (valueAtEndPoint.getValue() != null) {
                    indexesList.add(index);
                }
                index++;
            }
        }

        return indexesList.toArray(new Integer[indexesList.size()]);
    }

}
