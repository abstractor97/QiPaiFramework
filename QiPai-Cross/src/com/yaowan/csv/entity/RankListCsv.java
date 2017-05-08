package com.yaowan.csv.entity;

public class RankListCsv {

	private int ID;
	
	private int ShowLimit;
	
	private int RankingLimit;
	
	private int Refresh;

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public int getShowLimit() {
		return ShowLimit;
	}

	public void setShowLimit(int showLimit) {
		ShowLimit = showLimit;
	}

	public int getRankingLimit() {
		return RankingLimit;
	}

	public void setRankingLimit(int rankingLimit) {
		RankingLimit = rankingLimit;
	}

	public int getRefresh() {
		return Refresh;
	}

	public void setRefresh(int refresh) {
		Refresh = refresh;
	}
}
