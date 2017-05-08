/**
 * 
 */
package com.yaowan.server.game.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.dao.DouniuDataDao;
import com.yaowan.server.game.model.data.entity.DouniuData;
import com.yaowan.server.game.model.data.entity.Role;

/**
 * @author zane
 *
 */
@Component
public class DouniuDataFunction extends FunctionAdapter {
	
	@Autowired
	private DouniuDataDao douniuDataDao;
	
	/**
	 * 缓存
	 */
	private final ConcurrentHashMap<Long, DouniuData> cacheMap = new ConcurrentHashMap<>();
	
	@Override
	public void handleOnNextDay() {
		int week = TimeUtil.getDayOfWeek();
		if(week==1){
			for(Map.Entry<Long, DouniuData> entry:cacheMap.entrySet()){
				DouniuData douniuData = entry.getValue();
				douniuData.setCountWeek(0);
				douniuData.setMaxcontinueWeek(0);
				douniuData.setMaxWinWeek(0);
				douniuData.setWinWeek(0);
			}
			douniuDataDao.resetWeek();
		}
		
	}
    
    public DouniuData getDouniuData(long rid) {
    	DouniuData data = cacheMap.get(rid);
    	if(data == null){
    		data = douniuDataDao.findByKey(rid);
    		if(data == null){
    			data = new DouniuData();
    			data.setRid(rid);
    			douniuDataDao.insert(data);
    		}
    		cacheMap.put(rid, data);
    	}
		return data;
	}
    
    public void updateDouniuData(long rid, int maxWinWeek,boolean success) {
    	DouniuData data = getDouniuData(rid);
    	data.setCountWeek(data.getCountWeek() + 1);
		if(success){
			data.setWinWeek(data.getWinWeek() + 1);
			data.setMaxcontinue(data.getMaxcontinue() + 1);
			if(data.getMaxcontinue() > data.getMaxcontinueWeek()){
				data.setMaxcontinueWeek(data.getMaxcontinue());
			}
			if(data.getMaxcontinueWeek() > data.getMaxcontinueTotal()){
				data.setMaxcontinueTotal(data.getMaxcontinueWeek());
			}
			if(maxWinWeek > data.getMaxWinWeek()){
				data.setMaxWinWeek(maxWinWeek);
			}
			
			if(data.getMaxWinWeek() > data.getMaxWinTotal()){
				data.setMaxWinTotal(data.getMaxWinWeek());
			}
			data.setWinTotal(data.getWinTotal() + 1);
			
		}else{
			data.setMaxcontinue(0);
		}
		data.setCountTotal(data.getCountTotal() + 1);
		
		douniuDataDao.update(data);
    }
    
    @Override
	public void handleOnRoleLogout(Role role) {
    	cacheMap.remove(role.getRid());
	}
    
	
}
