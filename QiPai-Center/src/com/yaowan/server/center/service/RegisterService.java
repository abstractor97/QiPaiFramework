/**
 * 
 */
package com.yaowan.server.center.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.GameServer;
import com.yaowan.protobuf.center.CRegister.CMsg_22001001;
import com.yaowan.protobuf.center.CRegister.CServerInfo;
import com.yaowan.server.center.function.CGameFunction;
import com.yaowan.server.center.function.CRegisterFunction;
import com.yaowan.server.center.model.data.Server;

/**
 * @author zane
 * 
 * 游戏服务器注册
 */
@Component
public class RegisterService {
	
	@Autowired
	private CRegisterFunction registerFunction;
	
	@Autowired
	private CGameFunction gameFunction;
	
	
	
	public void gameServerRegister(GameServer gameServer, int serverId,int online,List<Integer> gameTypes) {
		gameServer.setServerId(serverId);
		gameServer.getOnline().set(online);
		if(gameTypes!=null){
			Integer[] gt = new Integer[gameTypes.size()];
			gameTypes.toArray(gt);
			gameServer.setGameTypes(gt);
		}
		registerFunction.registerGameServer(gameServer);
		
		Server server = gameFunction.getServer(serverId);
		LogUtil.info("GameServer " + serverId + " registed");
		CMsg_22001001.Builder builder = CMsg_22001001.newBuilder();
		CServerInfo.Builder value = CServerInfo.newBuilder();
		value.setId(server.getId());
		value.setActorTable(server.getMysqlDataName());
		value.setHost(server.getHost());
		value.setHttpPort(server.getHttpPort());
		value.setLogTable(server.getMysqlLogName());
		value.setMysqlHost(server.getMysqlHost());
		value.setMysqlPasswd(server.getMysqlPasswd());
		value.setMysqlPort(server.getMysqlPort());
		value.setMysqlUser(server.getMysqlUser());
		value.setName(server.getName());
		value.setPlatform(server.getPlatform());
		value.setPort(Integer.valueOf(server.getPort()));
		builder.addServers(value);
		gameServer.write(builder.build());
	}
	

}
