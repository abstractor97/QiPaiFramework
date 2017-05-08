/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MenjiRoomCsv;

/**
 * @author zane
 *
 */
@Component
public class MenjiRoomCache extends ConfigCache<MenjiRoomCsv> {

	private Map<Integer, MenjiRoomCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"roomID"}, mapCache);
	}

	
	public MenjiRoomCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_menji_room_config";
	}
}
