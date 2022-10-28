package cn.t.freetunnel.client.socks5.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadUtil {
    public static final ScheduledExecutorService scheduledExecutorService =  Executors.newScheduledThreadPool(1);
    public static void scheduleTask(Runnable runnable, int initialDelayInSeconds, int periodInSeconds) {
        scheduledExecutorService.scheduleAtFixedRate(runnable, initialDelayInSeconds, periodInSeconds, TimeUnit.SECONDS);
    }
}
