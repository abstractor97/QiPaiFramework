/**
 * 
 */
package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.AvatarCsv;

/**
 * @author zane
 *
 */
@Component
public class AvatarCache extends ConfigCache<AvatarCsv> {

	private Map<Integer, AvatarCsv> mapCache = new HashMap<>();
	
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{"characterId"}, mapCache);
	}

	
	
	public AvatarCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_avatar_config";
	}
}
