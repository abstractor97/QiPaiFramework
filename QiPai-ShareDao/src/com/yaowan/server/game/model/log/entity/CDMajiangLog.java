package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;

/**
 * 成都麻将日志
 * 
 * @author yangbin
 */
@Table(name = "zhenxiong_majiang_log", comment = "镇雄麻将日志  ")
public class CDMajiangLog {
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
	@Column(comment = "参与玩家 格式:玩家id_玩家昵称_身份_金币变化_胡牌牌型_胡牌番数|", length = 1000)
	private String roleInfo;

	@Column(comment = "角色信息流水明细；格式：角色id：番数_金币变化_另外玩家,|", length = 1000)
	private String roleInfoDetail;
	
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
	private String winRoles ;
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getRoleInfo() {
		return roleInfo;
	}

	public void setRoleInfo(String roleInfo) {
		this.roleInfo = roleInfo;
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

	public String getWinRoles() {
		return winRoles;
	}

	public void setWinRoles(String winRoles) {
		this.winRoles = winRoles;
	}

	public long getRoomId() {
		return roomId;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}

	public String getRoleInfoDetail() {
		return roleInfoDetail;
	}

	public void setRoleInfoDetail(String roleInfoDetail) {
		this.roleInfoDetail = roleInfoDetail;
	}
	
	 

	public int getAiGold() {
		return aiGold;
	}

	public void setAiGold(int aiGold) {
		this.aiGold = aiGold;
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
		return "CDMajiangLog [id=" + id + ", roomId=" + roomId + ", roleInfo="
				+ roleInfo + ", roleInfoDetail=" + roleInfoDetail + ", aiGold="
				+ aiGold + ", totalPlayerCount=" + totalPlayerCount
				+ ", realPlayerCount=" + realPlayerCount + ", startTime="
				+ startTime + ", endTime=" + endTime + ", winRoles=" + winRoles
				+ "]";
	}

}
