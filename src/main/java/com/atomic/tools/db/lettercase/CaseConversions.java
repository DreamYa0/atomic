package com.atomic.tools.db.lettercase;

public enum CaseConversions implements CaseConversion {

    /**
     * Lower conversion of the case of a {@link String}.
     */
    LOWER {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getConversionName() {
            return "LOWER - Lower case conversion";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String convert(String value) {
            if (value == null) {
                return null;
            }
            return value.toLowerCase();
        }
    },
    /**
     * Upper conversion of the case of a {@link String}.
     */
    UPPER {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getConversionName() {
            return "UPPER - Upper case conversion";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String convert(String value) {
            if (value == null) {
                return null;
            }
            return value.toUpperCase();
        }
    },
    /**
     * No conversion of the case of a {@link String}.
     */
    NO {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getConversionName() {
            return "NO - No case conversion";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String convert(String value) {
            return value;
        }
    };
}
