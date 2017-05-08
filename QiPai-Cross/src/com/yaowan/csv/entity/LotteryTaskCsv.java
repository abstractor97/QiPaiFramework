package com.yaowan.csv.entity;

public class LotteryTaskCsv {

	private int lotteryTaskID;//奖券任务ID
	
	private int ascriptionGameId;//
	
	private int taskType;//任务类型
	
	private int count;//
	
	private int probability;//
	
	private int rewards;//
	
	private int rewardsCount;//
	
	private String tips;//

	public int getLotteryTaskID() {
		return lotteryTaskID;
	}

	public int getAscriptionGameId() {
		return ascriptionGameId;
	}

	public int getTaskType() {
		return taskType;
	}

	public int getCount() {
		return count;
	}

	public int getProbability() {
		return probability;
	}

	public int getRewards() {
		return rewards;
	}

	public int getRewardsCount() {
		return rewardsCount;
	}

	public String getTips() {
		return tips;
	}

	public void setLotteryTaskID(int lotteryTaskID) {
		this.lotteryTaskID = lotteryTaskID;
	}

	public void setAscriptionGameId(int ascriptionGameId) {
		this.ascriptionGameId = ascriptionGameId;
	}

	public void setTaskType(int taskType) {
		this.taskType = taskType;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setProbability(int probability) {
		this.probability = probability;
	}

	public void setRewards(int rewards) {
		this.rewards = rewards;
	}

	public void setRewardsCount(int rewardsCount) {
		this.rewardsCount = rewardsCount;
	}

	public void setTips(String tips) {
		this.tips = tips;
	}

	
}
