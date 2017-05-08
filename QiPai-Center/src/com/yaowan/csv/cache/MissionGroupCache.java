/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MissionGroupCsv;
import com.yaowan.framework.util.Probability;

/**
 * @author zane
 *
 */
@Component
public class MissionGroupCache extends ConfigCache<MissionGroupCsv> {

	private Map<Integer, MissionGroupCsv> mapCache = new HashMap<>();
	
	
	private Map<MissionGroupCsv, Double> rateMap = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"ID"}, mapCache);
	}
	
	@Override
	public void loadAfterAllConfigReady() {
		for(MissionGroupCsv csv:getConfigList()){
			rateMap.put(csv, (double)csv.getProbability());
		}
	}

	
	public MissionGroupCsv getConfig(int id) {
		return mapCache.get(id);
	}

	
	public MissionGroupCsv rand() {
		return Probability.getRand(rateMap, 1);
	}
	
	@Override
	public String getFileName() {
		return "cfg_missiongroup";
	}
}
