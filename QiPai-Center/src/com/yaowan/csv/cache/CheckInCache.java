package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.CheckInCsv;

@Component
public class CheckInCache extends ConfigCache<CheckInCsv> {

	private Map<Integer, CheckInCsv> mapCache = new HashMap<>();
	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return "cfg_check_in";
	}
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[] { "ID" }, mapCache);
	}

	public CheckInCsv getConfig(int id) {
		return mapCache.get(id);
	}

}
