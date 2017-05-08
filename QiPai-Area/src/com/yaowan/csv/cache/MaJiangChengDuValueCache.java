package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MaJiangChengDuValueCsv;


/**
 * @author yangbin
 *
 */

@Component
public class MaJiangChengDuValueCache extends ConfigCache<MaJiangChengDuValueCsv> {

	private Map<Integer, MaJiangChengDuValueCsv> mapCache = new HashMap<>();
	
	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return "cfg_majiang_chengdu_value";
	}
	
	public MaJiangChengDuValueCsv getConfig(int id) {
		return mapCache.get(id);
	}
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"huTypeId"}, mapCache);
	}
	
}
