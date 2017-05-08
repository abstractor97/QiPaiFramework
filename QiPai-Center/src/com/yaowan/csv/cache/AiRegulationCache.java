package com.yaowan.csv.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.AiRegulationCsv;

@Component
public class AiRegulationCache extends ConfigCache<AiRegulationCsv>{
	
	private Map<Integer, AiRegulationCsv> mapCache = new HashMap<>();
	private Map<Integer, List<AiRegulationCsv>> aiRegulationCache = new HashMap<>();
	
	@Override
	public String getFileName() {
		return "cfg_ai_regulation";
	}
	@Override
	public void loadIndexsMap() {
		// TODO Auto-generated method stub
		this.loadMap(new String[]{ "regulationID" }, mapCache);
	}
	
	public AiRegulationCsv getConfig(int id) {
		return mapCache.get(id);
	}
	
	@Override
	public void loadAfterAllConfigReady() {
		for(Integer key : mapCache.keySet()){
			AiRegulationCsv csv = mapCache.get(key);
			csv.setHighestValue(csv.getControlMoney()*(csv.getHighestLine()/10000f));
			csv.setMinimumValue(csv.getControlMoney()*(csv.getMinimumLine()/10000f));
			int realType = getRealType(csv.getAscriptionGameId(), csv.getRoomID());
			List<AiRegulationCsv> list =  aiRegulationCache.get(realType);
			if(list == null){
				list = new ArrayList<>();
			}
			list.add(csv);
			aiRegulationCache.put(realType, list);
		}
	}
	
	private int getRealType(int gameType, int roomType) {
		return gameType * 10000000 + roomType;
	}
	
	public Map<Integer, List<AiRegulationCsv>> getAiRegulationCache() {
		return aiRegulationCache;
	}
	
}
