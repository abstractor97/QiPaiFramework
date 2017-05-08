package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.PropsGiftCsv;

/**
 * @author lijintao
 * 2017年2月24日
 */
@Component
public class PropsGiftCache extends ConfigCache<PropsGiftCsv> {
	private Map<Integer, PropsGiftCsv> mapCache = new HashMap<>();

	@Override
	public void loadIndexsMap() {
		String expressionId="ExpressionId";
		this.loadMap(new String[] {expressionId}, mapCache);
	}

	public PropsGiftCsv getConfig(int id) {
		return mapCache.get(id);
	}

	@Override
	public String getFileName() {
		return "cfg_propsgift";
	}
}
