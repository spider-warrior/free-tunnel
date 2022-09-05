package cn.t.freetunnel.common.thread;

import cn.t.freetunnel.common.constants.MonitorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * 可监控的线程池
 * 继承ThreadPoolExecutor类，覆盖了shutdown(), shutdownNow(), beforeExecute() 和 afterExecute()方法来统计线程池的执行情况
 *
 * corePoolSize：
 * 线程池的基本大小，即在没有任务需要执行的时候线程池的大小，并且只有在工作队列满了并且数量小于MAX POOL SIZE的情况下才会创建超出这个数量的线程。
 * maximumPoolSize：
 * 线程池中允许的最大线程数，线程池中的当前线程数目不会超过该值。
 *
 * isShutDown: isShutDown当调用shutdown()或shutdownNow()方法后返回为true。
 * isTerminated: 当调用shutdown()方法后，并且所有提交的任务完成后返回为true; 当调用当调用shutdown()方法前调用isTerminated()永远返回false
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-24 21:52
 **/
public class MonitoredThreadPool extends ThreadPoolExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorConstants.THREAD_MONITOR_LOG_NAME);

    /**
     * 保存任务开始执行的时间，当任务结束时，用任务结束时间减去开始时间计算任务执行时间
     */
    private final ConcurrentHashMap<String, Long> startTimes;

    /**
     * 线程池名称，一般以业务名称命名，方便区分
     */
    private final String poolName;

    /**
     * 调用父类的构造方法，并初始化HashMap和线程池名称
     *
     * @param corePoolSize    线程池核心线程数
     * @param maximumPoolSize 线程池最大线程数
     * @param keepAliveTime   线程的最大空闲时间
     * @param unit            空闲时间的单位
     * @param workQueue       保存被提交任务的队列
     * @param poolName        线程池名称
     */
    public MonitoredThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                               TimeUnit unit, BlockingQueue<Runnable> workQueue, String poolName) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
            Executors.defaultThreadFactory(), poolName);
    }


    /**
     * 调用父类的构造方法，并初始化HashMap和线程池名称
     *
     * @param corePoolSize    线程池核心线程数
     * @param maximumPoolSize 线程池最大线程数
     * @param keepAliveTime   线程的最大空闲时间
     * @param unit            空闲时间的单位
     * @param workQueue       保存被提交任务的队列
     * @param threadFactory   线程工厂
     * @param poolName        线程池名称
     */
    public MonitoredThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                               TimeUnit unit, BlockingQueue<Runnable> workQueue,
                               ThreadFactory threadFactory, String poolName) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.startTimes = new ConcurrentHashMap<>();
        this.poolName = poolName;
    }

    /**
     * 线程池延迟关闭时（等待线程池里的任务都执行完毕），统计线程池情况
     */
    @Override
    public void shutdown() {
        // 统计已执行任务、正在执行任务、未执行任务数量
        LOGGER.info("{} 线程池即将关闭. 完成任务数量: {}, 正在执行的线程数量: {}, 待执行任务数量: {}",
            this.poolName, this.getCompletedTaskCount(), this.getActiveCount(), this.getQueue().size());
        super.shutdown();
    }

    /**
     * 线程池立即关闭时，统计线程池情况
     */
    @Override
    public List<Runnable> shutdownNow() {
        // 统计已执行任务、正在执行任务、未执行任务数量
        LOGGER.info("{} 线程池立即关闭. 完成任务数量: {}, 正在执行的线程数量: {}, 待执行任务数量: {}",
            this.poolName, this.getCompletedTaskCount(), this.getActiveCount(), this.getQueue().size());
        return super.shutdownNow();
    }

    /**
     * 任务执行之前，记录任务开始时间
     */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        startTimes.put(String.valueOf(r.hashCode()), System.currentTimeMillis());
    }

    /**
     * 任务执行之后，计算任务结束时间
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        Long startDate = startTimes.remove(String.valueOf(r.hashCode()));
        long finishDate = System.currentTimeMillis();
        LOGGER.info("{}-monitor: 任务耗时: {}秒", this.poolName, (finishDate - startDate) / 1000);
    }

    /**
     * 创建固定线程池，代码源于Executors.newFixedThreadPool方法，这里增加了poolName
     *
     * @param nThreads 线程数量
     * @param poolName 线程池名称
     * @return ExecutorService对象
     */
    public static ExecutorService newFixedThreadPool(int nThreads, String poolName) {
        return new MonitoredThreadPool(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), poolName);
    }

    /**
     * 创建缓存型线程池，代码源于Executors.newCachedThreadPool方法，这里增加了poolName
     *
     * @param poolName 线程池名称
     * @return ExecutorService对象
     */
    public static ExecutorService newCachedThreadPool(String poolName) {
        return new MonitoredThreadPool(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), poolName);
    }

}
