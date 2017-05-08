/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MajiangZhenXiongAIBasicCsv;

/**
 * @author yangbin
 *
 */
@Component
public class MajiangZhenXiongAIBasicCache extends ConfigCache<MajiangZhenXiongAIBasicCsv> {

	private Map<Integer, MajiangZhenXiongAIBasicCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"RoomId"}, mapCache);
	}
	
	public MajiangZhenXiongAIBasicCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_majiang_zhenxiong_ai_basic_config";
	}

}
