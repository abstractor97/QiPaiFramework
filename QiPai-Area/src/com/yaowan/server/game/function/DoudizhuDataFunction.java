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
import com.yaowan.server.game.model.data.dao.DoudizhuDataDao;
import com.yaowan.server.game.model.data.entity.DoudizhuData;
import com.yaowan.server.game.model.data.entity.Role;

/**
 * @author zane
 *
 */
@Component
public class DoudizhuDataFunction extends FunctionAdapter {
	
	@Autowired
	private DoudizhuDataDao doudizhuDataDao;
	
	/**
	 * 缓存
	 */
	private final ConcurrentHashMap<Long, DoudizhuData> cacheMap = new ConcurrentHashMap<>();
	
	@Override
	public void handleOnNextDay() {
		int week = TimeUtil.getDayOfWeek();
		if(week==1){
			for(Map.Entry<Long, DoudizhuData> entry:cacheMap.entrySet()){
				DoudizhuData doudizhuData = entry.getValue();
				doudizhuData.setBombWeek(0);
				doudizhuData.setCountWeek(0);
				doudizhuData.setFarmerWeek(0);
				doudizhuData.setFarmerWinWeek(0);
				doudizhuData.setHighPowerWeek(0);
				doudizhuData.setHostWeek(0);
				doudizhuData.setHostWinWeek(0);
				doudizhuData.setKingBombWeek(0);
				doudizhuData.setMaxcontinueWeek(0);
				doudizhuData.setMaxWinWeek(0);
				doudizhuData.setWinWeek(0);
			}
			doudizhuDataDao.resetWeek();
		}
		
	}
    
    public DoudizhuData getDoudizhuData(long rid) {
    	DoudizhuData data = cacheMap.get(rid);
    	if(data == null){
    		data = doudizhuDataDao.findByKey(rid);
    		if(data == null){
    			data = new DoudizhuData();
    			data.setRid(rid);
    			doudizhuDataDao.insert(data);
    		}
    		cacheMap.put(rid, data);
    	}
		return data;
	}
    
    public void updateDoudizhuData(long rid,boolean isDizhu,int power,int maxWinWeek,boolean success,int isWangZha,int zhadan) {
    	DoudizhuData doudizhuData = getDoudizhuData(rid);
    	doudizhuData.setCountWeek(doudizhuData.getCountWeek() + 1);
    	doudizhuData.setCountTotal(doudizhuData.getCountTotal() + 1);
    	if(isDizhu){
    		doudizhuData.setHostWeek(doudizhuData.getHostWeek() + 1);
			doudizhuData.setHostTotal(doudizhuData.getHostTotal() + 1);
			if(success){
				doudizhuData.setHostWinWeek(doudizhuData.getHostWinWeek() + 1);
				doudizhuData.setHostWinTotal(doudizhuData.getHostWinTotal() + 1);
			}
    	}else{
    		if(success){
				doudizhuData.setFarmerWinWeek(doudizhuData.getFarmerWinWeek() + 1);
				doudizhuData.setFarmerWinTotal(doudizhuData.getFarmerWinTotal() + 1);
			}
    		doudizhuData.setFarmerWeek(doudizhuData.getFarmerWeek() + 1);
    		doudizhuData.setFarmerTotal(doudizhuData.getFarmerTotal() + 1);
    	}
    	if(power > doudizhuData.getHighPowerWeek()){
    		doudizhuData.setHighPowerWeek(power);
    	}
    	if(power > doudizhuData.getHighPowerTotal()){
    		doudizhuData.setHighPowerTotal(power);
    	}
    	doudizhuData.setKingBombWeek(doudizhuData.getKingBombWeek() + isWangZha);
		doudizhuData.setKingBombTotal(doudizhuData.getKingBombTotal() + isWangZha);
    	doudizhuData.setBombWeek(doudizhuData.getBombWeek() + zhadan);
    	doudizhuData.setBombTotal(doudizhuData.getBombTotal() + zhadan);
    	
		if(success){
			doudizhuData.setWinWeek(doudizhuData.getWinWeek() + 1);
			doudizhuData.setMaxcontinue(doudizhuData.getMaxcontinue() + 1);
			if(doudizhuData.getMaxcontinue() > doudizhuData.getMaxcontinueWeek()){
				doudizhuData.setMaxcontinueWeek(doudizhuData.getMaxcontinue());
			}
			if(doudizhuData.getMaxcontinueWeek() > doudizhuData.getMaxcontinueTotal()){
				doudizhuData.setMaxcontinueTotal(doudizhuData.getMaxcontinueWeek());
			}
			if(maxWinWeek > doudizhuData.getMaxWinWeek()){
				doudizhuData.setMaxWinWeek(maxWinWeek);
			}
			if(maxWinWeek > doudizhuData.getMaxWinTotal()){
				doudizhuData.setMaxWinTotal(maxWinWeek);
			}
			
			if(doudizhuData.getMaxWinWeek() > doudizhuData.getMaxWinTotal()){
				doudizhuData.setMaxWinTotal(doudizhuData.getMaxWinWeek());
			}
			doudizhuData.setWinTotal(doudizhuData.getWinTotal() + 1);
			
		}else{
			doudizhuData.setMaxcontinue(0);
		}
		
		doudizhuDataDao.update(doudizhuData);
    }
    
    @Override
	public void handleOnRoleLogout(Role role) {
		cacheMap.remove(role.getRid());
	}
	
}
