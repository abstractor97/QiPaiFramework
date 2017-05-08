/**
 * 
 */
package com.yaowan.core.base;

import java.io.File;
import java.util.Iterator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yaowan.ServerConfig;
import com.yaowan.framework.core.events.listener.EventListener;

/**
 * @author huangyuyuan
 *
 */
public class Spring {

	private static ApplicationContext appContext;

	public static void init() {
		appContext = new ClassPathXmlApplicationContext("file:" + ServerConfig.configPath0 + File.separator + "applicationContext.xml");
		
		Iterator<EventListener> iterator = appContext.getBeansOfType(EventListener.class).values().iterator();
		while (iterator.hasNext()) {
			EventListener eventListener = (EventListener) iterator.next();
			eventListener.addToEventHandlerAddListenerAdapter();
		}
	}

	public static Object getBean(String name) {
		return appContext.getBean(name);
	}

	public static <T> T getBean(String name, Class<T> type) {
		return appContext.getBean(name, type);
	}
	
	public static <T> T getBean(Class<T> type) {
		return appContext.getBean(type);
	}

	public static boolean containBean(String name) {
		return appContext.containsBean(name);
	}
}
