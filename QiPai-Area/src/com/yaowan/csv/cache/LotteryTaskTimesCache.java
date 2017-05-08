package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.LotteryTaskTimesCsv;
@Component
public class LotteryTaskTimesCache  extends ConfigCache<LotteryTaskTimesCsv>{

	private Map<Integer, LotteryTaskTimesCsv> mapCache = new HashMap<>();

	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[] { "ascriptionGameId" }, mapCache);
	}

	public LotteryTaskTimesCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_lottery_task_times";
	}
	
}
