package com.yaowan.server.game.cross.controller;

import java.nio.ByteBuffer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.cross.BasePacket;
import com.yaowan.cross.Controller;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.cmd.CMD.CrossCMD;
import com.yaowan.server.game.cross.CrossFunction;
/**
 * 设置玩家在跨区游戏中， 或退出跨区游戏
 * @author YW0941
 *
 */
@Component
public class CrossGameJoinOrQuitController extends Controller<Player> {

	@Autowired
	private CrossFunction crossFunction;
	public CrossGameJoinOrQuitController() {
		super((short)CrossCMD.CrossGameJoinOrQuit_VALUE);
		
	}

	@Override
	public void execute(Player player, BasePacket packet) {
	
		byte[] bytes = packet.getData();
		if(bytes == null || bytes.length ==0){
			return;
		}
		
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		int flag = buffer.get();
		if(flag == 1){ //加入
			crossFunction.joinCrossGame(player.getId(),buffer.getInt());
		}else {//退出
			crossFunction.quitCrossGame(player.getId());
		}
		
	}

}
