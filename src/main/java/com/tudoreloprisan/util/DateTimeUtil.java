package com.tudoreloprisan.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeUtil {

    public static String getDateTimeFromUnixRepresentation(String dateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'");
        return dateTimeFormatter.print(dateTimeFormatter.parseDateTime(dateTime));
    }

    public static String getDateTimeAsStringFromRegularDateTime(String dateTime) {
        DateTimeFormatter regularDateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
        DateTimeFormatter unixDateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'");
        DateTime regularDateTime = regularDateTimeFormatter.parseDateTime(dateTime);
        return unixDateTimeFormatter.print(regularDateTime);
    }

    public static DateTime getDateTimeFromRegularDateTime(String dateTime) {
        DateTimeFormatter regularDateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd' 'HH:mm:ss");
        DateTimeFormatter unixDateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'");
        DateTime regularDateTime = regularDateTimeFormatter.parseDateTime(dateTime);
        return regularDateTime;
    }

    /*
//2018-08-01T15:00:00.000000000Z
dateTimeFormatter.print(dateTimeFormatter.parseDateTime(startDate));
     */

}
