package com.slimebot.utils;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class TimeScheduler {
    Timer timer;
    int seconds;

    public TimeScheduler(int seconds){
        this.timer = new Timer();
        this.seconds = seconds;
    }

    public void startTimer(TimerTask timerTask){
        timer.scheduleAtFixedRate(timerTask, 0, seconds * 1000);
    }
}