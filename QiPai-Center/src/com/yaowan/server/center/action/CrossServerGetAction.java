package com.yaowan.server.center.action;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.protobuf.InvalidProtocolBufferException;
import com.yaowan.model.struct.GameServer;
import com.yaowan.protobuf.center.CRegister.CServerInfo;
import com.yaowan.protobuf.cmd.CMD.CenterCMD;
import com.yaowan.protobuf.game.GCenter.IntVar;
import com.yaowan.server.center.function.CGameFunction;
import com.yaowan.server.center.function.CRegisterFunction;
import com.yaowan.server.center.model.data.Server;
import com.yaowan.server.center.model.data.dao.ServerDao;

/**
 * 获取跨服游戏
 * @author YW0941
 *
 */
@Component
public class CrossServerGetAction extends CenterAction {

	@Autowired
	private CGameFunction cGameFunction;
	@Autowired
	private CRegisterFunction cRegisterFunction;
	public CrossServerGetAction() {
		super(CenterCMD.CrossServerGetAction_VALUE);
	}

	@Override
	public void execute(GameServer gameServer, byte[] data) {
		IntVar intVar;
		try {
			intVar = IntVar.parseFrom(data);
			int gameType = intVar.getVal();
			List<GameServer> gameServers = cRegisterFunction.getCrossGameServers(gameType);
			
			if(gameServers == null||gameServers.size() ==0){
				return;
			}
			Collections.sort(gameServers, new Comparator<GameServer>() {
				@Override
				public int compare(GameServer o1, GameServer o2) {
					return o1.getOnline().get() - o2.getOnline().get();
				}
			});
			//获取需要的服务器信息
			Server server = cGameFunction.getServer(gameServers.get(0).getServerId());
			GameServer crossGameServer = cRegisterFunction.getGameServer(server.getId());
			CServerInfo.Builder builder = CServerInfo.newBuilder();
			builder.setId(server.getId());
			builder.addAllGameTypes(Arrays.asList(crossGameServer.getGameTypes()));
			builder.setHost(server.getHost());
			builder.setPort(Integer.valueOf(server.getPort()));
			sendResponse(gameServer, builder.build());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		
	}

}
