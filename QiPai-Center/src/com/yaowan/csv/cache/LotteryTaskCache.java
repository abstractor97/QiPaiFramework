package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.LotteryTaskCsv;
@Component
public class LotteryTaskCache extends ConfigCache<LotteryTaskCsv>{

	private Map<Integer, LotteryTaskCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[] { "lotteryTaskID" }, mapCache);
	}

	public LotteryTaskCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_lottery_task";
	}
}
