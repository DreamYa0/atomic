package com.atomic.tools.db.util;

import com.atomic.exception.AssertJDBException;
import com.atomic.tools.db.DateTimeValue;
import com.atomic.tools.db.DateValue;
import com.atomic.tools.db.TimeValue;
import com.atomic.tools.db.Value;
import com.atomic.tools.db.ValueType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.UUID;

public class Values {

    private Values() {
        // Empty
    }

    public static boolean areEqual(Value value, Object expected) {
        ValueType valueType = value.getValueType();
        if (valueType == ValueType.BOOLEAN) {
            if (expected instanceof Boolean) {
                return areEqual(value, (Boolean) expected);
            }
        } else if (valueType == ValueType.NUMBER) {
            if (expected instanceof Number) {
                return areEqual(value, (Number) expected);
            } else if (expected instanceof String) {
                return areEqual(value, (String) expected);
            }
        } else if (valueType == ValueType.BYTES) {
            if (expected instanceof byte[]) {
                return areEqual(value, (byte[]) expected);
            }
        } else if (valueType == ValueType.TEXT) {
            if (expected instanceof String) {
                return areEqual(value, (String) expected);
            } else if (expected instanceof Character) {
                return areEqual(value, (Character) expected);
            }
        } else if (valueType == ValueType.UUID) {
            if (expected instanceof UUID) {
                return areEqual(value, (UUID) expected);
            } else if (expected instanceof String) {
                return areEqual(value, (String) expected);
            }
        } else if (valueType == ValueType.DATE) {
            if (expected instanceof DateValue) {
                return areEqual(value, (DateValue) expected);
            } else if (expected instanceof String) {
                return areEqual(value, (String) expected);
            } else if (expected instanceof Date) {
                return areEqual(value, DateValue.from((Date) expected));
            }
        } else if (valueType == ValueType.TIME) {
            if (expected instanceof TimeValue) {
                return areEqual(value, (TimeValue) expected);
            } else if (expected instanceof String) {
                return areEqual(value, (String) expected);
            } else if (expected instanceof Time) {
                return areEqual(value, TimeValue.from((Time) expected));
            }
        } else if (valueType == ValueType.DATE_TIME) {
            if (expected instanceof DateTimeValue) {
                return areEqual(value, (DateTimeValue) expected);
            } else if (expected instanceof DateValue) {
                return areEqual(value, (DateValue) expected);
            } else if (expected instanceof String) {
                return areEqual(value, (String) expected);
            } else if (expected instanceof Timestamp) {
                return areEqual(value, DateTimeValue.from((Timestamp) expected));
            }
        } else {
            Object object = value.getValue();
            if (expected == null && object == null) {
                return true;
            }
            if (object != null) {
                return object.equals(expected);
            }
        }

        return false;
    }

    public static boolean areEqual(Value value, Boolean expected) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }
        return expected.equals(object);
    }

    public static boolean areEqual(Value value, Number expected) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }
        if (expected instanceof BigInteger) {
            BigInteger bi;

            bi = getBigInteger(expected, object);

            if (bi.compareTo((BigInteger) expected) == 0) {
                return true;
            }
        } else if (expected instanceof BigDecimal) {
            BigDecimal bd;

            bd = getBigDecimal(expected, object);

            if (bd.compareTo((BigDecimal) expected) == 0) {
                return true;
            }
        } else {
            Long actualValue = null;

            if (object instanceof Float) {
                if (((Float) object) == expected.floatValue()) {
                    return true;
                }
            } else if (object instanceof Double) {
                if (((Double) object) == expected.doubleValue()) {
                    return true;
                }
            } else if (object instanceof BigInteger) {
                BigInteger bi = new BigInteger("" + expected);
                if (((BigInteger) object).compareTo(bi) == 0) {
                    return true;
                }
            } else if (object instanceof BigDecimal) {
                BigDecimal bd = new BigDecimal("" + expected);
                if (((BigDecimal) object).compareTo(bd) == 0) {
                    return true;
                }
            } else if (object instanceof Byte) {
                actualValue = ((Byte) object).longValue();
            } else if (object instanceof Short) {
                actualValue = ((Short) object).longValue();
            } else if (object instanceof Integer) {
                actualValue = ((Integer) object).longValue();
            } else if (object instanceof Long) {
                actualValue = (Long) object;
            }

            if (actualValue != null) {
                if (expected instanceof Float) {
                    if (actualValue == expected.floatValue()) {
                        return true;
                    }
                } else if (expected instanceof Double) {
                    if (actualValue == expected.doubleValue()) {
                        return true;
                    }
                } else {
                    if (actualValue == expected.longValue()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static BigInteger getBigInteger(Number expected, Object object) {
        BigInteger bi;
        if (object instanceof BigInteger) {
            bi = (BigInteger) object;
        } else {
            try {
                bi = new BigInteger("" + object);
            } catch (NumberFormatException e) {
                throw new AssertJDBException("Expected <%s> can not be compared to a BigInteger (<%s>)", expected, object);
            }
        }
        return bi;
    }

    public static boolean areEqual(Value value, byte[] expected) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }

        if (object instanceof byte[]) {
            byte[] bytes = (byte[]) object;
            if (bytes.length != expected.length) {
                return false;
            }
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] != expected[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean areEqual(Date date, String expected) {
        try {
            DateTimeValue dateTimeValue = DateTimeValue.of(DateValue.from(date));
            DateTimeValue expectedDateTimeValue = DateTimeValue.parse(expected);
            if (dateTimeValue.equals(expectedDateTimeValue)) {
                return true;
            }
        } catch (ParseException e) {
            throw new AssertJDBException("Expected <%s> is not correct to compare to <%s>", expected, date);
        }
        return false;
    }

    private static boolean areEqual(Time time, String expected) {
        try {
            TimeValue timeValue = TimeValue.from(time);
            TimeValue expectedTimeValue = TimeValue.parse(expected);
            if (timeValue.equals(expectedTimeValue)) {
                return true;
            }
        } catch (ParseException e) {
            throw new AssertJDBException("Expected <%s> is not correct to compare to <%s>", expected, time);
        }
        return false;
    }

    private static boolean areEqual(Timestamp timestamp, String expected) {
        try {
            DateTimeValue dateTimeValue = DateTimeValue.from(timestamp);
            DateTimeValue expectedDateTimeValue = DateTimeValue.parse(expected);
            if (dateTimeValue.equals(expectedDateTimeValue)) {
                return true;
            }
        } catch (ParseException e) {
            throw new AssertJDBException("Expected <%s> is not correct to compare to <%s>", expected, timestamp);
        }
        return false;
    }

    private static boolean areEqual(Number number, String expected) {
        try {
            if (number instanceof Float) {
                if (number.floatValue() == Float.parseFloat(expected)) {
                    return true;
                }
            } else if (number instanceof Double) {
                if (number.doubleValue() == Double.parseDouble(expected)) {
                    return true;
                }
            } else if (number instanceof BigInteger) {
                BigInteger bi = new BigInteger("" + expected);
                if (((BigInteger) number).compareTo(bi) == 0) {
                    return true;
                }
            } else if (number instanceof BigDecimal) {
                BigDecimal bd = new BigDecimal("" + expected);
                if (((BigDecimal) number).compareTo(bd) == 0) {
                    return true;
                }
            } else {
                Long actual = null;

                if (number instanceof Byte || number instanceof Short || number instanceof Integer) {
                    actual = number.longValue();
                } else if (number instanceof Long) {
                    actual = (Long) number;
                }

                if (actual != null && actual == Long.parseLong(expected)) {
                    return true;
                }
            }
        } catch (NumberFormatException e) {
            throw new AssertJDBException("Expected <%s> is not correct to compare to <%s>", expected, number);
        }
        return false;
    }

    public static boolean areEqual(Value value, String expected) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }

        if (object instanceof Number) {
            return areEqual((Number) object, expected);
        } else if (object instanceof Date) {
            return areEqual((Date) object, expected);
        } else if (object instanceof Time) {
            return areEqual((Time) object, expected);
        } else if (object instanceof Timestamp) {
            return areEqual((Timestamp) object, expected);
        } else if (object instanceof UUID) {
            return areEqual((UUID) object, expected);
        }
        return expected.equals(object);
    }

    public static boolean areEqual(Value value, Character expected) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }

        if (object instanceof String) {
            return ((String) object).charAt(0) == expected;
        }

        return expected.equals(object);
    }

    public static boolean areEqual(Value value, UUID expected) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }
        return expected.equals(object);
    }

    public static boolean areEqual(UUID value, String expected) {
        if (expected == null) {
            return value == null;
        }
        try {
            return UUID.fromString(expected).equals(value);
        } catch (IllegalArgumentException e) {
            throw new AssertJDBException("Expected <%s> is not correct to compare to <%s>", expected, value);
        }

    }

    public static boolean areEqual(Value value, DateValue expected) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }

        if (object instanceof Date) {
            Date date = (Date) object;
            DateValue dateValue = DateValue.from(date);
            return dateValue.equals(expected);
        } else if (object instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) object;
            DateTimeValue dateTimeValue = DateTimeValue.from(timestamp);
            return dateTimeValue.equals(DateTimeValue.of(expected));
        }
        return false;
    }

    public static boolean areEqual(Value value, TimeValue expected) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }

        if (object instanceof Time) {
            Time time = (Time) object;
            TimeValue timeValue = TimeValue.from(time);
            return timeValue.equals(expected);
        }
        return false;
    }

    public static boolean areEqual(Value value, DateTimeValue expected) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }

        if (object instanceof Date) {
            Date date = (Date) object;
            DateTimeValue dateTimeValue = DateTimeValue.of(DateValue.from(date));
            return dateTimeValue.equals(expected);
        }
        if (object instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) object;
            DateTimeValue dateTimeValue = DateTimeValue.from(timestamp);
            return dateTimeValue.equals(expected);
        }
        return false;
    }

    public static int compare(Value value, Number expected) {
        Object object = value.getValue();
        // If parameter is a BigInteger,
        // change the actual in BigInteger to compare
        if (expected instanceof BigInteger) {
            BigInteger bi;

            bi = getBigInteger(expected, object);

            return bi.compareTo((BigInteger) expected);
        }
        // If parameter is a BigDecimal,
        // change the value in BigDecimal to compare
        else if (expected instanceof BigDecimal) {
            BigDecimal bd;

            bd = getBigDecimal(expected, object);

            return bd.compareTo((BigDecimal) expected);
        }
        // Otherwise
        // If the value is Float, Double, BigInteger or BigDecimal
        // change the value to compare to make the comparison possible
        // else
        // get the value value in Long to compare
        else {
            Long actualValue = null;

            if (object instanceof Float) {
                float f = (Float) object;
                float expectedF = expected.floatValue();
                if (f > expectedF) {
                    return 1;
                } else if (f < expectedF) {
                    return -1;
                } else {
                    return 0;
                }
            } else if (object instanceof Double) {
                double d = (Double) object;
                double expectedD = expected.doubleValue();
                if (d > expectedD) {
                    return 1;
                } else if (d < expectedD) {
                    return -1;
                } else {
                    return 0;
                }
            } else if (object instanceof BigInteger) {
                BigInteger bi = new BigInteger("" + expected);
                return ((BigInteger) object).compareTo(bi);
            } else if (object instanceof BigDecimal) {
                BigDecimal bd = new BigDecimal("" + expected);
                return ((BigDecimal) object).compareTo(bd);
            } else if (object instanceof Byte) {
                actualValue = ((Byte) object).longValue();
            } else if (object instanceof Short) {
                actualValue = ((Short) object).longValue();
            } else if (object instanceof Integer) {
                actualValue = ((Integer) object).longValue();
            } else if (object instanceof Long) {
                actualValue = (Long) object;
            }

            if (actualValue != null) {
                if (expected instanceof Float) {
                    float expectedF = expected.floatValue();
                    if (actualValue > expectedF) {
                        return 1;
                    } else if (actualValue < expectedF) {
                        return -1;
                    } else {
                        return 0;
                    }
                } else if (expected instanceof Double) {
                    double expectedD = expected.doubleValue();
                    if (actualValue > expectedD) {
                        return 1;
                    } else if (actualValue < expectedD) {
                        return -1;
                    } else {
                        return 0;
                    }
                } else {
                    double expectedL = expected.longValue();
                    if (actualValue > expectedL) {
                        return 1;
                    } else if (actualValue < expectedL) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        }

        throw new AssertJDBException("Expected <%s> can not be compared to a Number (<%s>)", expected, object);
    }

    private static BigDecimal getBigDecimal(Number expected, Object object) {
        BigDecimal bd;
        if (object instanceof BigDecimal) {
            bd = (BigDecimal) object;
        } else {
            try {
                bd = new BigDecimal("" + object);
            } catch (NumberFormatException e) {
                throw new AssertJDBException("Expected <%s> can not be compared to a BigDecimal (<%s>)", expected, object);
            }
        }
        return bd;
    }


    public static Object[] getRepresentationsFromValuesInFrontOfExpected(Value[] values, Object[] expected) {
        Object[] representationsValues = new Object[values.length];
        int i = 0;
        for (Value obj : values) {
            if (i >= expected.length) {
                representationsValues[i] = obj.getValue();
            } else {
                representationsValues[i] = Values.getRepresentationFromValueInFrontOfExpected(obj, expected[i]);
            }
            i++;
        }
        return representationsValues;
    }

    public static Object getRepresentationFromValueInFrontOfExpected(Value value, Object expected) {
        Object object = value.getValue();
        ValueType valueType = value.getValueType();
        if (valueType == ValueType.DATE) {
            if (expected instanceof String) {
                if (((String) expected).contains("T")) {
                    return DateTimeValue.of(DateValue.from((Date) object)).toString();
                } else {
                    return DateValue.from((Date) object).toString();
                }
            }
        }
        return getRepresentationFromValueInFrontOfClass(value, expected == null ? null : expected.getClass());
    }

    public static Object getRepresentationFromValueInFrontOfClass(Value value, Class<?> clazz) {
        Object object = value.getValue();
        ValueType valueType = value.getValueType();
        if (valueType == ValueType.DATE) {
            if (clazz == DateValue.class) {
                return DateValue.from((Date) object);
            } else if (clazz == DateTimeValue.class) {
                return DateTimeValue.of(DateValue.from((Date) object));
            } else if (clazz == String.class) {
                return DateTimeValue.of(DateValue.from((Date) object)).toString();
            }
            return object;
        } else if (valueType == ValueType.TIME) {
            if (clazz == String.class) {
                return TimeValue.from((Time) object).toString();
            } else {
                return TimeValue.from((Time) object);
            }
        } else if (valueType == ValueType.DATE_TIME) {
            if (clazz == String.class) {
                return DateTimeValue.from((Timestamp) object).toString();
            } else {
                return DateTimeValue.from((Timestamp) object);
            }
        } else if (valueType == ValueType.NUMBER || valueType == ValueType.UUID) {
            if (clazz == String.class) {
                return object.toString();
            } else {
                return object;
            }
        }
        return object;
    }

    private static boolean isObjectCloseToBigInteger(Object object, BigInteger expected, Number tolerance) {
        BigInteger bi;

        bi = getBigInteger(expected, object);

        BigInteger bigTolerance = new BigInteger("" + tolerance);
        BigInteger bigMin = expected.subtract(bigTolerance);
        BigInteger bigMax = expected.add(bigTolerance);
        if (bi.compareTo(bigMin) >= 0 && bi.compareTo(bigMax) <= 0) {
            return true;
        }
        return false;
    }

    private static boolean isObjectCloseToBigDecimal(Object object, BigDecimal expected, Number tolerance) {
        BigDecimal bd;

        bd = getBigDecimal(expected, object);

        BigDecimal bigExpected = (BigDecimal) expected;
        BigDecimal bigTolerance = new BigDecimal("" + tolerance);
        BigDecimal bigMin = bigExpected.subtract(bigTolerance);
        BigDecimal bigMax = bigExpected.add(bigTolerance);
        if (bd.compareTo(bigMin) >= 0 && bd.compareTo(bigMax) <= 0) {
            return true;
        }
        return false;
    }

    private static boolean isFloatCloseToNumber(Float nb, Number expected, Number tolerance) {
        float fMin = expected.floatValue() - tolerance.floatValue();
        float fMax = expected.floatValue() + tolerance.floatValue();
        if (nb >= fMin && nb <= fMax) {
            return true;
        }
        return false;
    }

    private static boolean isDoubleCloseToNumber(Double nb, Number expected, Number tolerance) {
        double dMin = expected.doubleValue() - tolerance.doubleValue();
        double dMax = expected.doubleValue() + tolerance.doubleValue();
        if (nb >= dMin && nb <= dMax) {
            return true;
        }
        return false;
    }

    private static boolean isBigIntegerCloseToNumber(BigInteger nb, Number expected, Number tolerance) {
        BigInteger bigExpected = new BigInteger("" + expected);
        BigInteger bigTolerance = new BigInteger("" + tolerance);
        BigInteger bigMin = bigExpected.subtract(bigTolerance);
        BigInteger bigMax = bigExpected.add(bigTolerance);
        if (nb.compareTo(bigMin) >= 0 && nb.compareTo(bigMax) <= 0) {
            return true;
        }
        return false;
    }

    private static boolean isBigDecimalCloseToNumber(BigDecimal nb, Number expected, Number tolerance) {
        BigDecimal bigExpected = new BigDecimal("" + expected);
        BigDecimal bigTolerance = new BigDecimal("" + tolerance);
        BigDecimal bigMin = bigExpected.subtract(bigTolerance);
        BigDecimal bigMax = bigExpected.add(bigTolerance);
        if (nb.compareTo(bigMin) >= 0 && nb.compareTo(bigMax) <= 0) {
            return true;
        }
        return false;
    }

    private static Long getLong(Object object) {
        if (object instanceof Byte) {
            return ((Byte) object).longValue();
        } else if (object instanceof Short) {
            return ((Short) object).longValue();
        } else if (object instanceof Integer) {
            return ((Integer) object).longValue();
        } else if (object instanceof Long) {
            return (Long) object;
        }
        return null;
    }

    private static boolean isLongCloseToFloat(Long nb, Float expected, Number tolerance) {
        if (tolerance instanceof Float) {
            if (nb >= expected.floatValue() - tolerance.floatValue() &&
                    nb <= expected.floatValue() + tolerance.floatValue()) {

                return true;
            }
        } else if (tolerance instanceof Double) {
            if (nb >= expected.floatValue() - tolerance.doubleValue() &&
                    nb <= expected.floatValue() + tolerance.doubleValue()) {

                return true;
            }
        } else {
            if (nb >= expected.floatValue() - tolerance.longValue() &&
                    nb <= expected.floatValue() + tolerance.longValue()) {

                return true;
            }
        }
        return false;
    }

    /**
     * Returns if nb is close to the {@code Double} in parameter with the tolerance in parameter.
     * @param nb        The {@code Long}.
     * @param expected  The {@code Double} to compare.
     * @param tolerance The tolerance of the closeness.
     * @return {@code true} if the value is close to the {@code Double} parameter, {@code false} otherwise.
     */
    private static boolean isLongCloseToDouble(Long nb, Double expected, Number tolerance) {
        if (tolerance instanceof Float) {
            if (nb >= expected.doubleValue() - tolerance.floatValue() &&
                    nb <= expected.doubleValue() + tolerance.floatValue()) {

                return true;
            }
        } else if (tolerance instanceof Double) {
            if (nb >= expected.doubleValue() - tolerance.doubleValue() &&
                    nb <= expected.doubleValue() + tolerance.doubleValue()) {

                return true;
            }
        } else {
            if (nb >= expected.doubleValue() - tolerance.longValue() &&
                    nb <= expected.doubleValue() + tolerance.longValue()) {

                return true;
            }
        }
        return false;
    }

    private static boolean isLongCloseToNumber(Long nb, Number expected, Number tolerance) {
        if (tolerance instanceof Float) {
            if (nb >= expected.longValue() - tolerance.floatValue() &&
                    nb <= expected.longValue() + tolerance.floatValue()) {

                return true;
            }
        } else if (tolerance instanceof Double) {
            if (nb >= expected.longValue() - tolerance.doubleValue() &&
                    nb <= expected.longValue() + tolerance.doubleValue()) {

                return true;
            }
        } else {
            if (nb >= expected.longValue() - tolerance.longValue() &&
                    nb <= expected.longValue() + tolerance.longValue()) {

                return true;
            }
        }
        return false;
    }

    public static boolean areClose(Value value, Number expected, Number tolerance) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }

        if (expected instanceof BigInteger) {
            return isObjectCloseToBigInteger(object, (BigInteger) expected, tolerance);
        } else if (expected instanceof BigDecimal) {
            return isObjectCloseToBigDecimal(object, (BigDecimal) expected, tolerance);
        } else if (object instanceof Float) {
            return isFloatCloseToNumber((Float) object, expected, tolerance);
        } else if (object instanceof Double) {
            return isDoubleCloseToNumber((Double) object, expected, tolerance);
        } else if (object instanceof BigInteger) {
            return isBigIntegerCloseToNumber((BigInteger) object, expected, tolerance);
        } else if (object instanceof BigDecimal) {
            return isBigDecimalCloseToNumber((BigDecimal) object, expected, tolerance);
        } else {
            Long actualValue = getLong(object);
            if (actualValue != null) {
                if (expected instanceof Float) {
                    return isLongCloseToFloat(actualValue, expected.floatValue(), tolerance);
                } else if (expected instanceof Double) {
                    return isLongCloseToDouble(actualValue, expected.doubleValue(), tolerance);
                } else {
                    return isLongCloseToNumber(actualValue, expected.longValue(), tolerance);
                }
            }
        }

        return false;
    }

    public static boolean areClose(Value value, DateValue expected, DateValue tolerance) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }

        if (object instanceof Date) {
            Date date = (Date) object;
            DateValue dateValue = DateValue.from(date);
            DateValue dateValueMin = expected.move(tolerance.reverse());
            DateValue dateValueMax = expected.move(tolerance);
            return dateValue.compareTo(dateValueMin) >= 0 && dateValue.compareTo(dateValueMax) <= 0;
        } else if (object instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) object;
            DateTimeValue dateTimeValue = DateTimeValue.from(timestamp);
            DateTimeValue dateTimeValueMin = DateTimeValue.of(expected).move(tolerance.reverse());
            DateTimeValue dateTimeValueMax = DateTimeValue.of(expected).move(tolerance);
            return dateTimeValue.compareTo(dateTimeValueMin) >= 0 && dateTimeValue.compareTo(dateTimeValueMax) <= 0;
        }
        return false;
    }

    public static boolean areClose(Value value, DateValue expected, TimeValue tolerance) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }
        if (object instanceof Date) {
            Date date = (Date) object;
            DateTimeValue dateTimeValue = DateTimeValue.of(DateValue.from(date));
            DateTimeValue dateTimeValueMin = expected.move(tolerance.reverse());
            DateTimeValue dateTimeValueMax = expected.move(tolerance);
            return dateTimeValue.compareTo(dateTimeValueMin) >= 0 && dateTimeValue.compareTo(dateTimeValueMax) <= 0;
        } else if (object instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) object;
            DateTimeValue dateTimeValue = DateTimeValue.from(timestamp);
            DateTimeValue dateTimeValueMin = DateTimeValue.of(expected).move(tolerance.reverse());
            DateTimeValue dateTimeValueMax = DateTimeValue.of(expected).move(tolerance);
            return dateTimeValue.compareTo(dateTimeValueMin) >= 0 && dateTimeValue.compareTo(dateTimeValueMax) <= 0;
        }
        return false;
    }

    public static boolean areClose(Value value, DateValue expected, DateTimeValue tolerance) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }
        if (object instanceof Date) {
            Date date = (Date) object;
            DateTimeValue dateTimeValue = DateTimeValue.of(DateValue.from(date));
            DateTimeValue dateTimeValueMin = expected.move(tolerance.reverse());
            DateTimeValue dateTimeValueMax = expected.move(tolerance);
            return dateTimeValue.compareTo(dateTimeValueMin) >= 0 && dateTimeValue.compareTo(dateTimeValueMax) <= 0;
        } else if (object instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) object;
            DateTimeValue dateTimeValue = DateTimeValue.from(timestamp);
            DateTimeValue dateTimeValueMin = DateTimeValue.of(expected).move(tolerance.reverse());
            DateTimeValue dateTimeValueMax = DateTimeValue.of(expected).move(tolerance);
            return dateTimeValue.compareTo(dateTimeValueMin) >= 0 && dateTimeValue.compareTo(dateTimeValueMax) <= 0;
        }
        return false;
    }

    public static boolean areClose(Value value, TimeValue expected, TimeValue tolerance) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }

        if (object instanceof Time) {
            Time time = (Time) object;
            TimeValue timeValue = TimeValue.from(time);
            TimeValue timeValueMin = expected.move(tolerance.reverse());
            TimeValue timeValueMax = expected.move(tolerance);
            return timeValue.compareTo(timeValueMin) >= 0 && timeValue.compareTo(timeValueMax) <= 0;
        }
        return false;
    }

    public static boolean areClose(Value value, DateTimeValue expected, DateValue tolerance) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }

        if (object instanceof Date) {
            Date date = (Date) object;
            DateTimeValue dateTimeValue = DateTimeValue.of(DateValue.from(date));
            DateTimeValue dateTimeValueMin = expected.move(tolerance.reverse());
            DateTimeValue dateTimeValueMax = expected.move(tolerance);
            return dateTimeValue.compareTo(dateTimeValueMin) >= 0 && dateTimeValue.compareTo(dateTimeValueMax) <= 0;
        }
        if (object instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) object;
            DateTimeValue dateTimeValue = DateTimeValue.from(timestamp);
            DateTimeValue dateTimeValueMin = expected.move(tolerance.reverse());
            DateTimeValue dateTimeValueMax = expected.move(tolerance);
            return dateTimeValue.compareTo(dateTimeValueMin) >= 0 && dateTimeValue.compareTo(dateTimeValueMax) <= 0;
        }
        return false;
    }

    public static boolean areClose(Value value, DateTimeValue expected, TimeValue tolerance) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }

        if (object instanceof Date) {
            Date date = (Date) object;
            DateTimeValue dateTimeValue = DateTimeValue.of(DateValue.from(date));
            DateTimeValue dateTimeValueMin = expected.move(tolerance.reverse());
            DateTimeValue dateTimeValueMax = expected.move(tolerance);
            return dateTimeValue.compareTo(dateTimeValueMin) >= 0 && dateTimeValue.compareTo(dateTimeValueMax) <= 0;
        }
        if (object instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) object;
            DateTimeValue dateTimeValue = DateTimeValue.from(timestamp);
            DateTimeValue dateTimeValueMin = expected.move(tolerance.reverse());
            DateTimeValue dateTimeValueMax = expected.move(tolerance);
            return dateTimeValue.compareTo(dateTimeValueMin) >= 0 && dateTimeValue.compareTo(dateTimeValueMax) <= 0;
        }
        return false;
    }

    public static boolean areClose(Value value, DateTimeValue expected, DateTimeValue tolerance) {
        Object object = value.getValue();
        if (expected == null) {
            return object == null;
        }

        if (object instanceof Date) {
            Date date = (Date) object;
            DateTimeValue dateTimeValue = DateTimeValue.of(DateValue.from(date));
            DateTimeValue dateTimeValueMin = expected.move(tolerance.reverse());
            DateTimeValue dateTimeValueMax = expected.move(tolerance);
            return dateTimeValue.compareTo(dateTimeValueMin) >= 0 && dateTimeValue.compareTo(dateTimeValueMax) <= 0;
        }
        if (object instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) object;
            DateTimeValue dateTimeValue = DateTimeValue.from(timestamp);
            DateTimeValue dateTimeValueMin = expected.move(tolerance.reverse());
            DateTimeValue dateTimeValueMax = expected.move(tolerance);
            return dateTimeValue.compareTo(dateTimeValueMin) >= 0 && dateTimeValue.compareTo(dateTimeValueMax) <= 0;
        }
        return false;
    }
}
