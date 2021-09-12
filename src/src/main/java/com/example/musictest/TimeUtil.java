package com.example.musictest;

public class TimeUtil {
        public static String formatTime(int milli){
            int minute = milli / 1000 / 60;
            int second = milli / 1000 % 60;
            return String.format("%02d:%02d", minute, second);
        }
    }

