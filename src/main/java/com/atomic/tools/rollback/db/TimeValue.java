package com.atomic.tools.rollback.db;

import java.sql.Time;
import java.text.ParseException;
import java.util.Calendar;

/**
 * This class represents a time value in the database.
 * @author Régis Pouiller
 */
public class TimeValue implements Comparable<TimeValue> {

    private static final String TIME_FORMAT = "\\d\\d:\\d\\d";
    private static final String TIME_FORMAT_WITH_SECONDS = "\\d\\d:\\d\\d:\\d\\d";
    private static final String TIME_FORMAT_WITH_NANO = "\\d\\d:\\d\\d:\\d\\d.\\d\\d\\d\\d\\d\\d\\d\\d\\d";
    /**
     * 小时.
     */
    private final int hours;
    /**
     * 分钟.
     */
    private final int minutes;
    /**
     * 秒.
     */
    private final int seconds;
    /**
     * 纳秒.
     */
    private final int nanoSeconds;

    public TimeValue(int hours, int minutes, int seconds, int nanoSeconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.nanoSeconds = nanoSeconds;
    }

    public TimeValue(int hours, int minutes, int seconds) {
        this(hours, minutes, seconds, 0);
    }

    public TimeValue(int hours, int minutes) {
        this(hours, minutes, 0, 0);
    }

    public TimeValue(String time) throws ParseException {
        if (time == null) {
            throw new NullPointerException("time should be not null");
        }

        if (time.matches(TIME_FORMAT)) {
            hours = Integer.parseInt(time.substring(0, 2));
            minutes = Integer.parseInt(time.substring(3));
            seconds = 0;
            nanoSeconds = 0;
        } else if (time.matches(TIME_FORMAT_WITH_SECONDS)) {
            hours = Integer.parseInt(time.substring(0, 2));
            minutes = Integer.parseInt(time.substring(3, 5));
            seconds = Integer.parseInt(time.substring(6));
            nanoSeconds = 0;
        } else if (time.matches(TIME_FORMAT_WITH_NANO)) {
            hours = Integer.parseInt(time.substring(0, 2));
            minutes = Integer.parseInt(time.substring(3, 5));
            seconds = Integer.parseInt(time.substring(6, 8));
            nanoSeconds = Integer.parseInt(time.substring(9));
        } else {
            throw new ParseException("time must respect hh:mm, hh:mm:ss or hh:mm:ss.nnnnnnnnn format", time.length());
        }
    }

    public TimeValue(Time time) {
        if (time == null) {
            throw new NullPointerException("time should be not null");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time.getTime());

        hours = calendar.get(Calendar.HOUR_OF_DAY);
        minutes = calendar.get(Calendar.MINUTE);
        seconds = calendar.get(Calendar.SECOND);
        nanoSeconds = calendar.get(Calendar.MILLISECOND) * 1000000;
    }

    public TimeValue(Calendar calendar) {
        if (calendar == null) {
            throw new NullPointerException("time should be not null");
        }

        hours = calendar.get(Calendar.HOUR_OF_DAY);
        minutes = calendar.get(Calendar.MINUTE);
        seconds = calendar.get(Calendar.SECOND);
        nanoSeconds = calendar.get(Calendar.MILLISECOND) * 1000000;
    }

    /**
     * 创建时间实例
     * @param hours       小时.
     * @param minutes     分钟.
     * @param seconds     秒.
     * @param nanoSeconds 纳秒.
     * @return
     */
    public static TimeValue of(int hours, int minutes, int seconds, int nanoSeconds) {
        return new TimeValue(hours, minutes, seconds, nanoSeconds);
    }

    public static TimeValue of(int hours, int minutes, int seconds) {
        return new TimeValue(hours, minutes, seconds);
    }

    public static TimeValue of(int hours, int minutes) {
        return new TimeValue(hours, minutes);
    }

    public static TimeValue parse(String time) throws ParseException {
        return new TimeValue(time);
    }

    public static TimeValue from(Time time) {
        return new TimeValue(time);
    }

    public static TimeValue from(Calendar calendar) {
        return new TimeValue(calendar);
    }

    public static TimeValue now() {
        return from(Calendar.getInstance());
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getNanoSeconds() {
        return nanoSeconds;
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d:%02d.%09d", hours, minutes, seconds, nanoSeconds);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeValue) {
            TimeValue timeValue = (TimeValue) obj;
            return hours == timeValue.hours && minutes == timeValue.minutes && seconds == timeValue.seconds
                    && nanoSeconds == timeValue.nanoSeconds;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + hours;
        result = prime * result + minutes;
        result = prime * result + nanoSeconds;
        result = prime * result + seconds;
        return result;
    }

    @Override
    public int compareTo(TimeValue other) {
        if (hours < other.hours) {
            return -1;
        } else if (hours > other.hours) {
            return 1;
        } else if (minutes < other.minutes) {
            return -1;
        } else if (minutes > other.minutes) {
            return 1;
        } else if (seconds < other.seconds) {
            return -1;
        } else if (seconds > other.seconds) {
            return 1;
        } else if (nanoSeconds < other.nanoSeconds) {
            return -1;
        } else if (nanoSeconds > other.nanoSeconds) {
            return 1;
        }
        return 0;
    }

    public boolean isBefore(TimeValue time) {
        return compareTo(time) == -1;
    }

    public boolean isAfter(TimeValue time) {
        return compareTo(time) == 1;
    }

    public TimeValue move(TimeValue time) {
        int thisHours = this.hours;
        int thisMinutes = this.minutes;
        int thisSeconds = this.seconds;
        int thisNanoSeconds = this.nanoSeconds;

        int hours = time.getHours();
        int minutes = time.getMinutes();
        int seconds = time.getSeconds();
        int nanoSeconds = time.getNanoSeconds();

        if (nanoSeconds >= 0 || thisNanoSeconds >= -nanoSeconds) {
            thisNanoSeconds += nanoSeconds;
        } else {
            thisNanoSeconds += 1000000000 + nanoSeconds;
            seconds--;
        }
        if (seconds >= 0 || thisSeconds >= -seconds) {
            thisSeconds += seconds;
        } else {
            thisSeconds += 60 + seconds;
            minutes--;
        }
        if (minutes >= 0 || thisMinutes >= -minutes) {
            thisMinutes += minutes;
        } else {
            thisMinutes += 60 + minutes;
            hours--;
        }
        thisHours += hours;

        return of(thisHours, thisMinutes, thisSeconds, thisNanoSeconds);
    }

    public TimeValue reverse() {
        return of(-getHours(), -getMinutes(), -getSeconds(), -getNanoSeconds());
    }
}
