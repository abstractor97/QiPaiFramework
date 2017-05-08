package com.yaowan.server.game.model.data.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Table;

@Table(name = "role_red_bag")
public class RoleRedBag {
	
	@Id
	@Column(comment = "玩家id")
	private long rid;
	
	@Column(comment = "一天领取的次数")
	private int dailyNum;
	
	@Column(comment = "每次领取的")
	private int timesNum;

	public long getRid() {
		return rid;
	}

	public int getDailyNum() {
		return dailyNum;
	}

	public int getTimesNum() {
		return timesNum;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public void setDailyNum(int dailyNum) {
		this.dailyNum = dailyNum;
	}

	public void setTimesNum(int timesNum) {
		this.timesNum = timesNum;
	}
	
	
}
