package com.atomic.tools.db;


import java.sql.Date;
import java.text.ParseException;
import java.util.Calendar;

/**
 * 这个类表示数据库中的日期值
 */
public class DateValue implements Comparable<DateValue>, DateValueContainer {

    private static final String DATE_FORMAT = "\\d\\d\\d\\d-\\d\\d-\\d\\d";
    /**
     * 一个月的一天
     */
    private final int dayOfTheMonth;
    private final int month;
    private final int year;

    public DateValue(int year, int month, int dayOfTheMonth) {
        this.dayOfTheMonth = dayOfTheMonth;
        this.month = month;
        this.year = year;
    }

    public DateValue(String date) throws ParseException {
        if (date == null) {
            throw new NullPointerException("date should be not null");
        }

        if (date.matches(DATE_FORMAT)) {
            year = Integer.parseInt(date.substring(0, 4));
            month = Integer.parseInt(date.substring(5, 7));
            dayOfTheMonth = Integer.parseInt(date.substring(8));
        } else {
            throw new ParseException("date must respect yyyy-mm-dd format", date.length());
        }
    }

    public DateValue(Date date) {
        if (date == null) {
            throw new NullPointerException("date should be not null");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());

        dayOfTheMonth = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH) + 1;
        year = calendar.get(Calendar.YEAR);
    }

    public DateValue(Calendar calendar) {
        if (calendar == null) {
            throw new NullPointerException("date should be not null");
        }

        dayOfTheMonth = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH) + 1;
        year = calendar.get(Calendar.YEAR);
    }

    public static DateValue of(int year, int month, int dayOfTheMonth) {
        return new DateValue(year, month, dayOfTheMonth);
    }

    /**
     * 格式化一个字符串为日期 {@code String} {@code yyyy-mm-dd}.
     * @param date Date in {@code String} format ({@code yyyy-mm-dd}).
     * @return
     * @throws NullPointerException If {@code date} is {@code null}.
     * @throws ParseException
     */
    public static DateValue parse(String date) throws ParseException {
        return new DateValue(date);
    }

    public static DateValue from(Date date) {
        return new DateValue(date);
    }

    public static DateValue from(Calendar calendar) {
        return new DateValue(calendar);
    }

    public static DateValue now() {
        return from(Calendar.getInstance());
    }

    @Override
    public DateValue getDate() {
        return this;
    }

    public boolean isMidnight() {
        return true;
    }

    /**
     * 返回当月的日期
     * @return 返回当月的日期
     */
    public int getDayOfTheMonth() {
        return dayOfTheMonth;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String toString() {
        return String.format("%4d-%02d-%02d", year, month, dayOfTheMonth);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DateValue) {
            DateValue dateValue = (DateValue) obj;
            return year == dateValue.year && month == dateValue.month && dayOfTheMonth == dateValue.dayOfTheMonth;
        } else if (obj instanceof DateValueContainer) {
            DateValueContainer value = (DateValueContainer) obj;
            return equals(value.getDate()) && value.isMidnight();
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dayOfTheMonth;
        result = prime * result + month;
        result = prime * result + year;
        return result;
    }

    @Override
    public int compareTo(DateValue other) {
        if (year < other.year) {
            return -1;
        } else if (year > other.year) {
            return 1;
        } else if (month < other.month) {
            return -1;
        } else if (month > other.month) {
            return 1;
        } else if (dayOfTheMonth < other.dayOfTheMonth) {
            return -1;
        } else if (dayOfTheMonth > other.dayOfTheMonth) {
            return 1;
        }
        return 0;
    }

    public boolean isBefore(DateValue date) {
        return compareTo(date) == -1;
    }

    public boolean isAfter(DateValue date) {
        return compareTo(date) == 1;
    }

    public DateValue move(DateValue date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfTheMonth);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (date.getYear() != 0) {
            calendar.add(Calendar.YEAR, date.getYear());
        }
        if (date.getMonth() != 0) {
            calendar.add(Calendar.MONTH, date.getMonth());
        }
        if (date.getDayOfTheMonth() != 0) {
            calendar.add(Calendar.DAY_OF_MONTH, date.getDayOfTheMonth());
        }

        return from(calendar);
    }

    public DateTimeValue move(TimeValue time) {
        TimeValue timeValue = TimeValue.of(0, 0);
        TimeValue movedTimeValue = timeValue.move(time);

        int hours = movedTimeValue.getHours();
        int days = hours / 24;
        if (hours > 0) {
            hours -= days * 24;
        } else {
            hours += days * 24;
        }
        if (hours < 0) {
            days--;
            hours += 24;
        }

        DateValue dateValue = getDate();
        DateValue movedDateValue = dateValue.move(DateValue.of(0, 0, days));

        return DateTimeValue.of(movedDateValue, TimeValue.of(hours, movedTimeValue.getMinutes(),
                movedTimeValue.getSeconds(), movedTimeValue.getNanoSeconds()));
    }

    public DateTimeValue move(DateTimeValue dateTime) {
        DateValue date = move(dateTime.getDate());
        return date.move(dateTime.getTime());
    }

    /**
     * 返回日期的反向
     * @return 返回日期的反向
     */
    public DateValue reverse() {
        return of(-getYear(), -getMonth(), -getDayOfTheMonth());
    }
}
