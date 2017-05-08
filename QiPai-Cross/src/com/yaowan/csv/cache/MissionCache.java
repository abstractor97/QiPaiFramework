package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MissionCsv;
/**
 * 日常任务配表
 * @author YW0861
 *
 */
@Component
public class MissionCache extends ConfigCache<MissionCsv>{

	private Map<Integer, MissionCsv> mapCache = new HashMap<>();
	private Map<Integer, MissionCsv> mainTaskCache = new HashMap<>();
	

	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[] { "ID" }, mapCache);
	}

	public MissionCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public void loadAfterAllConfigReady() {
		if(mapCache == null){
			return ;
		}
		for(Integer key : mapCache.keySet()){
			MissionCsv csv = mapCache.get(key);
			if(csv != null && csv.getTaskType() == 1){
				mainTaskCache.put(key, csv);
			}
		}
	}
	
	
	public Map<Integer, MissionCsv> getMainTaskCache() {
		return mainTaskCache;
	}


	@Override
	public String getFileName() {
		return "cfg_mission";
	}
	
}
