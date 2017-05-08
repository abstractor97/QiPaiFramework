package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.CumulativeDayCsv;

@Component
public class CumulativeDayCache extends ConfigCache<CumulativeDayCsv> {

	private Map<Integer, CumulativeDayCsv> mapCache = new HashMap<>();

	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[] { "Id" }, mapCache);
	}

	public CumulativeDayCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_cumulative_day";
	}

	//获取连续签到的奖励
	public String getDataByDays(int days) {
		for (Map.Entry<Integer, CumulativeDayCsv> entry : mapCache
				.entrySet()) {
			CumulativeDayCsv cumulativeDayCsv = entry.getValue();
			if (cumulativeDayCsv.getCumulativeDay() == days) {
				return cumulativeDayCsv.getRewardItem();
			}
		}
		return null;
	}
}
