package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.PoChanCsv;

@Component
public class PoChanCache extends ConfigCache<PoChanCsv> {

	private Map<Integer, PoChanCsv> mapCache = new HashMap<>();
	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return "cfg_pochan";
	}
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[] { "Id" }, mapCache);
	}
		
	public PoChanCsv getConfig(int id)
	{
		return mapCache.get(id);
	}
}
