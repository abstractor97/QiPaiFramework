package com.yaowan.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.yaowan.core.base.GlobalConfig;
import com.yaowan.framework.util.LogUtil;

/**
 * 定时任务管理器
 * 
 * @author pvsp
 * 
 */
@Component
public class SchedulerBean {

	/**
	 * 所有任务
	 */
	private Map<String, Future<?>> tasks = new ConcurrentHashMap<String, Future<?>>();

	/**
	 * 定时任务线程池
	 */
	private ScheduledExecutorService executors;
	
	
	public SchedulerBean() {
		init();
	}
	
	public void init() {
		// CPU数量
		int availableProcessors = Runtime.getRuntime().availableProcessors()
				* GlobalConfig.quartzThreadSize;
		if(availableProcessors == 0) {
			availableProcessors = 1;
		}
		executors = Executors.newScheduledThreadPool(availableProcessors,
				new ThreadFactory() {
					AtomicInteger sn = new AtomicInteger();

					public Thread newThread(Runnable r) {
						SecurityManager s = System.getSecurityManager();
						ThreadGroup group = (s != null) ? s.getThreadGroup()
								: Thread.currentThread().getThreadGroup();
						Thread t = new Thread(group, r);
						t.setName("scheduled - " + sn.incrementAndGet());
						return t;
					}
				});
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				executors.shutdown();
			}
		}));
	}

	private Runnable newTask(final String taskId, final Runnable task,
			final boolean removeAfterExecute) {
		return new Runnable() {
			public void run() {
				try {
					task.run();
				} catch (Throwable e) {
					LogUtil.error("SchedulerTask", e);
				}
				if (removeAfterExecute) {
					tasks.remove(taskId);
				}
			}
		};
	}

	/**
	 * 
	 * @param taskId
	 * @param interval 间隔时间
	 * @param task
	 */
	public void submit(final String taskId, int interval,
			final Runnable task) {
		cancel(taskId);
		ScheduledFuture<?> future = executors.scheduleWithFixedDelay(
				newTask(taskId, task, false), 0, interval, TimeUnit.MILLISECONDS);
		if (!future.isDone() && !future.isCancelled()) {
			tasks.put(taskId, future);
		}
	}
	
	/**
	 * 提交定时任务
	 * 
	 * @param delay
	 *            延迟时间(单位为毫秒)
	 * @param interval
	 *            间隔时间(MS)
	 * @param task
	 *            运行任务
	 * @return 定时任务ID
	 */
	public void submit(final String taskId, long delay, long interval,
			final Runnable task) {
		cancel(taskId);
		ScheduledFuture<?> future = executors.scheduleWithFixedDelay(newTask(
				taskId, task, false), delay, interval, TimeUnit.MILLISECONDS);
		if (!future.isDone() && !future.isCancelled()) {
			tasks.put(taskId, future);
		}

	}

	public void cancel(String taskId) {
		if (taskId != null) {
			if (tasks.containsKey(taskId)) {
				Future<?> future = tasks.get(taskId);
				future.cancel(false);
				tasks.remove(taskId);
			}
		}
	}
	
	public void execute(Runnable command){
		executors.execute(command);
	}
}
