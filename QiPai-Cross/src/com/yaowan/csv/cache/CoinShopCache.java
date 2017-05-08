package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.CoinShopCsv;

@Component
public class CoinShopCache extends ConfigCache<CoinShopCsv> {

	private Map<Integer, CoinShopCsv> mapCache = new HashMap<>();

	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[] { "itemOrder" }, mapCache);
	}

	public CoinShopCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_coinshop";
	}
}
