/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.ExpCsv;

/**
 * @author zane
 *
 */
@Component
public class ExpCache extends ConfigCache<ExpCsv> {

	private Map<Integer, ExpCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"lv"}, mapCache);
	}

	
	public ExpCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_exp";
	}
}
