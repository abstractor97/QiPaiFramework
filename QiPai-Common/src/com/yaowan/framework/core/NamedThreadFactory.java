package com.yaowan.framework.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory{
	private final ThreadGroup group;
	private final AtomicInteger counter = new AtomicInteger(1);
	
	public NamedThreadFactory(String name) {
		this.group = new ThreadGroup(name);
	}

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(group, r, group.getName() + counter.getAndIncrement(), 0);
	}
}
