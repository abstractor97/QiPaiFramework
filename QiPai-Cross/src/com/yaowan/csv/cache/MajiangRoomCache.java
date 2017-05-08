/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MajiangRoomCsv;

/**
 * @author zane
 *
 */
@Component
public class MajiangRoomCache extends ConfigCache<MajiangRoomCsv> {

	private Map<Integer, MajiangRoomCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"roomID"}, mapCache);
	}

	
	public MajiangRoomCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_majiang_room_config";
	}
}
