package com.example.flashcardapp.main;

public class TimeUtils {

    // Method to convert milliseconds to mm:hh DD format
    public static String formatInterval(long timeInMillis) {
        long seconds = timeInMillis / 1000;
        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;

        return String.format("%02d:%02d %02d", minutes, hours, days);
    }

    // Method to convert milliseconds to DD HH:MM:SS format
    public static String formatIntervalDetailed(long timeInMillis) {
        long seconds = timeInMillis / 1000;
        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        return String.format("%02d %02d:%02d:%02d", days, hours, minutes, remainingSeconds);
    }
}
