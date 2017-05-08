/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MenjiAICollectCsv;

/**
 * @author zane
 *
 */
@Component
public class MenjiAICollectCache extends ConfigCache<MenjiAICollectCsv> {

	private Map<Integer, MenjiAICollectCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"roomId"}, mapCache);
	}
	
	public MenjiAICollectCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_menji_ai_collect";
	}

}
