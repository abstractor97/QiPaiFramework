package com.yaowan.game.qipai.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadPool {

	private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
	
	private static ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
	
	private static ExecutorService simpleThread = Executors.newSingleThreadExecutor();
	
	public static void execute(Runnable runnable){
		fixedThreadPool.execute(runnable);
	}
	
	public static void scheduled(Runnable runnable, long delay, TimeUnit type){
		scheduledThreadPool.schedule(runnable, delay, type);
	}
	
	public static void  simpleExecute(Runnable runnable) {
		simpleThread.execute(runnable);
	}
	
}
