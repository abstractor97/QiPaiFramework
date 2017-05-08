/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MenjiCardValueCsv;

/**
 * @author zane
 *
 */
@Component
public class MenjiCardValueCache extends ConfigCache<MenjiCardValueCsv> {

	private Map<Integer, MenjiCardValueCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"CardId"}, mapCache);
	}
	
	public MenjiCardValueCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_menji_card_value";
	}

}
