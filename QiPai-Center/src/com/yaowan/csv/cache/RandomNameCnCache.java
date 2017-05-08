package com.yaowan.csv.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.RandomNameCnCsv;

@Component
public class RandomNameCnCache extends ConfigCache<RandomNameCnCsv>{

	private Map<Integer, RandomNameCnCsv> mapCache = new HashMap<>();
	
	private Map<Byte, List<RandomNameCnCsv>> listMapCache;

	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[] { "key" }, mapCache);
	}

	public RandomNameCnCsv getConfig(int id) {
		return mapCache.get(id);
	}

	public List<RandomNameCnCsv> getRandomNameList(byte type) {
		if(listMapCache.containsKey(type)) {
			return listMapCache.get(type);
		}
		return null;
	}
	
	@Override
	public String getFileName() {
		return "RandomNameCn";
	}
	
	@Override
	protected void loadOther() {
		Map<Byte, List<RandomNameCnCsv>> tempMap = new HashMap<>();
		for(RandomNameCnCsv randomName : this.getConfigList()) {
			List<RandomNameCnCsv> list = null;
			if(tempMap.containsKey(randomName.getType())) {
				list = tempMap.get(randomName.getType());
			} else {
				list = new ArrayList<>();
				tempMap.put(randomName.getType(), list);
			}
			list.add(randomName);
		}
		listMapCache = tempMap;
	}
}
