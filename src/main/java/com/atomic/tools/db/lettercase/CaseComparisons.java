package com.atomic.tools.db.lettercase;

public enum CaseComparisons implements CaseComparison {

    /**
     * Comparison on {@link String} which ignore the case.
     */
    IGNORE {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getComparisonName() {
            return "IGNORE - Ignore the case";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(String value1, String value2) {
            if (value1 == null) {
                if (value2 == null) {
                    return 0;
                } else {
                    return -1;
                }
            } else if (value2 == null) {
                return 1;
            }
            return value1.compareToIgnoreCase(value2);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEqual(String value1, String value2) {
            if (value1 == null) {
                return value2 == null;
            }
            return value1.equalsIgnoreCase(value2);
        }
    },
    /**
     * Comparison on {@link String} which strictly consider the case.
     */
    STRICT {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getComparisonName() {
            return "STRICT - Strictly compare the case";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(String value1, String value2) {
            if (value1 == null) {
                if (value2 == null) {
                    return 0;
                } else {
                    return -1;
                }
            } else if (value2 == null) {
                return 1;
            }
            return value1.compareTo(value2);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEqual(String value1, String value2) {
            if (value1 == null) {
                return value2 == null;
            }
            return value1.equals(value2);
        }
    };
}
