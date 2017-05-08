package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.ItemCsv;

@Component
public class ItemCache extends ConfigCache<ItemCsv> {

	
	private Map<Integer, ItemCsv> mapCache = new HashMap<>();
	
	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return "cfg_item";
	}
	
	@Override
	public void loadIndexsMap() {
		// TODO Auto-generated method stub
		this.loadMap(new String[]{"ID"}, mapCache);
	}
	
	public ItemCsv getConfig(int id) {
		return mapCache.get(id);
	}

}
