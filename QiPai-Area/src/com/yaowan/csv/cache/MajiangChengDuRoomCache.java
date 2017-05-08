/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MajiangChengDuRoomCsv;

/**
 * @author yangbin
 *
 */
@Component
public class MajiangChengDuRoomCache extends ConfigCache<MajiangChengDuRoomCsv> {

	private Map<Integer, MajiangChengDuRoomCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"roomID"}, mapCache);
	}

	
	public MajiangChengDuRoomCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_majiang_chengdu_room_config";
	}
}
