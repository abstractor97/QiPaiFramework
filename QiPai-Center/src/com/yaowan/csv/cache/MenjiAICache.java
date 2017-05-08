/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MenjiAICsv;

/**
 * @author zane
 *
 */
@Component
public class MenjiAICache extends ConfigCache<MenjiAICsv> {

	private Map<Integer, MenjiAICsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"AiId"}, mapCache);
	}
	
	public MenjiAICsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_menji_ai";
	}

}
