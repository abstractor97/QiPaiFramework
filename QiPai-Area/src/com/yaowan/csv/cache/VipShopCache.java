package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.VipShopCsv;

@Component
public class VipShopCache extends ConfigCache<VipShopCsv>{

	private Map<Integer, VipShopCsv> mapCache = new HashMap<>();

	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[] { "itemOrder" }, mapCache);
	}

	public VipShopCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_vipshop";
	}
}
