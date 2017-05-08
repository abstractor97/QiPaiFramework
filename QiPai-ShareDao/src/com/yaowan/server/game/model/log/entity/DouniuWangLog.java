package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;

/**
 * 斗牛坐庄日志
 * 
 * @author zane 2016年12月29日 下午9:28:54
 */
@Table(name = "douniu_wang_log", comment = "斗牛坐庄日志")
public class DouniuWangLog {
	/**
	 * 牌局ID
	 */
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "牌局ID")
	private long id;

	/**
	 * G_T_C 房间id
	 */
	@Column(comment = "房间号")
	private long roomId;
	
	/**
	 * G_T_C 房间Lv
	 */
	@Column(comment = "房间等级")
	private int roomLv;
	
	/**
	 * G_T_C 房间类型
	 */
	@Column(comment = "房间类型")
	private int roomType;
	
	/**
	 * G_T_C 玩家id
	 */
	@Column(comment = "玩家id")
	private long rid;
	
	/**
	 * 1-Ai,0-玩家
	 */
	@Column(comment = "是否是AI", length =3)
	private int isAi;
	
	/**
	 * 开始时间
	 */
	@Column(comment = "开始时间")
	private int startTime;
	
	/**
	 * 结束时间
	 */
	@Column(comment = "结束时间")
	private int endTime;


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}


	public long getRoomId() {
		return roomId;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}
	
	public long getRoomLv() {
		return roomLv;
	}

	public void setRoomLv(int roomLv) {
		this.roomLv = roomLv;
	}
	
	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}
	
	public long getRoomType() {
		return roomType;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public long getRid() {
		return rid;
	}
	
	public void setIsAi(int isAi) {
		this.isAi = isAi;
	}
	
	public long getIsAi() {
		return isAi;
	}

	@Override
	public String toString() {
		return "斗牛坐庄日志 [id=" + id + ", roomId=" + roomId + ", roomLv=" + roomLv 
				+ ", roomType=" + roomType
				+ ", rid=" + rid + ", isAi=" + isAi
				+ ", startTime=" + startTime + ", endTime=" + endTime + "]";
	}
}
