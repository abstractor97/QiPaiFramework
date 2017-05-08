package com.yaowan.server.game.controller;

import org.springframework.stereotype.Component;

import com.yaowan.cross.BasePacket;
import com.yaowan.cross.Controller;
import com.yaowan.cross.CrossPlayer;
import com.yaowan.framework.core.handler.server.IServerExecutor;
import com.yaowan.framework.core.handler.server.ServerDispatcher;
import com.yaowan.framework.netty.Message;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.protobuf.cmd.CMD.CrossCMD;
/**
 * 地区玩家请求分发
 * @author YW0941
 *
 */
@Component
public class RetransmissionController extends Controller<CrossPlayer> {

	public RetransmissionController() {
		super((short)CrossCMD.Retransmission_VALUE);
	}

	@Override
	public void execute(CrossPlayer player, BasePacket packet) {
		Message message = Message.build(packet.getGmsgID(), packet.getData());
		int protocol = message.getProtocol();
		
		IServerExecutor executor = ServerDispatcher.getExecutor(protocol);
		if (executor == null) {
			LogUtil.error("Executor " + protocol + " not found");
			return;
		}
		
		LogUtil.info("ServerExecutor -----------" + executor.getClass().getName()+">>>>>"+protocol);
	
		try {
			executor.execute(player, message.getMsgBody());
		} catch (Exception e) {
			LogUtil.error(e);
		}

	}



}
