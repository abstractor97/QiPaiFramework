/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.NiuniuRoomCsv;

/**
 * @author zane
 *
 */
@Component
public class NiuniuRoomCache extends ConfigCache<NiuniuRoomCsv> {

	private Map<Integer, NiuniuRoomCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"Id"}, mapCache);
	}

	
	public NiuniuRoomCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_niuniu_room_config";
	}
}
