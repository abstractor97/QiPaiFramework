/**
 * 
 */
package com.yaowan.model.struct;

import io.netty.channel.Channel;

import com.yaowan.framework.core.model.AbstractPlayer;
import com.yaowan.server.game.model.data.entity.Role;

/**
 * @author zane
 *
 */
public class Player extends AbstractPlayer{
	
	private Role role;
	//保存所在跨服游戏服的serverId, 默认为0
	private int crossServerId;
	public Player(Channel channel) {
		super(channel);
	}
	@Override
	public long getId() {
		if(role != null) {
			return role.getRid();
		}
		return 0;
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public int getCrossServerId() {
		return crossServerId;
	}
	public void setCrossServerId(int crossServerId) {
		this.crossServerId = crossServerId;
	}
	
	
}
