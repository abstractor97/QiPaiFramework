package com.yaowan.csv.entity;

/**
 * Created by fixend on 2017/1/6.
 */
public class MatchRewardCsv {
	
	// 奖励组
	private int rewardGroupId;
	// 开始名次
	private int startRanking;
	// 结束名次
	private int endRanking;
	// 奖励
	private String reward;
	
	public int getRewardGroupId() {
		return rewardGroupId;
	}
	
	public void setRewardGroupId(int rewardGroupId) {
		this.rewardGroupId = rewardGroupId;
	}
	
	public int getStartRanking() {
		return startRanking;
	}
	
	public void setStartRanking(int startRanking) {
		this.startRanking = startRanking;
	}
	
	public int getEndRanking() {
		return endRanking;
	}
	
	public void setEndRanking(int endRanking) {
		this.endRanking = endRanking;
	}
	
	public String getReward() {
		return reward;
	}
	
	public void setReward(String reward) {
		this.reward = reward;
	}
}
