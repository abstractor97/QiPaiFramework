package com.yaowan.server.game.controller;

import java.nio.ByteBuffer;

import org.springframework.stereotype.Component;

import com.yaowan.constant.MoneyEvent;
import com.yaowan.cross.BasePacket;
import com.yaowan.cross.Controller;
import com.yaowan.cross.CrossPlayer;
import com.yaowan.cross.CrossPlayerContainer;
import com.yaowan.framework.util.ObjectAndByteArrayUtil;
import com.yaowan.protobuf.cmd.CMD.CrossCMD;
import com.yaowan.server.game.model.data.entity.Role;
@Component
public class InitCrossPlayerController extends Controller<CrossPlayer> {

	
	public InitCrossPlayerController() {
		super((short)CrossCMD.InitCrossPlayer_VALUE);
		
	}

	@Override
	public void execute(CrossPlayer player, BasePacket packet) {
		byte[] roleBytes = packet.getData();
		Role role = (Role) ObjectAndByteArrayUtil.toObject(roleBytes);
		player.setRole(role);
	
		
//		//TODO 测试跨服扣减金币
//		int moneyEvent= MoneyEvent.DEZHOU.getValue();
//		int sub = 10;
//		ByteBuffer buffer = ByteBuffer.allocate(8);
//		buffer.putInt(moneyEvent);
//		buffer.putInt(sub);
//		BasePacket basePacket = new BasePacket((short)CrossCMD.GoldSub_VALUE, 0, player.getId(), buffer.array());
//		player.write(basePacket);
	}

}
