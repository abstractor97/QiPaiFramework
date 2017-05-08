package com.yaowan.cross;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.yaowan.cross.BasePacket;
import com.yaowan.cross.DispatchController;
import com.yaowan.framework.core.NamedThreadFactory;
import com.yaowan.framework.util.LogUtil;

public class OrderedPacketProcessor extends PacketProcessor {

	private final Lock lock = new ReentrantLock();
	private final Condition notEmpty = lock.newCondition();
	private final List<BasePacket> packets = new LinkedList<BasePacket>();
	private final List<Thread> threads = new ArrayList<Thread>();
	private final int minThreads;
	private final int maxThreads;
	private static final int REDUCE_THRESHOLD = 3;
	private static final int INCREASE_THRESHOLD = 50;
	private Thread checker;
	private volatile boolean stopped = false;
	private ThreadFactory threadFactory = new NamedThreadFactory(
			"OrderedPacketProcessor");

	protected OrderedPacketProcessor(int minThreads, int maxThreads) {
		if (minThreads <= 0)
			this.minThreads = 1;
		else
			this.minThreads = minThreads;
		if (maxThreads < this.minThreads)
			this.maxThreads = this.minThreads;
		else
			this.maxThreads = maxThreads;

		for (int i = 0; i < this.minThreads; i++) {
			newThread();
		}
		if (this.minThreads != this.maxThreads)
			startCheckerThread();
	}

	public void shutdown() {
		stopped = true;
		lock.lock();
		try {
			// 中断心跳
			checker.interrupt();
			// 中断线程池内的所有线程
			for (Thread t : threads) {
				t.interrupt();
				// 线程可能被阻塞，唤醒后中断
				notEmpty.signalAll();
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void pushPacket(BasePacket packet) {
		if (stopped)
			return;
		lock.lock();
		try {
			packets.add(packet);
			// 唤醒等待线程
			notEmpty.signal();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public BasePacket getAvailablePacket() {
		while (!Thread.interrupted()) {
			while (packets.isEmpty())
				notEmpty.awaitUninterruptibly();
			ListIterator<BasePacket> it = packets.listIterator();
			while (it.hasNext()) {
				BasePacket packet = it.next();
				if (packet.getCrossPlayer().tryLock()) {
					it.remove();
					return packet;
				}
			}
			// 当队列中所有的包都是一个连接发送过来的，阻塞在这里，当之前的一个包被处理后，一定要唤醒，否则只能等待新包过来时唤醒
			notEmpty.awaitUninterruptibly();
		}
		return null;
	}

	private void startCheckerThread() {
		checker = new Thread(new CheckerTask(),
				"OrderedExecutionHandler:Checker");
		checker.start();
	}

	/**
	 * 心跳检查，是否需要创建新线程或销毁线程
	 * @author YW0941
	 *
	 */
	private final class CheckerTask implements Runnable {
		private int lastSize = 0;

		public void run() {

			while (!Thread.interrupted()) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					break;
				}
	
				int size = packets.size();
				if (size <= lastSize) {
					if (size < REDUCE_THRESHOLD) {
						killThread();
					}
				} else if (size > lastSize) {
					if (size > INCREASE_THRESHOLD) {
						if (!newThread() && size > INCREASE_THRESHOLD * 3) {
							LogUtil.warn("["
									+ size
									+ "] packets are waiting for execution, "
									+ "you should increasing maxthreads of OrderedExecutionHandler");

						}
					}
				}
				lastSize = size;
			}
			LogUtil.debug("thread[" + Thread.currentThread().getName()
					+ "] exit");
		}
	}

	private synchronized boolean newThread() {
		if (threads.size() >= maxThreads)
			return false;
		Thread t = threadFactory.newThread(new ProcessorTask());
		LogUtil.debug("create new OrderedExecution thread:" + t.getName());
		threads.add(t);
		t.start();
		return true;
	}

	private synchronized void killThread() {
		if (threads.size() > minThreads) {
			Thread t = threads.remove(threads.size() - 1);
			t.interrupt();
			LogUtil.debug("kill OrderedExecution thread:" + t.getName());
		}
	}

	/**
	 * 抢占式
	 * @author YW0941
	 *
	 */
	private final class ProcessorTask implements Runnable {

		@Override
		public void run() {
			BasePacket packet = null;
			for (;;) {
				lock.lock();
				try {
					// 处理完一个消息，通知各线程再次争抢消息
					if (packet != null) {
						packet.getCrossPlayer().unlock();
						notEmpty.signalAll();
					}
					// 如果线程被中断，结束任务
					if (Thread.interrupted()) {
							LogUtil.debug("thread["
									+ Thread.currentThread().getName()
									+ "] exit");
						return;
					}
					packet = getAvailablePacket();
					// 只有线程被中断时才会发生
					if (packet == null)
						break;
				} finally {
					lock.unlock();
				}
				// 业务逻辑执行
				try {
					packet.setCrossPlayer(CrossPlayerContainer.get(packet.getRid()));
					DispatchController.get(packet.getCmd()).execute(packet.getCrossPlayer(), packet);
				} catch (Exception e) {
					LogUtil.error("Controller not exists!!! cmd="+packet.getCmd());
				}
			}
		}
	}

}
