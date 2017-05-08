package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;

@Table(name="role_remain_money")
public class RoleRemainMoneyLog {
	
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "id")
	private long id;
	
	@Column(comment="日期 ")
	private int time;
	
	@Column(comment="金币")
	private long gold;
	
	@Column(comment="钻石")
	private long diamond;
	
	@Column(comment="奖券")
	private long lottery;

	public long getId() {
		return id;
	}

	public int getTime() {
		return time;
	}

	public long getGold() {
		return gold;
	}

	public long getDiamond() {
		return diamond;
	}

	public long getLottery() {
		return lottery;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public void setGold(long gold) {
		this.gold = gold;
	}

	public void setDiamond(long diamond) {
		this.diamond = diamond;
	}

	public void setLottery(long lottery) {
		this.lottery = lottery;
	}

	@Override
	public String toString() {
		return "RoleRemainMoneyLog [id=" + id + ", time=" + time + ", gold="
				+ gold + ", diamond=" + diamond + ", lottery=" + lottery + "]";
	}
	
	
}
