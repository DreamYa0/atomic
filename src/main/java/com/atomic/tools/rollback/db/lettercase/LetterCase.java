package com.atomic.tools.rollback.db.lettercase;

import java.util.HashMap;
import java.util.Map;

public class LetterCase implements CaseConversion, CaseComparison {

    /**
     * The cache containing the different possible letter case after the first instantiation.
     */
    private static Map<CaseConversion, Map<CaseComparison, LetterCase>> CACHE;
    /**
     * The default letter case for table.
     */
    public static LetterCase TABLE_DEFAULT = getLetterCase(CaseConversions.NO, CaseComparisons.IGNORE);
    /**
     * The default letter case for table.
     */
    public static LetterCase COLUMN_DEFAULT = getLetterCase(CaseConversions.UPPER, CaseComparisons.IGNORE);
    /**
     * The default letter case for table.
     */
    public static LetterCase PRIMARY_KEY_DEFAULT = getLetterCase(CaseConversions.UPPER, CaseComparisons.IGNORE);
    /**
     * The conversion of the case of a {@link String}.
     */
    private CaseConversion conversion;
    /**
     * The comparison on {@link String} which consider the case.
     */
    private CaseComparison comparison;

    /**
     * Constructor.
     * @param conversion The conversion of the case of a {@link String}.
     * @param comparison The comparison on {@link String} which consider the case.
     */
    private LetterCase(CaseConversion conversion, CaseComparison comparison) {
        this.conversion = conversion;
        this.comparison = comparison;
    }

    /**
     * Returns a instance of a letter case.
     * @param conversion The conversion of the case of a {@link String}.
     * @param comparison The comparison on {@link String} which consider the case.
     * @return An instance of a letter case.
     */
    public static synchronized LetterCase getLetterCase(CaseConversion conversion, CaseComparison comparison) {
        if (conversion == null) {
            throw new NullPointerException("The case conversion must be not null");
        }
        if (comparison == null) {
            throw new NullPointerException("The case comparison must be not null");
        }

        if (CACHE == null) {
            CACHE = new HashMap<>();
        }
        Map<CaseComparison, LetterCase> map = CACHE.get(conversion);
        if (map == null) {
            map = new HashMap<>();
            CACHE.put(conversion, map);
            LetterCase letterCase = new LetterCase(conversion, comparison);
            map.put(comparison, letterCase);
            return letterCase;
        }
        LetterCase letterCase = map.get(comparison);
        if (letterCase == null) {
            letterCase = new LetterCase(conversion, comparison);
            map.put(comparison, letterCase);
        }
        return letterCase;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComparisonName() {
        return comparison.getComparisonName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEqual(String value1, String value2) {
        return comparison.isEqual(value1, value2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(String value1, String value2) {
        return comparison.compare(value1, value2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConversionName() {
        return conversion.getConversionName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String convert(String value) {
        return conversion.convert(value);
    }
}
