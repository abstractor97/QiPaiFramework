/**
 * 
 */
package com.yaowan.server.center.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.AppGameCache;
import com.yaowan.server.center.model.data.Server;
import com.yaowan.server.center.model.data.dao.ServerDao;

/**
 * @author zane
 *
 */
@Component
public class CGameFunction extends FunctionAdapter{
	
	@Autowired
	private AppGameCache appGameCache;
	@Autowired
	private ServerDao serverDao;

	private Map<Integer, Server> serverMap = new ConcurrentHashMap<Integer, Server>();

	public long nextTime = 0;
	
	@Override
	public void handleOnServerStart() {
		getServerMap();
	}

	public Server getServer(int serverId) {
		//如果中心服一直开着， 新开了服，这时就需要从数据库中读取了
		if(!serverMap.containsKey(serverId)){
			Server server = serverDao.getServer(serverId);
			if(server !=null){
				serverMap.put(serverId, server);
			}
		}
		return serverMap.get(serverId);
	}

	public Map<Integer, Server> getServerMap() {
		if (System.currentTimeMillis() > nextTime) {
			nextTime = System.currentTimeMillis() + 600000;
			Map<Integer, Server> map = new ConcurrentHashMap<Integer, Server>();
			for (Server server : serverDao.findAll()) {
				map.put(server.getId(), server);
			}
			serverMap = map;
		}
		return serverMap;
	}
	

}
