package com.yaowan.server.game.service;

import java.nio.ByteBuffer;

import org.springframework.stereotype.Component;

import com.yaowan.cross.BasePacket;
import com.yaowan.cross.CrossPlayer;
import com.yaowan.cross.CrossPlayerContainer;
import com.yaowan.protobuf.cmd.CMD.CenterCMD;
import com.yaowan.protobuf.cmd.CMD.CrossCMD;
import com.yaowan.server.game.main.NettyClient;

/**
 * 处理推送消息
 * @author YW0941
 *
 */
@Component
public class PushMessageService {
	/**
	 * 加入游戏
	 * @param rid
	 */
	public void pushJoinCrossGame(long rid,int gameType){
		CrossPlayer player = CrossPlayerContainer.get(rid);
		if(player == null){
			throw new RuntimeException("Player not in CrossServer , rid="+rid);
		}
		
		ByteBuffer buffer = ByteBuffer.allocate(5);
		buffer.put((byte)1);
		buffer.putInt(gameType);
		BasePacket basePacket = new BasePacket((short)CrossCMD.CrossGameJoinOrQuit_VALUE, 0, rid, buffer.array());
		player.write(basePacket);
		
		//推送消息给中心服
		NettyClient.request((short)CenterCMD.OnlineAction_VALUE,null);
	}
	/**
	 * 退出跨服游戏
	 * @param rid
	 */
	public void pushQuitCrossGame(long rid){
		CrossPlayer player = CrossPlayerContainer.get(rid);
		if(player == null){
			throw new RuntimeException("Player not in CrossServer , rid="+rid);
		}
		
		ByteBuffer buffer = ByteBuffer.allocate(1);
		buffer.put((byte)2);
		BasePacket basePacket = new BasePacket((short)CrossCMD.CrossGameJoinOrQuit_VALUE, 0, rid, buffer.array());
		player.write(basePacket);
	}
}
