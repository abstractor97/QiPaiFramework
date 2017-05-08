package com.yaowan.server.center.action;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.model.struct.GameServer;
import com.yaowan.protobuf.center.CGame;
import com.yaowan.protobuf.center.CGame.CServer;
import com.yaowan.protobuf.cmd.CMD.CenterCMD;
import com.yaowan.server.center.model.data.Server;
import com.yaowan.server.center.service.GameService;

@Component
public class ServerListAction extends CenterAction {
	@Autowired
	private GameService gameService;
	
	public ServerListAction() {
		super(CenterCMD.ServerListAction.getNumber());
	}

	@Override
	public void execute(GameServer gameServer, byte[] data) {
		
		Iterator<Server> iterator = gameService.getServers().iterator();
		
//		CGame.CServer cServer = CGame.CServer.parseFrom(bytesParam.toByteArray());
		
		CGame.CServerList.Builder builder = CGame.CServerList.newBuilder();
		CServer.Builder serverInfo = CServer.newBuilder();
		while (iterator.hasNext()) {
			Server server = (Server) iterator.next();
			serverInfo.setId(server.getId());
			serverInfo.setIp(server.getHost());
			serverInfo.setPort(Integer.valueOf(server.getPort()));
			//TODO  在线人数  ， 服务器类型
			
			builder.addServers(serverInfo.build());
		}
		
		sendResponse(gameServer,builder.build());

	}

}
