package cn.t.tool.netproxytool.util;

import cn.t.tool.netproxytool.constants.ThreadPoolConfig;
import cn.t.tool.netproxytool.monitor.MonitoredThreadFactory;
import cn.t.tool.netproxytool.monitor.MonitoredThreadPool;
import cn.t.tool.netproxytool.monitor.ThreadPoolMonitor;

import java.util.concurrent.*;

/**
 * 线程工具
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-22 22:45
 **/
public class ThreadUtil {

    public static final ScheduledExecutorService scheduledExecutorService =  Executors.newScheduledThreadPool(1);

    public static void submitProxyTask(Runnable runnable) {
        Socks5ThreadPoolHolder.PROXY_THREAD_POOL_EXECUTOR.submit(runnable);
    }

    public static void scheduleTask(Runnable runnable, int initialDelayInSeconds, int periodInSeconds) {
        scheduledExecutorService.scheduleAtFixedRate(runnable, initialDelayInSeconds, periodInSeconds, TimeUnit.SECONDS);
    }

    public static void closeThreadPool() {
        if(!Socks5ThreadPoolHolder.PROXY_THREAD_POOL_EXECUTOR.isShutdown()) {
            Socks5ThreadPoolHolder.PROXY_THREAD_POOL_EXECUTOR.shutdownNow();
        }
        if(!scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdownNow();
        }
    }

    private static class Socks5ThreadPoolHolder {
        private static final ThreadPoolExecutor PROXY_THREAD_POOL_EXECUTOR = new MonitoredThreadPool(
            ThreadPoolConfig.PROXY_CORE_THREAD_COUNT,
            ThreadPoolConfig.PROXY_MAX_THREAD_COUNT,
            ThreadPoolConfig.PROXY_THREAD_TT,
            ThreadPoolConfig.PROXY_THREAD_TT_TIME_UNIT,
            new ArrayBlockingQueue<>(ThreadPoolConfig.PROXY_BLOCKING_THREAD_COUNT),
            new MonitoredThreadFactory(ThreadPoolConfig.PROXY_THREAD_POOL_NAME),
            ThreadPoolConfig.PROXY_THREAD_POOL_NAME
        );
        static {
            scheduleTask(new ThreadPoolMonitor(ThreadPoolConfig.PROXY_THREAD_POOL_NAME, PROXY_THREAD_POOL_EXECUTOR), 3, 5);
        }
    }



}
