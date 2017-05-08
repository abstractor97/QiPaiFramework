package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MatchCsv;

@Component
public class MatchCache extends ConfigCache<MatchCsv> {
	
	/**
	 * Key: matchType
	 */
	private Map<Integer, MatchCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"matchType"}, mapCache);
	}
	
	public MatchCsv getConfig(int matchType) {
		return mapCache.get(matchType);
	}
	
	@Override
	public String getFileName() {
		return "cfg_match";
	}
}
