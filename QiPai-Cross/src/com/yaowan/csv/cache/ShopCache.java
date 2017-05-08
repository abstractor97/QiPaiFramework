package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.ShopCsv;

@Component
public class ShopCache extends ConfigCache<ShopCsv> {
	private Map<Integer, ShopCsv> mapCache = new HashMap<>();

	@Override
	public void loadIndexsMap() {
		String goodsId="goodsId";
		this.loadMap(new String[] {goodsId}, mapCache);
	}

	public ShopCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_shop";
	}
}
