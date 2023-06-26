package com.slimebot.utils;


import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DailyTask {
    public DailyTask(int hour, Runnable runnable){
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime nextRun = now.withHour(hour).withMinute(0).withSecond(0);
        if(now.compareTo(nextRun) > 0)
            nextRun = nextRun.plusDays(1);
        System.out.println("Next run: "+nextRun);
        Duration duration = Duration.between(now, nextRun);
        long initialDelay = duration.getSeconds();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(runnable, initialDelay, 24*60*60, TimeUnit.SECONDS);
    }
}
