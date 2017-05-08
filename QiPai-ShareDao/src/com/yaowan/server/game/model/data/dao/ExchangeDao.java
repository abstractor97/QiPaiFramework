package com.yaowan.server.game.model.data.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.entity.Exchange;

@Repository
public class ExchangeDao extends SingleKeyDataDao<Exchange, Long>{

	public Map<Long, Exchange> findAllMap() {
		int time = TimeUtil.time();
		String sql = "select * from exchange where end_time >"+time+" and del_flag=0 order by item_order";
		Map<Long, Exchange> map = findForMap(sql);
		if(map == null){
			map = new ConcurrentHashMap<Long, Exchange>();
		}
		return map;
	}

	public void delById(long id) {
		String sql = "update exchange set del_flag=1 where id="+id;
		executeSql(sql);
	}
}
