/**
 * 
 */
package com.yaowan.server.center.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.yaowan.model.struct.GameServer;

/**
 * @author zane
 *
 */
@Component
public class CRegisterFunction {
 
	private final ConcurrentMap<Integer, GameServer> gameServerMap = new ConcurrentHashMap<Integer, GameServer>();
	
	/**
	 * 被注册的跨区服务器列表
	 * GameType为键
	 */
	private ConcurrentMap<Integer, List<GameServer>> registedCrossServerMap = new ConcurrentHashMap<Integer, List<GameServer>>();

	public void registerGameServer(GameServer gameServer) {
		GameServer oGameServer = gameServerMap.putIfAbsent(gameServer.getServerId(), gameServer);
		if(oGameServer!=null){
			oGameServer.close();
		}
		Integer[] gameTypes = gameServer.getGameTypes();
		if(gameTypes == null){
			return;
		}
		for (Integer gameType : gameTypes) {
			if(!registedCrossServerMap.containsKey(gameType)){
				registedCrossServerMap.put(gameType, new ArrayList<GameServer>());
			}
			registedCrossServerMap.get(gameType).remove(oGameServer);
			registedCrossServerMap.get(gameType).add(gameServer);
		}
	}
	/**
	 * 获取跨区服的列表
	 * @param gameType
	 * @return
	 */
	public List<GameServer> getCrossGameServers(int gameType){
		return registedCrossServerMap.get(gameType);
	}

	public GameServer getGameServer(int serverId) {
		return gameServerMap.get(serverId);
	}

	public Map<Integer, GameServer> getGameServerMap() {
		return gameServerMap;
	}

}
