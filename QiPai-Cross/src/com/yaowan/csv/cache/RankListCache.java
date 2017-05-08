package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.RankListCsv;

@Component
public class RankListCache extends ConfigCache<RankListCsv> {
	private Map<Integer, RankListCsv> mapCache = new HashMap<>();
	@Override
	public void loadIndexsMap() {
		String ID="ID";
		this.loadMap(new String[] {ID}, mapCache);
	}

	public RankListCsv getConfig(int id) {
		return mapCache.get(id);
	}
	@Override
	public String getFileName() {
		return "cfg_rankinglist";
	}
}