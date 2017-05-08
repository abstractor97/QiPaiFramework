/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.MajiangZhenXiongRoomCsv;

/**
 * @author yangbin
 *
 */
@Component
public class MajiangZhenXiongRoomCache extends ConfigCache<MajiangZhenXiongRoomCsv> {

	private Map<Integer, MajiangZhenXiongRoomCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"roomID"}, mapCache);
	}

	
	public MajiangZhenXiongRoomCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_majiang_zhenxiong_room_config";
	}
}
