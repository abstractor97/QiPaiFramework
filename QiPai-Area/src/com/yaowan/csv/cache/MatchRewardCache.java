package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MatchRewardCsv;
import com.yaowan.model.struct.ItemGet;

@Component
public class MatchRewardCache extends ConfigCache<MatchRewardCsv> {

	// TODO:
	private Map<Integer, List<MatchRewardCsv>> mapCache = new HashMap<>();
	private Map<Integer, List<List<ItemGet>>> rewardItemCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"rewardGroupId"}, mapCache);
	}
	
	@Override
	public String getFileName() {
		return "cfg_match_reward";
	}
}
