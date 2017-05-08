/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MenjiAIBasicCsv;

/**
 * @author zane
 *
 */
@Component
public class MenjiAIBasicCache extends ConfigCache<MenjiAIBasicCsv> {

	private Map<Integer, MenjiAIBasicCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"RoomId"}, mapCache);
	}
	
	public MenjiAIBasicCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_menji_ai_basic_config";
	}

}
