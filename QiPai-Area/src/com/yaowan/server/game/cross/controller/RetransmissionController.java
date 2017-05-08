package com.yaowan.server.game.cross.controller;

import org.springframework.stereotype.Component;

import com.yaowan.cross.BasePacket;
import com.yaowan.cross.Controller;
import com.yaowan.framework.netty.Message;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.cmd.CMD.CrossCMD;
/**
 * 从跨区服收到消息，然后转发消息给玩家
 * @author YW0941
 *
 */
@Component
public class RetransmissionController extends Controller<Player> {

	public RetransmissionController() {
		super((short)CrossCMD.Retransmission_VALUE);
	}

	@Override
	public void execute(Player player, BasePacket packet) {
		player.write(Message.build(packet.getGmsgID(), packet.getData()));
	}

}
