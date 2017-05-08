package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.DiamondShopCsv;

@Component
public class DiamondShopCache extends ConfigCache<DiamondShopCsv> {

	private Map<Integer, DiamondShopCsv> mapCache = new HashMap<>();

	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[] { "itemOrder" }, mapCache);
	}

	public DiamondShopCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_diamondshop";
	}
}
