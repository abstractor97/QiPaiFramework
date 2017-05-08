package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MaJiangChengDuValueCsv;
import com.yaowan.csv.entity.MenjiAIConfigCsv;


/**
 * @author yangbin
 *
 */

@Component
public class MenjiAIConfigCache extends ConfigCache<MenjiAIConfigCsv> {

	private Map<Integer, MenjiAIConfigCsv> mapCache = new HashMap<>();
	
	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return "cfg_menji_ai_config";
	}
	
	public MenjiAIConfigCsv getConfig(int id) {
		return mapCache.get(id);
	}
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"RoomID"}, mapCache);
	}
	
}
