package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MaJiangValueCsv;

@Component
public class MaJiangValueCache extends ConfigCache<MaJiangValueCsv> {

	private Map<Integer, MaJiangValueCsv> mapCache = new HashMap<>();
	
	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return "cfg_majiang_value";
	}
	
	public MaJiangValueCsv getConfig(int id) {
		return mapCache.get(id);
	}
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"huTypeId"}, mapCache);
	}
	
}
