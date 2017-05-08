/**
 * 
 */
package com.yaowan.server.game.function;



import java.nio.ByteBuffer;
import java.util.Collection;

import org.springframework.stereotype.Component;

import com.google.protobuf.GeneratedMessageLite;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.cross.BasePacket;
import com.yaowan.cross.CrossPlayer;
import com.yaowan.cross.CrossPlayerContainer;
import com.yaowan.framework.netty.Message;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.cmd.CMD.CrossCMD;
import com.yaowan.server.game.model.data.entity.Role;



/**
 * @author zane
 *
 */

@Component
public class RoleFunction extends FunctionAdapter {

	public Player getPlayer(long rid) {
		return CrossPlayerContainer.get(rid);
	}

    /**
	 * 玩家是否在线
	 * @param rid
	 * @return
	 */
	public boolean isOnline(long rid) {
		Player player = getPlayer(rid);
		if(player != null && player.getRole() != null && player.getRole().getOnline()==1){
			return true;
		}
		return false;
	}
    
    
    public void sendMessageToPlayer(long rid, GeneratedMessageLite msg) {
    	CrossPlayer player = CrossPlayerContainer.get(rid);
	
		if(player == null) {
			return;
		}
		player.write(msg);
	}
	
	/**
	 * 群发消息
	 * @param rids
	 * @param message
	 */
	public void sendMessageToPlayers(Collection<Long> rids, GeneratedMessageLite msg) {
		Message message = Message.build(msg);
		for(Long rid : rids) {
			Player player = CrossPlayerContainer.get(rid);
			if(player == null) {
				continue;
			}
			player.write(message);
		}
	}
	
	/**
	 * 金币增加
	 */
	public boolean goldAdd(Role role, int gold, MoneyEvent moneyEvent,boolean update){
		if (gold < 0) {
			return false;
		}
		
		if (role.getGold() < 1000000000) {//不能超过十亿
			role.setGold(role.getGold() + gold);
		}
		
		CrossPlayer player = CrossPlayerContainer.get(role.getRid());
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putInt(moneyEvent.getValue());
		buffer.putInt(gold);
		BasePacket basePacket = new BasePacket((short)CrossCMD.GoldAdd_VALUE, 0, player.getId(), buffer.array());
		player.write(basePacket);
		
		return true;
	}

	/**
	 * 金币(减少)
	 */
	public boolean goldSub(Role role, int gold, MoneyEvent moneyEvent,boolean update) {
		if (gold < 0) {
			return false;
		}
		role.setGold(role.getGold() - gold);
	    if(role.getGold() <0){
	    	role.setGold(0);
	    }
		CrossPlayer player = CrossPlayerContainer.get(role.getRid());
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putInt(moneyEvent.getValue());
		buffer.putInt(gold);
		BasePacket basePacket = new BasePacket((short)CrossCMD.GoldSub_VALUE, 0, player.getId(), buffer.array());
		player.write(basePacket);

		return true;
	}	
}
