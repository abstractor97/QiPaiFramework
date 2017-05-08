package com.yaowan.csv.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.ExchangeCsv;

@Component
public class ExchangeCache extends ConfigCache<ExchangeCsv> {

	
	private Map<Integer, ExchangeCsv> mapCache = new HashMap<>();
	
	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return "cfg_change";
	}
	
	@Override
	public void loadIndexsMap() {
		// TODO Auto-generated method stub
		this.loadMap(new String[]{ "GoodsId" }, mapCache);
	}
	public List<ExchangeCsv> getExchangeCsvList(){
		return this.getConfigList();
	}
	
	public ExchangeCsv getConfig(int GoodsId) {
		return mapCache.get(GoodsId);
	}

}
