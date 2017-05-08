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
import com.yaowan.protobuf.game.GMenJi;
import com.yaowan.protobuf.game.GMenJi.MJCardType;
import com.yaowan.server.game.model.data.dao.MenjiDataDao;
import com.yaowan.server.game.model.data.entity.MenjiData;
import com.yaowan.server.game.model.data.entity.Role;

/**
 * @author zane
 *
 */
@Component
public class MenjiDataFunction extends FunctionAdapter {
	
	@Autowired
	private MenjiDataDao menjiDataDao;
	
	/**
	 * 缓存
	 */
	private final ConcurrentHashMap<Long, MenjiData> cacheMap = new ConcurrentHashMap<>();
	
	@Override
	public void handleOnNextDay() {
		int week = TimeUtil.getDayOfWeek();
		if(week==1){
			for(Map.Entry<Long, MenjiData> entry:cacheMap.entrySet()){
				MenjiData menjiData = entry.getValue();
				menjiData.setBaoziWeek(0);
				menjiData.setCountWeek(0);
				menjiData.setMaxcontinueWeek(0);
				menjiData.setMaxWinWeek(0);
				menjiData.setShunziWeek(0);
				menjiData.setTonghuashunWeek(0);
				menjiData.setTonghuaWeek(0);
				menjiData.setWinWeek(0);
			}
			menjiDataDao.resetWeek();
		}
		
	}
    
    public MenjiData getMenjiData(long rid) {
    	MenjiData data = cacheMap.get(rid);
    	if(data == null){
    		data = menjiDataDao.findByKey(rid);
    		if(data == null){
    			data = new MenjiData();
    			data.setRid(rid);
    			menjiDataDao.insert(data);
    		}
    		cacheMap.put(rid, data);
    	}
		return data;
	}
    
    public void updateMenjiData(long rid,MJCardType mjCardType,int maxWinWeek,boolean success) {
    	MenjiData menjiData = getMenjiData(rid);
		menjiData.setCountWeek(menjiData.getCountWeek() + 1);
		if(success){
			menjiData.setWinWeek(menjiData.getWinWeek() + 1);
			menjiData.setMaxcontinue(menjiData.getMaxcontinue() + 1);
			if(menjiData.getMaxcontinue() > menjiData.getMaxcontinueWeek()){
				menjiData.setMaxcontinueWeek(menjiData.getMaxcontinue());
			}
			if(menjiData.getMaxcontinueWeek() > menjiData.getMaxcontinueTotal()){
				menjiData.setMaxcontinueTotal(menjiData.getMaxcontinueWeek());
			}
			if(maxWinWeek > menjiData.getMaxWinWeek()){
				menjiData.setMaxWinWeek(maxWinWeek);
			}
			
			if(menjiData.getMaxWinWeek() > menjiData.getMaxWinTotal()){
				menjiData.setMaxWinTotal(menjiData.getMaxWinWeek());
			}
			menjiData.setWinTotal(menjiData.getWinTotal() + 1);
			
		}else{
			menjiData.setMaxcontinue(0);
		}
		menjiData.setCountTotal(menjiData.getCountTotal() + 1);
		if(mjCardType.equals(GMenJi.MJCardType.BAO_ZI)){
			menjiData.setBaoziTotal(menjiData.getBaoziTotal() + 1);
			menjiData.setBaoziWeek(menjiData.getBaoziWeek() + 1);
		}else if(mjCardType.equals(GMenJi.MJCardType.TONG_HUA_SHUN)){
			menjiData.setTonghuashunWeek(menjiData.getTonghuashunWeek() + 1);
			menjiData.setTonghuashunTotal(menjiData.getTonghuashunTotal() + 1);
		}else if(mjCardType.equals(GMenJi.MJCardType.JIN_HUA)){
			menjiData.setTonghuaTotal(menjiData.getTonghuaTotal() + 1);
			menjiData.setTonghuaWeek(menjiData.getTonghuaWeek() + 1);
		}else if(mjCardType.equals(GMenJi.MJCardType.SHUN_ZI)){
			menjiData.setShunziTotal(menjiData.getShunziTotal() + 1);
			menjiData.setShunziWeek(menjiData.getShunziWeek() + 1);
		}
		menjiDataDao.update(menjiData);
    }
    
    @Override
	public void handleOnRoleLogout(Role role) {
    	cacheMap.remove(role.getRid());
	}
    
	
}
