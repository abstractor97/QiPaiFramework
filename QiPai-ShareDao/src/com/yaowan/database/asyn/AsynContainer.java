package com.yaowan.database.asyn;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.yaowan.framework.util.LogUtil;
import com.yaowan.server.game.model.log.dao.AsynLogDao;



/**
 * 异步数据库
 * @author zane 2016年10月14日 下午3:00:36
 *
 */
public class AsynContainer {

	private static AtomicInteger atomicNum = new AtomicInteger(0);

	private static AsynContainer[] asynList = new AsynContainer[4];
	
	private static AsynLogDao asynLogDao = null;

	public static int getThreadNum() {
		return asynList.length;
	}
	/**
	 * 待插入到日志的队列
	 */
	private ConcurrentLinkedQueue<List<String>> readyQueue = new ConcurrentLinkedQueue<>();
	/**
	 * 空闲的日志对象
	 */
	private static ConcurrentLinkedQueue<List<String>> idleQueue = new ConcurrentLinkedQueue<>();
	/**
	 * 空闲的对象个数
	 */
	private static final int idleCount = 500;

	private boolean inited = false;

	private void start() {
		this.inited = true;
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				executeOnce();
			}

		}, 1000, 1000);
	}

	private void executeOnce() {
		int size = readyQueue.size();
		if (size <= 0) {
			return;
		}
		for (int i = 0; i < size; i++) {
			List<String> query = readyQueue.poll();
			if (query == null) {
				continue;
			}
			try {
				
				asynLogDao.executeSql(query);
				if(idleQueue.size() < idleCount) {
					query.clear();
					idleQueue.add(query);
				}
			} catch (Exception e) {
				LogUtil.info(query.toString());
				e.printStackTrace();
			}
		}
	}

	/**
	 * 记录日志
	 * @param query 插入的SQL
	 */
	public static void add(String query) {
		List<String> list =getQuery();
		list.add(query);
		add(list);
	}

	/**
	 * 记录日志
	 * @param query 插入的SQL
	 */
	public static void add(List<String> query) {

		int num = atomicNum.incrementAndGet();

		if (num >= Integer.MAX_VALUE) {
			num = Integer.MIN_VALUE;
			atomicNum.set(num);
		}

		int key = num % asynList.length;
		key = Math.abs(key);

		AsynContainer container = asynList[key];
		if (container == null) {
			container = new AsynContainer();
			asynList[key] = container;
		}

		container.readyQueue.add(query);
		if (!container.inited) {
			container.start();
		}
	}
	
	private static List<String> getQuery() {
		List<String> query = idleQueue.poll();
		if(query == null) {
			query = new ArrayList<String>();
		}else{
			query.clear();
		}
		return query;
	}

	public static AsynLogDao getAsynLogDao() {
		return asynLogDao;
	}

	public static void setAsynLogDao(AsynLogDao asynLogDao) {
		AsynContainer.asynLogDao = asynLogDao;
	}
}
