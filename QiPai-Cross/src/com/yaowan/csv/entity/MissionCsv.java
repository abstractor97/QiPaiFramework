package com.yaowan.csv.entity;

import java.util.List;

import com.yaowan.framework.util.StringUtil;

/**
 * 任务配表
 * @author YW0861
 *
 */
public class MissionCsv  implements Cloneable {
	private int ID;
	private int mission;
	private int gameType;
	private int handPatterns;

	private int count;
	private String timeQuantum;
	private int rewards;
	private int rewardsCount;
	private int missionTips;
	private String missionIcon;
	private String tips;
	
	private int frontTask;
	private int postTask;
	private int taskType;
	private int taskGroup;
	
	private List<Integer[]> timeQuantumList;

	public int getTaskType() {
		return taskType;
	}
	public int getTaskGroup() {
		return taskGroup;
	}
	public void setTaskType(int taskType) {
		this.taskType = taskType;
	}
	public void setTaskGroup(int taskGroup) {
		this.taskGroup = taskGroup;
	}
	
	
	
	
	public int getFrontTask() {
		return frontTask;
	}
	public int getPostTask() {
		return postTask;
	}
	public void setFrontTask(int frontTask) {
		this.frontTask = frontTask;
	}
	public void setPostTask(int postTask) {
		this.postTask = postTask;
	}
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public int getMission() {
		return mission;
	}
	public void setMission(int mission) {
		this.mission = mission;
	}
	public int getGameType() {
		return gameType;
	}
	public void setGameType(int gameType) {
		this.gameType = gameType;
	}
	public int getHandPatterns() {
		return handPatterns;
	}
	public void setHandPatterns(int handPatterns) {
		this.handPatterns = handPatterns;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getTimeQuantum() {
		return timeQuantum;
	}
	public void setTimeQuantum(String timeQuantum) {
		this.timeQuantum = timeQuantum;
	}

	public int getRewardsCount() {
		return rewardsCount;
	}
	public void setRewardsCount(int rewardsCount) {
		this.rewardsCount = rewardsCount;
	}
	public String getMissionIcon() {
		return missionIcon;
	}
	public void setMissionIcon(String missionIcon) {
		this.missionIcon = missionIcon;
	}


	public List<Integer[]> getTimeQuantumList() {
		if (timeQuantumList == null) {
			timeQuantumList = StringUtil.stringToListArray(timeQuantum,
					Integer.class, "|", "_");
		}
		return timeQuantumList;
	}

	public void setTimeQuantumList(List<Integer[]> timeQuantumList) {
		this.timeQuantumList = timeQuantumList;
	}
	public String getTips() {
		return tips;
	}
	public void setTips(String tips) {
		this.tips = tips;
	}
	public int getRewards() {
		return rewards;
	}
	public void setRewards(int rewards) {
		this.rewards = rewards;
	}
	public int getMissionTips() {
		return missionTips;
	}
	public void setMissionTips(int missionTips) {
		this.missionTips = missionTips;
	}


}
