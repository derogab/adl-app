package com.derogab.adlanalyzer.utils;

import java.util.Locale;

public class CountDown {

    private static final int seconds_in_a_minute = 60;
    private static final int minutes_in_a_hour = 60;
    private static final int hours_in_a_day = 24;

    private static final String two_digit_format = "%02d";

    public static String get(long input) {

        long days, hours, minutes, seconds;

        days = input / (hours_in_a_day * minutes_in_a_hour * seconds_in_a_minute);
            input = input % (hours_in_a_day * minutes_in_a_hour * seconds_in_a_minute);

        hours = input / (minutes_in_a_hour * seconds_in_a_minute);
            input = input % (minutes_in_a_hour * seconds_in_a_minute);

        minutes = input / seconds_in_a_minute;
            input = input % seconds_in_a_minute;

        seconds = input;

        String output;

        if (days > 0) {
            output = String.format(Locale.getDefault(), two_digit_format, days) + ":"
                    + String.format(Locale.getDefault(), two_digit_format, hours) + ":"
                    + String.format(Locale.getDefault(), two_digit_format, minutes) + ":"
                    + String.format(Locale.getDefault(), two_digit_format, seconds);
        }
        else if (hours > 0) {
            output = String.format(Locale.getDefault(), two_digit_format, hours) + ":"
                    + String.format(Locale.getDefault(), two_digit_format, minutes) + ":"
                    + String.format(Locale.getDefault(), two_digit_format, seconds);
        }
        else {
            output = String.format(Locale.getDefault(), two_digit_format, minutes) + ":"
                    + String.format(Locale.getDefault(), two_digit_format, seconds);
        }

        return output;

    }

}
