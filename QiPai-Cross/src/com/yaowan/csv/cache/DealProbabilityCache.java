package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.DealProbabilityCsv;

@Component
public class DealProbabilityCache extends ConfigCache<DealProbabilityCsv> {

	private Map<Integer, DealProbabilityCsv> mapCache = new HashMap<>();

	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[] { "DrawCardId" }, mapCache);
	}

	public DealProbabilityCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_deal_probability";
	}
	
}
