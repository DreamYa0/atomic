package com.atomic.param;

import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author dreamyao
 * @title
 * @date ${data} ${time}
 * @since 1.0.0
 */
public class ParamUtilsTest {

    @Test
    public void testGetJSONStringWithDateFormat() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parse = sdf.parse("2018-05-06 00:00:00");
            Person person = new Person();
            person.setDate(parse);
            person.setName("DreamYao");
            String string = ParamUtils.getJSONStringWithDateFormat(person, true, "yyyy-MM-dd HH:mm:ss");
            System.out.println(string);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private class Person{
        private String name;
        private Date date;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}