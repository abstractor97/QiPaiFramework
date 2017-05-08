/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.HeadPortraitCsv;

/**
 * @author zane
 *
 */
@Component
public class HeadPortraitCache extends ConfigCache<HeadPortraitCsv> {

	private Map<Integer, HeadPortraitCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"ID"}, mapCache);
	}

	
	
	public HeadPortraitCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_headportrait";
	}
}
