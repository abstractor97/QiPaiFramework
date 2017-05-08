package com.yaowan.server.game.model.struct;

import com.yaowan.framework.util.TimeUtil;


/**
 * 比赛排名项
 */
public class MatchRankingInfo {
	/**
	 * 角色id
	 */
	private long rid;
	/**
	 * 职分
	 */
	private int score;
	/**
	 * 排名
	 */
	private int ranking;
	/**
	 * 上榜时间
	 */
	private int time;
	
	public MatchRankingInfo(long rid, int score) {
		this.rid = rid;
		this.score = score;
		this.ranking = 0;
		this.time = TimeUtil.time();
	}
	
	public long getRid() {
		return rid;
	}
	
	public void setRid(long rid) {
		this.rid = rid;
	}
	
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public int getRanking() {
		return ranking;
	}
	
	public void setRanking(int ranking) {
		this.ranking = ranking;
	}
	
	public int getTime() {
		return time;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
}
