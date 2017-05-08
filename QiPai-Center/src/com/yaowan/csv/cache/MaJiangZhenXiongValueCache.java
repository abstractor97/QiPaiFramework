package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MaJiangZhenXiongValueCsv;


/**
 * @author yangbin
 *
 */

@Component
public class MaJiangZhenXiongValueCache extends ConfigCache<MaJiangZhenXiongValueCsv> {

	private Map<Integer, MaJiangZhenXiongValueCsv> mapCache = new HashMap<>();
	
	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return "cfg_majiang_zhenxiong_value";
	}
	
	public MaJiangZhenXiongValueCsv getConfig(int id) {
		return mapCache.get(id);
	}
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"huTypeId"}, mapCache);
	}
	
}
