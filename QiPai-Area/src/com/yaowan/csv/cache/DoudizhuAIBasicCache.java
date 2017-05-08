/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.DoudizhuAIBasicCsv;

/**
 * @author zane
 *
 */
@Component
public class DoudizhuAIBasicCache extends ConfigCache<DoudizhuAIBasicCsv> {

	private Map<Integer, DoudizhuAIBasicCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"RoomId"}, mapCache);
	}
	
	public DoudizhuAIBasicCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_doudizhu_ai_basic_config";
	}

}
