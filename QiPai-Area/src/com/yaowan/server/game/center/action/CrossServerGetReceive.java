package com.yaowan.server.game.center.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.protobuf.InvalidProtocolBufferException;
import com.yaowan.protobuf.center.CRegister.CServerInfo;
import com.yaowan.protobuf.cmd.CMD.CenterCMD;
import com.yaowan.server.game.center.Receive;
import com.yaowan.server.game.cross.CrossFunction;
import com.yaowan.server.game.cross.CrossGameClient;
/**
 * 从中心服上获取跨服服务器的信息，并建立与跨服服务器的连接
 * @author YW0941
 *
 */
@Component
public class CrossServerGetReceive extends Receive {

	@Autowired
	private CrossFunction crossFunction;
	
	public CrossServerGetReceive() {
		super(CenterCMD.CrossServerGetAction_VALUE);
	}

	@Override
	public void execute(byte[] data) {
		try {
			CServerInfo serverInfo = CServerInfo.parseFrom(data);
			if(crossFunction.getCrossGameClient(serverInfo.getId()) == null){
				synchronized (CrossServerGetReceive.class) {
					if(crossFunction.getCrossGameClient(serverInfo.getId()) == null){
						CrossGameClient crossGameClient = new CrossGameClient(serverInfo.getHost(), serverInfo.getPort(),serverInfo.getGameTypesList());
						crossGameClient.connectToCrossGame();
						crossFunction.putCrossGameClient(serverInfo.getId(), crossGameClient);
					}
				}
			}
			crossFunction.executeRetransmission(serverInfo.getId(),crossFunction.getCrossGameClient(serverInfo.getId()));
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

	}

}
