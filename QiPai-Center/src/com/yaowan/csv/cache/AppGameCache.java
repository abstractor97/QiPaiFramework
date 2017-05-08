/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.AppGameCsv;

/**
 * @author zane
 *
 */
@Component
public class AppGameCache extends ConfigCache<AppGameCsv> {

	private Map<Integer, AppGameCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"appId"}, mapCache);
	}

	/**
	 * 所有游戏
	 */
	public Set<Integer> allGame = new HashSet<Integer>();
	
	public AppGameCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public void loadAfterAllConfigReady() {
		for(Map.Entry<Integer, AppGameCsv> entry:mapCache.entrySet()){
			AppGameCsv appGameCsv =entry.getValue();
			for(Integer id:appGameCsv.getGameListList()){
				allGame.add(id);
			}
		}
	}
	
	@Override
	public String getFileName() {
		return "cfg_game_list";
	}
}
