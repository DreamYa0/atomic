package com.atomic.tools.db;


import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;

/**
 * 这个类表示数据库中的日期/时间值
 */
public class DateTimeValue implements Comparable<DateTimeValue>, DateValueContainer {

    private static final String DATE_FORMAT = "\\d\\d\\d\\d-\\d\\d-\\d\\d";
    private static final String TIME_FORMAT = "\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d";
    private static final String TIME_FORMAT_WITH_SECONDS = "\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d";
    private static final String TIME_FORMAT_WITH_NANO = "\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d.\\d\\d\\d\\d\\d\\d\\d\\d\\d";
    private final DateValue date;
    private final TimeValue time;


    public DateTimeValue(DateValue date, TimeValue time) {
        if (date == null) {
            throw new NullPointerException("date should be not null");
        }
        if (time == null) {
            throw new NullPointerException("time should be not null");
        }
        this.date = date;
        this.time = time;
    }

    /**
     * @param dateTime Time in {@code String} format ({@code yyyy-mm-dd}, {@code yyyy-mm-ddThh:mm}, {@code
     *                 yyyy-mm-ddThh:mm:ss} or {@code yyyy-mm-ddThh:mm:ss.nnnnnnnnn}).
     * @throws NullPointerException If {@code dateTime} is {@code null}.
     * @throws ParseException       If {@code date} don't respect the {@code yyyy-mm-dd}, {@code yyyy-mm-ddThh:mm},
     *                              {@code yyyy-mm-ddThh:mm:ss} or {@code yyyy-mm-ddThh:mm:ss.nnnnnnnnn} format.
     */
    public DateTimeValue(String dateTime) throws ParseException {
        if (dateTime == null) {
            throw new NullPointerException("date/time should be not null");
        }

        if (dateTime.matches(DATE_FORMAT)) {
            date = DateValue.parse(dateTime);
            time = new TimeValue(0, 0);
        } else if (dateTime.matches(TIME_FORMAT) || dateTime.matches(TIME_FORMAT_WITH_SECONDS)
                || dateTime.matches(TIME_FORMAT_WITH_NANO)) {

            date = DateValue.parse(dateTime.substring(0, 10));
            time = TimeValue.parse(dateTime.substring(11));
        } else {
            throw new ParseException("date/time must respect yyyy-mm-dd, yyyy-mm-ddThh:mm, "
                    + "yyyy-mm-ddThh:mm:ss or yyyy-mm-ddThh:mm:ss.nnnnnnnnn format", dateTime.length());
        }
    }

    /**
     * @param timestamp 时间戳
     * @throws NullPointerException 如果timestamp为null
     */
    public DateTimeValue(Timestamp timestamp) {
        if (timestamp == null) {
            throw new NullPointerException("date/time should be not null");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp.getTime());

        date = DateValue.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
        time = TimeValue.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND), timestamp.getNanos());
    }

    /**
     * @param calendar 日历
     * @throws NullPointerException 如果calendar为null
     */
    public DateTimeValue(Calendar calendar) {
        if (calendar == null) {
            throw new NullPointerException("date/time should be not null");
        }

        date = DateValue.from(calendar);
        time = TimeValue.from(calendar);
    }

    public static DateTimeValue of(DateValue date) {
        return new DateTimeValue(date, TimeValue.of(0, 0));
    }

    /**
     * 在日期和时间上做一个日期/时间值的实例
     * @param date
     * @param time
     * @return 日期/时间的实例
     */
    public static DateTimeValue of(DateValue date, TimeValue time) {
        return new DateTimeValue(date, time);
    }

    /**
     * 格式化时间 {@code String} in {@code yyyy-mm-dd}, {@code yyyy-mm-ddThh:mm},
     * {@code yyyy-mm-ddThh:mm:ss} or {@code yyyy-mm-ddThh:mm:ss.nnnnnnnnn} format.
     * @param dateTime Date/time in {@code String} format ({@code yyyy-mm-dd}).
     * @return
     * @throws NullPointerException 如果 dateTime 是 null
     * @throws ParseException       如果 date 不是 {@code yyyy-mm-dd}, {@code yyyy-mm-ddThh:mm}, {@code yyyy-mm-ddThh:mm:ss}
     *                              or {@code yyyy-mm-ddThh:mm:ss.nnnnnnnnn} 格式.
     */
    public static DateTimeValue parse(String dateTime) throws ParseException {
        return new DateTimeValue(dateTime);
    }

    /**
     * 格式化时间戳
     * @param timestamp 时间戳
     * @return
     * @throws NullPointerException
     */
    public static DateTimeValue from(Timestamp timestamp) {
        return new DateTimeValue(timestamp);
    }

    /**
     * 格式化日历
     * @param calendar 日历
     * @return
     * @throws NullPointerException
     * @since 1.1.0
     */
    public static DateTimeValue from(Calendar calendar) {
        return new DateTimeValue(calendar);
    }

    /**
     * 创建与现在对应的日期/时间值的实例
     * @return 创建与现在对应的日期/时间值的实例
     * @since 1.1.0
     */
    public static DateTimeValue now() {
        return from(Calendar.getInstance());
    }

    @Override
    public DateValue getDate() {
        return date;
    }

    public boolean isMidnight() {
        return time.getHours() == 0 && time.getMinutes() == 0 && time.getSeconds() == 0 && time.getNanoSeconds() == 0;
    }

    public TimeValue getTime() {
        return time;
    }

    @Override
    public String toString() {
        return String.format("%4d-%02d-%02dT%02d:%02d:%02d.%09d", date.getYear(), date.getMonth(), date.getDayOfTheMonth(),
                time.getHours(), time.getMinutes(), time.getSeconds(), time.getNanoSeconds());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DateTimeValue) {
            DateTimeValue dateTimeValue = (DateTimeValue) obj;
            return date.equals(dateTimeValue.date) && time.equals(dateTimeValue.time);
        } else if (obj instanceof DateValueContainer) {
            DateValueContainer value = (DateValueContainer) obj;
            return date.equals(value.getDate()) && isMidnight();
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + date.hashCode();
        result = prime * result + time.hashCode();
        return result;
    }

    @Override
    public int compareTo(DateTimeValue other) {
        int compareDate = date.compareTo(other.date);
        if (compareDate != 0) {
            return compareDate;
        }
        return time.compareTo(other.time);
    }

    /**
     * 如果此日期/时间值在参数的日期/时间值之前返回 true
     * @param dateTime
     * @return
     */
    public boolean isBefore(DateTimeValue dateTime) {
        return compareTo(dateTime) == -1;
    }

    /**
     * 如果这个日期/时间值在参数的日期/时间值之后返回 true
     * @param dateTime The date/time value to compare to.
     * @return If this date/time value is after the date/time value in parameter.
     */
    public boolean isAfter(DateTimeValue dateTime) {
        return compareTo(dateTime) == 1;
    }

    /**
     * 将日期/时间与参数值一起移动
     * @param date
     * @return
     */
    public DateTimeValue move(DateValue date) {
        TimeValue timeValue = getTime();

        DateValue dateValue = getDate();
        DateValue movedDateValue = dateValue.move(date);

        return of(movedDateValue, timeValue);
    }

    public DateTimeValue move(TimeValue time) {
        TimeValue timeValue = getTime();
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

        return of(movedDateValue, TimeValue.of(hours, movedTimeValue.getMinutes(),
                movedTimeValue.getSeconds(), movedTimeValue.getNanoSeconds()));
    }

    public DateTimeValue move(DateTimeValue dateTime) {
        DateTimeValue aDateTime = move(dateTime.getDate());
        return aDateTime.move(dateTime.getTime());
    }

    /**
     * 返回日期/时间的反转
     * @return
     */
    public DateTimeValue reverse() {
        return of(date.reverse(), time.reverse());
    }
}
