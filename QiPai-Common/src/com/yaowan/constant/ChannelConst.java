/**
 * 
 */
package com.yaowan.constant;

import io.netty.util.AttributeKey;

import com.yaowan.model.struct.GameServer;
import com.yaowan.model.struct.Player;

/**
 * @author huangyuyuan
 *
 */
public class ChannelConst {
	
	public static final AttributeKey<Player> PLAYER = AttributeKey.valueOf("Player");
	
	public static final AttributeKey<GameServer> GAMESERVER = AttributeKey.valueOf("GameServer");
}
