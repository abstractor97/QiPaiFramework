package com.yaowan.server.game.model.struct;

import com.yaowan.server.game.model.data.entity.MatchData;

public class Match {
	
	private boolean inited = false;
	private MatchData matchData;
	
	/**
	 * 下一次更新时间，单位：秒
	 */
	private int nextUpdateTime;
	// 报名时间
	private int applyTime;
	// 预赛时间
	private int matchTime;
	
	public Match(MatchData matchData) {
		this.matchData = matchData;
	}
	
	public boolean isInited() {
		return inited;
	}
	
	public void setInited(boolean inited) {
		this.inited = inited;
	}
	
	public MatchData getMatchData() {
		return matchData;
	}
	
	public void setMatchData(MatchData matchData) {
		this.matchData = matchData;
	}
	
	public int getNextUpdateTime() {
		return nextUpdateTime;
	}
	
	public void setNextUpdateTime(int nextUpdateTime) {
		this.nextUpdateTime = nextUpdateTime;
	}
	
	public int getApplyTime() {
		return applyTime;
	}
	
	public void setApplyTime(int applyTime) {
		this.applyTime = applyTime;
	}
	
	public int getMatchTime() {
		return matchTime;
	}
	
	public void setMatchTime(int matchTime) {
		this.matchTime = matchTime;
	}
}
