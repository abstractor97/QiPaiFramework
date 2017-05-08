/**
 * 
 */
package com.yaowan.server.game.function;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.dao.MajiangDataDao;
import com.yaowan.server.game.model.data.entity.MajiangData;
import com.yaowan.server.game.model.data.entity.Role;

/**
 * @author zane
 *
 */
@Component
public class MajiangDataFunction extends FunctionAdapter {
	
	@Autowired
	private MajiangDataDao majiangDataDao;
	
	/**
	 * 缓存
	 */
	private final ConcurrentHashMap<Long, MajiangData> cacheMap = new ConcurrentHashMap<>();
	
	@Override
	public void handleOnNextDay() {
		int week = TimeUtil.getDayOfWeek();
		if(week==1){
			for(Map.Entry<Long, MajiangData> entry:cacheMap.entrySet()){
				MajiangData majiangDataDao = entry.getValue();
				majiangDataDao.setBigPairWeek(0);
				majiangDataDao.setCountWeek(0);
				majiangDataDao.setFirstWeek(0);
				majiangDataDao.setGangAndHuWeek(0);
				majiangDataDao.setMaxcontinueWeek(0);
				majiangDataDao.setMaxPowerWeek(0);
				majiangDataDao.setMaxWinWeek(0);
				majiangDataDao.setSameCardsWeek(0);
				majiangDataDao.setSevenWeek(0);
				majiangDataDao.setWinWeek(0);
			}
			majiangDataDao.resetWeek();
		}
		
	}
    
    public MajiangData getMajiangData(long rid) {
    	MajiangData data = cacheMap.get(rid);
    	if(data == null){
    		data = majiangDataDao.findByKey(rid);
    		if(data == null){
    			data = new MajiangData();
    			data.setRid(rid);
    			majiangDataDao.insert(data);
    		}
    		cacheMap.put(rid, data);
    	}
		return data;
	}
    
    /**
     * 两种情况：生涯和每周
     * @param rid
     * @param power 番数
     * @param maxWinWeek 最高	
     * @param success
     * @param isFirstWin 是否头彩
     */
    public void updateMajiangData(long rid,int power,int maxWinWeek,List<Integer> listHuType,boolean firstWeek,boolean success) {
    	MajiangData majiangData = getMajiangData(rid);
    	majiangData.setCountWeek(majiangData.getCountWeek() + 1);//总场数（每周)
		if(success){
			majiangData.setWinWeek(majiangData.getWinWeek() + 1);//胜场数（每周）	
			if(firstWeek){
				majiangData.setFirstWeek(majiangData.getFirstWeek()+1);
				majiangData.setFirstTotal(majiangData.getFirstTotal()+1);
			}
			//设置连胜
			majiangData.setMaxcontinue(majiangData.getMaxcontinue() + 1);//当前最高连胜					
			if(majiangData.getMaxcontinue() > majiangData.getMaxcontinueWeek()){
				majiangData.setMaxcontinueWeek(majiangData.getMaxcontinue());//设置当前最高连胜（每周）
			}
			if(majiangData.getMaxcontinueWeek() > majiangData.getMaxcontinueTotal()){
				majiangData.setMaxcontinueTotal(majiangData.getMaxcontinueWeek());//设置最高连胜
			}
			//设置连胜
			//设置赢柱
			if(maxWinWeek > majiangData.getMaxWinWeek()){
				majiangData.setMaxWinWeek(maxWinWeek);//最多赢柱（每周）
			}	
			if(majiangData.getMaxWinWeek() > majiangData.getMaxWinTotal()){
				majiangData.setMaxWinTotal(majiangData.getMaxWinWeek());//最多赢注（生涯）
			}
			//设置赢柱
			//设置最大番
			if(power>majiangData.getMaxPowerWeek()){
				majiangData.setMaxPowerWeek(power);
			}		
			if(majiangData.getMaxPowerWeek()>majiangData.getMaxcontinueWeek()){
				majiangData.setMaxPowerTotal(majiangData.getMaxPowerWeek());
			}
			//设置最大番
			majiangData.setWinTotal(majiangData.getWinTotal() + 1);//胜场数(生涯)
			
			//判定胡的类型，根据cfg_majiang_value表的HuTypeId
			for (Integer integer : listHuType) {
				switch (integer) {
				case 2://杠上花
					majiangData.setGangAndHuWeek(majiangData.getGangAndHuWeek()+1);//
					majiangData.setGangAndHuTotal(majiangData.getGangAndHuTotal()+1);//
					break;
				case 10://大对子
					majiangData.setBigPairWeek(majiangData.getBigPairWeek()+1);	
					majiangData.setBigPairTotal(majiangData.getBigPairTotal()+1);	
					break;
				case 13://清一色
					majiangData.setSameCardsWeek(majiangData.getSameCardsWeek()+1);
					majiangData.setSameCardsTotal(majiangData.getSameCardsTotal()+1);
					break;
				case 15://巧七对
					majiangData.setSevenWeek(majiangData.getSevenWeek()+1);
					majiangData.setSevenTotal(majiangData.getSevenTotal()+1);
					break;
				case 14://清大对子
					//清一色
					majiangData.setSameCardsWeek(majiangData.getSameCardsWeek()+1);
					majiangData.setSameCardsTotal(majiangData.getSameCardsTotal()+1);
					//大对子
					majiangData.setBigPairWeek(majiangData.getBigPairWeek()+1);	
					majiangData.setBigPairTotal(majiangData.getBigPairTotal()+1);	
					break;
				case 16://清巧七对
					//清一色
					majiangData.setSameCardsWeek(majiangData.getSameCardsWeek()+1);
					majiangData.setSameCardsTotal(majiangData.getSameCardsTotal()+1);
					//巧7对
					majiangData.setSevenWeek(majiangData.getSevenWeek()+1);
					majiangData.setSevenTotal(majiangData.getSevenTotal()+1);
					break;
				default:
					break;
				}
			}
					
		}else{
			majiangData.setMaxcontinue(0);//当前最高连胜
		}
		majiangData.setCountTotal(majiangData.getCountTotal() + 1);//总场数

		majiangDataDao.update(majiangData);
    }
    
    @Override
	public void handleOnRoleLogout(Role role) {
    	cacheMap.remove(role.getRid());
	}
	
}
