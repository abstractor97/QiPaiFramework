package com.yaowan.csv.entity;

import java.util.List;

import com.yaowan.framework.util.StringUtil;

//cfg_game_list
public class MissionGroupCsv {
	/**
	 * 
	 */
	private int ID;

	/**
	 * 
	 */
	private int Probability;
	/**
	 * 
	 */
	private String Mission;
	
	/**
	 * 
	 */
	private List<Integer> missionList;
	
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public int getProbability() {
		return Probability;
	}
	public void setProbability(int probability) {
		Probability = probability;
	}
	public String getMission() {
		return Mission;
	}
	public void setMission(String mission) {
		Mission = mission;
	}

	public List<Integer> getMissionList() {
		if (missionList == null) {
			missionList = StringUtil.stringToList(Mission, "|", Integer.class);
		}
		return missionList;
	}
	public void setMissionList(List<Integer> missionList) {
		this.missionList = missionList;
	}
	
	

}

