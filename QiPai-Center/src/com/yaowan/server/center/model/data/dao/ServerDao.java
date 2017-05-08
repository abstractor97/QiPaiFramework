package com.yaowan.server.center.model.data.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.server.center.model.data.Server;

@Component
public class ServerDao extends SingleKeyDataDao<Server,Integer> {
	
	/**
	 * 获取一个服务器配置
	 * @param serverId
	 * @return
	 */
	public Server getServer(int serverId){
		String sql = String.format("select * from server where id=%d", serverId);
		return find(sql);
	}
	/**
	 * 根据游戏类型获取列表
	 * @param gameType
	 * @return
	 */
	public List<Server> getServers(int gameType){
		String sql = String.format("select * from server where game_type=%d", gameType);
		return findList(sql);
	}
}
