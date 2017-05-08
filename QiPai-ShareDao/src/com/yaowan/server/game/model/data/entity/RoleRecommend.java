package com.yaowan.server.game.model.data.entity;


import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;

/**
 * 推荐信息表
 * @author YW0981
 *
 */

@Table(name = "role_recommend", comment = "推荐表")
@Index(names = {"rid"}, indexs = {"rid"})
public class RoleRecommend {

	@Id
	@Column(comment = "玩家id")
	private long rid;
	
	@Column(comment = "推荐码")
	private String code;
	
	@Column(comment = "能得到的奖励")
	private int canGetReward;
	
	@Column(comment = "已经得到的奖励")
	private int hasGetReward;
	
	@Column(comment = "已经推荐的数量")
	private int recommendFriendNum;
	
	@Column(comment = "今天推荐的次数")
	private int times;
	
	@Column(comment = "开启/关闭")
	private int isOpen;

	@Column(comment = "推广等级")
	private int level;
	
	
	
	

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public long getRid() {
		return rid;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getCanGetReward() {
		return canGetReward;
	}

	public void setCanGetReward(int canGetReward) {
		this.canGetReward = canGetReward;
	}

	public int getHasGetReward() {
		return hasGetReward;
	}

	public void setHasGetReward(int hasGetReward) {
		this.hasGetReward = hasGetReward;
	}

	public int getRecommendFriendNum() {
		return recommendFriendNum;
	}

	public void setRecommendFriendNum(int recommendFriendNum) {
		this.recommendFriendNum = recommendFriendNum;
	}
	
	public int getIsOpen() {
		return isOpen;
	}

	public void setIsOpen(int isOpen) {
		this.isOpen = isOpen;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}


}
