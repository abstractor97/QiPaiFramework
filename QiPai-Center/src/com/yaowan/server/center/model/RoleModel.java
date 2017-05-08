package com.yaowan.server.center.model;

import com.yaowan.server.game.model.data.entity.Role;

/**
 * 大厅玩家信息
 * 
 * @author KKON
 *
 */
public class RoleModel {

	/**
	 * 玩家ID
	 */
	private long rid;

	/**
	 * 准备的游戏类型
	 */
	private int realType;

	/**
	 * 玩家信息
	 */
	private Role role;

	/**
	 * 房间ID
	 */
	private long roomId;

	public long getRid() {
		return rid;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public int getRealType() {
		return realType;
	}

	public void setRealType(int realType) {
		this.realType = realType;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public long getRoomId() {
		return roomId;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}

}
