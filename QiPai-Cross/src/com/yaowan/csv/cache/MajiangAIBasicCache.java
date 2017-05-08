/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MajiangAIBasicCsv;

/**
 * @author zane
 *
 */
@Component
public class MajiangAIBasicCache extends ConfigCache<MajiangAIBasicCsv> {

	private Map<Integer, MajiangAIBasicCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"RoomId"}, mapCache);
	}
	
	public MajiangAIBasicCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_majiang_ai_basic_config";
	}

}
