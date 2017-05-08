package com.yaowan.server.game.model.data.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;

/**
 * G_T_C
 */
@Table(name = "activity_gift_bag", comment = "活动礼包配置表")
public class ActivityGiftBag {

	@Id(strategy = Strategy.AUTO)
	@Column(comment = "礼包序号")
	private long id;

	@Column(comment = "活动编号")
	private long aid;

	@Column(comment = "礼包名称")
	private String name;

	@Column(comment = "礼包领取条件")
	private String condition;

	@Column(comment = "礼包奖励")
	private String reward;

	public long getId() {
		return id;
	}

	public long getAid() {
		return aid;
	}

	public String getName() {
		return name;
	}

	public String getCondition() {
		return condition;
	}

	public String getReward() {
		return reward;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setAid(long aid) {
		this.aid = aid;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public void setReward(String reward) {
		this.reward = reward;
	}

	@Override
	public String toString() {
		return "GiftBag [id=" + id + ", aid=" + aid + ", name=" + name
				+ ", condition=" + condition + ", reward=" + reward + "]";
	}

}
