package com.yaowan.server.center.quartz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.core.function.FunctionManager;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.scheduler.SchedulerBean;

/**
 * 定时调度器
 */
@Component
public class CenterQueueManager {

    @Autowired
    private SchedulerBean schedulerBean;
  
    
    
    public void start() {
        // 注册各个扫描线程
        submitQueueThread();
    }

    public void stop() {
        schedulerBean.cancel("MATCHING_THREAD");
    }
				
    static int index = 1;
    /**
     *
     */
    private void submitQueueThread() {

        // 游戏房间匹配线程
        schedulerBean.submit("MATCHING_THREAD", TimeUtil.ONE_SECOND * 2500, new Runnable() {
            @Override
            public void run() {         	
            }
        });
        
   
        

        // 每日的凌晨1秒开始的线程
        schedulerBean.submit("DAILY_THREAD",
                TimeUtil.getDateStartTime(TimeUtil.getDateAdd(1))
                - System.currentTimeMillis(), TimeUtil.ONE_DAY * 10000,
                new Runnable() {
            @Override
            public void run() {
            	FunctionManager.doHandleOnNextDay();
            }
        });
    }
}
