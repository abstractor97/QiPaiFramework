package com.yaowan.server.game.model.data.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Table;

@Table(name = "game_match_role", comment = "比赛参加者信息")
public class MatchRoleData {
	
	@Id(sort = 1)
	@Column(comment = "比赛ID")
	private int matchId;
	
	@Id(sort = 2)
	@Column(comment = "玩家id")
	private long rid;
	
	@Column(comment = "报名时间")
	private int joinTime;
	
	@Column(comment = "胜利次数")
	private int winCount;
	
	@Column(comment = "失败次数")
	private int lostCount;
	
	@Column(comment = "平局次数")
	private int deuceCount;
	
	@Column(comment = "当前积分")
	private int score;
	
	@Column(comment = "参加的比赛阶段")
	private int step;
	
	@Column(comment = "玩家的状态")
	private int stat;
	
	public long getMatchId() {
		return matchId;
	}
	
	public void setMatchId(int matchId) {
		this.matchId = matchId;
	}
	
	public long getRid() {
		return rid;
	}
	
	public void setRid(long rid) {
		this.rid = rid;
	}
	
	public int getJoinTime() {
		return joinTime;
	}
	
	public void setJoinTime(int joinTime) {
		this.joinTime = joinTime;
	}
	
	public int getWinCount() {
		return winCount;
	}
	
	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}
	
	public int getLostCount() {
		return lostCount;
	}
	
	public void setLostCount(int lostCount) {
		this.lostCount = lostCount;
	}
	
	public int getDeuceCount() {
		return deuceCount;
	}
	
	public void setDeuceCount(int deuceCount) {
		this.deuceCount = deuceCount;
	}
	
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public int getStep() {
		return step;
	}
	
	public void setStep(int step) {
		this.step = step;
	}
	
	public int getStat() {
		return stat;
	}
	
	public void setStat(int stat) {
		this.stat = stat;
	}
}
