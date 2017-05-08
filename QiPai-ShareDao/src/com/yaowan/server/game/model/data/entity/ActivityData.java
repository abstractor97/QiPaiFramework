package com.yaowan.server.game.model.data.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.orm.UpdateProperty;

@Table(name = "activity_data", comment = "活动配置数据")
public class ActivityData extends UpdateProperty{

	@Id(strategy = Strategy.AUTO)
	@Column(comment = "活动序号")
	private long id;

	@Column(comment = "活动名字")
	private String name;

	@Column(comment = "活动说明")
	private String explain;

	@Column(comment = "活动图标地址")
	private String pictureUrl;

	@Column(comment = "活动地址")
	private String activtityUrl;
	
	@Column(comment = "活动开始时间")
	private int startTime;

	@Column(comment = "活动结束时间")
	private int endTime;

	@Column(comment = "活动礼包领取截止时间")
	private int rewardExpireTime;
	

	@Column(comment = "活动类型")
	private int type;
	
	@Column(comment = "活动点击量")
	private int clickNum;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getExplain() {
		return explain;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public int getRewardExpireTime() {
		return rewardExpireTime;
	}

	public int getType() {
		return type;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setExplain(String explain) {
		this.explain = explain;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public void setRewardExpireTime(int rewardExpireTime) {
		this.rewardExpireTime = rewardExpireTime;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public int getClickNum() {
		return clickNum;
	}

	public void setClickNum(int clickNum) {
		this.clickNum = clickNum;
	}
	
	

	public String getActivtityUrl() {
		return activtityUrl;
	}

	public void setActivtityUrl(String activtityUrl) {
		this.activtityUrl = activtityUrl;
	}

	@Override
	public String toString() {
		return "Activity [id=" + id + ", name=" + name + ", explain=" + explain
				+ ", pictureUrl=" + pictureUrl + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", rewardExpireTime="
				+ rewardExpireTime + ", type=" + type + "]";
	}

}
