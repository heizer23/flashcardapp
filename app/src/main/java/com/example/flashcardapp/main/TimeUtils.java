package com.example.flashcardapp.main;

public class TimeUtils {

    // Method to convert milliseconds to dd-hh:mm:ss format
    public static String formatTimeDifference(long timeInMillis) {
        long seconds = timeInMillis / 1000;
        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        return String.format("%02d\n%02d:%02d:%02d", days, hours, minutes, seconds);
    }
}
