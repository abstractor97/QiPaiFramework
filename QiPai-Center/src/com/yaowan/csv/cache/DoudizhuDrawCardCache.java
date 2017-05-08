package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.DoudizhuDrawCardCsv;

@Component
public class DoudizhuDrawCardCache extends ConfigCache<DoudizhuDrawCardCsv>{

	private Map<Integer, DoudizhuDrawCardCsv> mapCache = new HashMap<>();

	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[] { "drawCardId" }, mapCache);
	}

	public DoudizhuDrawCardCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_doudizhu_drawcard";
	}
}
