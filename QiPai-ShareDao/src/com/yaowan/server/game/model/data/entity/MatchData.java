package com.yaowan.server.game.model.data.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.orm.UpdateProperty;

/**
 * 比赛信息
 */
@Table(name = "game_match", comment = "比赛信息表")
public class MatchData extends UpdateProperty {
	
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "比赛ID")
	private int matchId;
	
	@Column(comment = "比赛类型")
	private int matchType;
	
	@Column(comment = "最小机器人数量")
	private int minNpcCount;
	
	@Column(comment = "最大机器人数量")
	private int maxNpcCount;
	
	@Column(comment = "发生方式，固定时间、每日、每周等")
	private int happenType;
	
	/**
	 *
	 */
	@Column(comment = "报名时间，yyyy-MM-dd")
	private String applyDate;
	
	@Column(comment = "报名时间，星期几")
	private int applyDayOfWeek;
	
	@Column(comment = "报名时间，HH:mm:ss")
	private String applyTimeOfDay;
	
	/**
	 *
	 */
	@Column(comment = "预赛时间，yyyy-MM-dd")
	private String match01Date;
	
	@Column(comment = "预赛时间，星期几")
	private int match01DayOfWeek;
	
	@Column(comment = "预赛时间，HH:mm:ss")
	private String match01TimeOfDay;
	
	@Column(comment = "当前状态")
	private int stat;
	
	public int getMatchId() {
		return matchId;
	}
	
	public void setMatchId(int matchId) {
		this.matchId = matchId;
	}
	
	public int getMatchType() {
		return matchType;
	}
	
	public void setMatchType(int matchType) {
		this.matchType = matchType;
	}
	
	public int getMinNpcCount() {
		return minNpcCount;
	}
	
	public void setMinNpcCount(int minNpcCount) {
		this.minNpcCount = minNpcCount;
	}
	
	public int getMaxNpcCount() {
		return maxNpcCount;
	}
	
	public void setMaxNpcCount(int maxNpcCount) {
		this.maxNpcCount = maxNpcCount;
	}
	
	public int getHappenType() {
		return happenType;
	}
	
	public void setHappenType(int happenType) {
		this.happenType = happenType;
	}
	
	public String getApplyDate() {
		return applyDate;
	}
	
	public void setApplyDate(String applyDate) {
		this.applyDate = applyDate;
	}
	
	public int getApplyDayOfWeek() {
		return applyDayOfWeek;
	}
	
	public void setApplyDayOfWeek(int applyDayOfWeek) {
		this.applyDayOfWeek = applyDayOfWeek;
	}
	
	public String getApplyTimeOfDay() {
		return applyTimeOfDay;
	}
	
	public void setApplyTimeOfDay(String applyTimeOfDay) {
		this.applyTimeOfDay = applyTimeOfDay;
	}
	
	public String getMatch01Date() {
		return match01Date;
	}
	
	public void setMatch01Date(String match01Date) {
		this.match01Date = match01Date;
	}
	
	public int getMatch01DayOfWeek() {
		return match01DayOfWeek;
	}
	
	public void setMatch01DayOfWeek(int match01DayOfWeek) {
		this.match01DayOfWeek = match01DayOfWeek;
	}
	
	public String getMatch01TimeOfDay() {
		return match01TimeOfDay;
	}
	
	public void setMatch01TimeOfDay(String match01TimeOfDay) {
		this.match01TimeOfDay = match01TimeOfDay;
	}
	
	public int getStat() {
		return stat;
	}
	
	public void setStat(int stat) {
		this.stat = stat;
	}
}
