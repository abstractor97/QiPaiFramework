

package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;



/**
 * 进入房间活跃日志
 */
@Table(name = "room_role_active", comment = "进入房间活跃日志 ")
@Index(names = {"rid","game_type","room_type"}, indexs = {"rid","game_type","room_type"})
public class RoomRoleActive {
	/**
	 * 角色ID
	 */
	@Column(comment = "角色ID")
	private long rid;
	/**
	 * 进入时间
	 */
	@Column(comment = "进入时间")
	private int joinTime;
	
	/**
	 * 
	 */
	@Column(comment = "游戏类型 1焖鸡 2斗地主 3麻将")
	private byte gameType;
	
	/**
	 * 
	 */
	@Column(comment = "房间类型 1低级房 2中级 3高级")
	private int roomType;
	
	/**
	 * 匹配时长(单位：秒)
	 */
	@Column(comment = "匹配时长(单位：秒)")
	private int matchTime;
	
	public long getRid() {
		return rid;
	}
	public void setRid(long rid) {
		this.rid = rid;
	}
	public int getJoinTime() {
		return joinTime;
	}
	public byte getGameType() {
		return gameType;
	}
	public int getRoomType() {
		return roomType;
	}
	public void setJoinTime(int joinTime) {
		this.joinTime = joinTime;
	}
	public void setGameType(byte gameType) {
		this.gameType = gameType;
	}
	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}
	public int getMatchTime() {
		return matchTime;
	}
	public void setMatchTime(int matchTime) {
		this.matchTime = matchTime;
	}
	
	
}

