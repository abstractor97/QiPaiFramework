package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;

/**
 * 斗地主日志
 * 
 * @author zane 2016年10月9日 下午9:28:54
 */
@Table(name = "doudizhu_log", comment = "斗地主日志")
public class DoudizhuLog {
	/**
	 * 牌局ID
	 */
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "牌局ID")
	private long id;

	/**
	 * G_T_C 房间号
	 */
	@Column(comment = "房间号")
	private long roomId;

	/**
	 * 参与玩家
	 */
	@Column(comment = "参与玩家 格式:玩家id_玩家昵称_身份_金币变化_是否使用王炸_使用炸弹数|", length = 1000)
	private String roleInfo;

	/**
	 * ai的输赢总金额
	 */
	@Column(comment = "ai胜负金币和(绝对值等于玩家的胜负和)")
	private int aiGold;
	
	@Column(comment = "参与玩家数量",length =3)
	private int totalPlayerCount;
	
	@Column(comment = "真实玩家数量，除了AI",length =3)
	private int realPlayerCount;

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
	/**
	 * 赢得玩家
	 */
	@Column(comment = "赢得玩家 格式:玩家id|")
	private String winRoles;
	

	public long getId() {
		return id;
	}

	public long getRoomId() {
		return roomId;
	}

	public String getRoleInfo() {
		return roleInfo;
	}

	public int getAiGold() {
		return aiGold;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public String getWinRoles() {
		return winRoles;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}

	public void setRoleInfo(String roleInfo) {
		this.roleInfo = roleInfo;
	}

	public void setAiGold(int aiGold) {
		this.aiGold = aiGold;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public void setWinRoles(String winRoles) {
		this.winRoles = winRoles;
	}

	public int getTotalPlayerCount() {
		return totalPlayerCount;
	}

	public int getRealPlayerCount() {
		return realPlayerCount;
	}

	public void setTotalPlayerCount(int totalPlayerCount) {
		this.totalPlayerCount = totalPlayerCount;
	}

	public void setRealPlayerCount(int realPlayerCount) {
		this.realPlayerCount = realPlayerCount;
	}

	@Override
	public String toString() {
		return "DoudizhuLog [id=" + id + ", roomId=" + roomId + ", roleInfo="
				+ roleInfo + ", aiGold=" + aiGold + ", totalPlayerCount="
				+ totalPlayerCount + ", realPlayerCount=" + realPlayerCount
				+ ", startTime=" + startTime + ", endTime=" + endTime
				+ ", winRoles=" + winRoles + "]";
	}
	
}
