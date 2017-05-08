package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Table;

/**
 * 投票活动日志
 * @author G_T_C
 */
@Table(name = "vote_activity_log", comment = "投票活动日志  ")
public class VoteActivityLog {

	@Column(comment="活动类型id")
	private int AtypeId;
	
	@Column(comment="玩家id")
	private long rid;
	
	@Column(comment="投票结果：1,2,3...代表A,B,C",length=2)
	private int voteResult;
	
	@Column(comment = "玩家昵称")
	private String nick;
	
	@Column(comment = "投票时间")
	private int time;

	public int getAtypeId() {
		return AtypeId;
	}

	public long getRid() {
		return rid;
	}

	public int getVoteResult() {
		return voteResult;
	}

	public void setAtypeId(int atypeId) {
		AtypeId = atypeId;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public void setVoteResult(int voteResult) {
		this.voteResult = voteResult;
	}

	public String getNick() {
		return nick;
	}

	public int getTime() {
		return time;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public void setTime(int time) {
		this.time = time;
	}
	
	
}
