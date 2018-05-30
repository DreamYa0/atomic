package com.atomic.tools.db.lettercase;

import java.util.Comparator;

public interface CaseComparison extends Comparator<String> {

    /**
     * Returns the name of the comparison.
     * @return The name of the comparison.
     */
    String getComparisonName();

    /**
     * Returns if {@code value1} is equal to {@code value2}.
     * @param value1 The first value to compare.
     * @param value2 The second value to compare.
     * @return If the two values are equal.
     */
    boolean isEqual(String value1, String value2);
}
