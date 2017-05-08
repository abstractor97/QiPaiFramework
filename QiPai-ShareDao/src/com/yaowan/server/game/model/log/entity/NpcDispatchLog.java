package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;

@Table(name = "npc_dispatch_log")
public class NpcDispatchLog {
	
	public NpcDispatchLog(String ids, String golds, int num, int isAllDay,
			int doStartTime, int doEndTime, int doStartDate, int doEndDate,
			int gameType, int roomType, String theme, String creator, int serverId) {
		super();
		this.ids = ids;
		this.golds = golds;
		this.num = num;
		this.isAllDay = isAllDay;
		this.doStartTime = doStartTime;
		this.doEndTime = doEndTime;
		this.doStartDate = doStartDate;
		this.doEndDate = doEndDate;
		this.gameType = gameType;
		this.roomType = roomType;
		this.theme = theme;
		this.creator = creator;
		this.serverId = serverId;
	}
	
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "主键")
	private long id;
	
	@Column(comment = "npc 的id",length=2048)
	private String ids;
	
	@Column(comment = "金币区间")
	private String golds;
	
	@Column(comment = "数量")
	private int num;
	
	@Column(comment = "服务器ID")
	private int serverId;
	
	@Column(comment = "ai调度是否是全天，1是  0为不是")
	private int  isAllDay;
	
	@Column(comment = "ai调度开始时间")
	private int  doStartTime;
	
	@Column(comment = "ai调度结束时间")
	private int  doEndTime;
	
	@Column(comment = "ai调度开始日期")
	private int  doStartDate;
	
	@Column(comment = "ai调度结束日期")
	private int  doEndDate;
	
	@Column(comment = "游戏类型")
	private int gameType;
	
	@Column(comment = "房间类型")
	private int roomType;
	
	@Column(comment = "主题")
	private String theme;
	
	@Column(comment = "创建人")
	private String creator;
	
	@Column(comment = "创建时间")
	private int createTime;
	
	@Column(comment = "开关 1为开，0为关")
	private int isOpen;

	public String getIds() {
		return ids;
	}

	public String getGolds() {
		return golds;
	}

	public int getNum() {
		return num;
	}

	public int getIsAllDay() {
		return isAllDay;
	}

	public int getDoStartTime() {
		return doStartTime;
	}

	public int getDoEndTime() {
		return doEndTime;
	}

	public int getDoStartDate() {
		return doStartDate;
	}

	public int getDoEndDate() {
		return doEndDate;
	}

	public int getGameType() {
		return gameType;
	}

	public int getRoomType() {
		return roomType;
	}

	public String getTheme() {
		return theme;
	}

	public String getCreator() {
		return creator;
	}

	public void setIds(String ids) {
		this.ids = ids;
	}

	public void setGolds(String golds) {
		this.golds = golds;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public void setIsAllDay(int isAllDay) {
		this.isAllDay = isAllDay;
	}

	public void setDoStartTime(int doStartTime) {
		this.doStartTime = doStartTime;
	}

	public void setDoEndTime(int doEndTime) {
		this.doEndTime = doEndTime;
	}

	public void setDoStartDate(int doStartDate) {
		this.doStartDate = doStartDate;
	}

	public void setDoEndDate(int doEndDate) {
		this.doEndDate = doEndDate;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getIsOpen() {
		return isOpen;
	}

	public void setIsOpen(int isOpen) {
		this.isOpen = isOpen;
	}

	public int getCreateTime() {
		return createTime;
	}

	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}
	
	
}
