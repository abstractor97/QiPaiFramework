package com.yaowan.server.game.model.data.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Index;
import com.yaowan.framework.database.annotation.Table;

@Table(name = "game_match_record")
@Index(names = {"rid"}, indexs = {"rid"})
public class MatchRecord {
	
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "自增id")
	private long id;
	
	@Column(comment = "角色id")
	private long rid;
	
	@Column(comment = "比赛类型")
	private int matchType;
	
	@Column(comment = "排名")
	private int ranking;
	
	@Column(comment = "时间")
	private int time;
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getRid() {
		return rid;
	}
	
	public void setRid(long rid) {
		this.rid = rid;
	}
	
	public int getMatchType() {
		return matchType;
	}
	
	public void setMatchType(int matchType) {
		this.matchType = matchType;
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
