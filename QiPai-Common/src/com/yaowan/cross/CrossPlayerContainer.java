package com.yaowan.cross;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * player对象容器
 * @author YW0941
 *
 */
public class CrossPlayerContainer {
	private static ConcurrentMap<Long, CrossPlayer> crossPlayerMap = new ConcurrentHashMap<Long, CrossPlayer>();
	
	public static void put(CrossPlayer player){
		crossPlayerMap.put(player.getId(), player);
	}
	
	public static void remove(CrossPlayer player){
		crossPlayerMap.remove(player.getId());
	}
	
	public static CrossPlayer get(long rid){
		if(!crossPlayerMap.containsKey(rid)){
			crossPlayerMap.putIfAbsent(rid, new CrossPlayer(rid));
		}
		return crossPlayerMap.get(rid);
	}
}
