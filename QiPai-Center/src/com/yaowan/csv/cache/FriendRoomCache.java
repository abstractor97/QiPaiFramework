/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.FriendRoomCsv;

/**
 * @author zane
 *
 */
@Component
public class FriendRoomCache extends ConfigCache<FriendRoomCsv> {

	private Map<Integer, FriendRoomCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"GameType"}, mapCache);
	}
	
	public FriendRoomCsv getConfig(int gameType) {
		return mapCache.get(gameType);
	}

	@Override
	public String getFileName() {
		return "cfg_haoyou_room_collect";
	}

}
