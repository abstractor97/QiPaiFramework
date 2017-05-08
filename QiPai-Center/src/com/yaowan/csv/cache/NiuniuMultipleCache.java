/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.NiuniuMultipleCsv;

/**
 * @author zane
 *
 */
@Component
public class NiuniuMultipleCache extends ConfigCache<NiuniuMultipleCsv> {

	private Map<Integer, NiuniuMultipleCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"CardTypeId"}, mapCache);
	}

	
	public NiuniuMultipleCsv getConfig(int id) {
		return mapCache.get(id);
	}
 
	@Override
	public String getFileName() {
		return "cfg_niuniu_multiple";
	}
}
