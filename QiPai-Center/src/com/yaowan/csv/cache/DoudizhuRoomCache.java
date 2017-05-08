/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.DoudizhuRoomCsv;

/**
 * @author zane
 *
 */
@Component
public class DoudizhuRoomCache extends ConfigCache<DoudizhuRoomCsv> {

	private Map<Integer, DoudizhuRoomCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"roomID"}, mapCache);
	}

	
	public DoudizhuRoomCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_doudizhu_room_config";
	}
}
