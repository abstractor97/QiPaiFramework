package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.PromotionAwardCsv;

@Component
public class PromotionAwardCache extends ConfigCache<PromotionAwardCsv>{
	private Map<Integer, PromotionAwardCsv> mapCache = new HashMap<Integer, PromotionAwardCsv>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"id"}, mapCache);
	}
	
	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return "cfg_promotion_award";
	}
	
	public PromotionAwardCsv getConfig(int id) {
		return mapCache.get(id);
	}

}
