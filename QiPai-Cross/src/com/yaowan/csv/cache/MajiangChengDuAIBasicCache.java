/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MajiangChengDuAIBasicCsv;

/**
 * @author yangbin
 *
 */
@Component
public class MajiangChengDuAIBasicCache extends ConfigCache<MajiangChengDuAIBasicCsv> {

	private Map<Integer, MajiangChengDuAIBasicCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"RoomId"}, mapCache);
	}
	
	public MajiangChengDuAIBasicCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_majiang_chengdu_ai_basic_config";
	}

}
