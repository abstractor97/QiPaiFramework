package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MenjiAIVictoryCsv;

@Component
public class MenjiAIVictoryCache extends ConfigCache<MenjiAIVictoryCsv>{

	private Map<Integer, MenjiAIVictoryCsv> mapCache = new HashMap<>();

	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[] { "VictoryId" }, mapCache);
	}

	public MenjiAIVictoryCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_menji_ai_victory";
	}
}
