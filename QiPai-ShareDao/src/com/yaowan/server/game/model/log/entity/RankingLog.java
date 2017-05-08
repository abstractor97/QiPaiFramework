package com.yaowan.server.game.model.log.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;

@Table(name="ranking_log")
public class RankingLog {
	
	@Id(strategy=Strategy.AUTO)
	@Column(comment="主键，自增id")
	private Long id;

	@Column(comment="排行榜类型：1赚钱榜，2活跃榜，3富豪榜",length=2)
	private int type;
	
	@Column(comment="活动信息：格式为：玩家Id_玩家匿称_名次_数据信息|", length = 1024)
	private String info;
	
	@Column(comment="记录时间")
	private int time;

	public Long getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public String getInfo() {
		return info;
	}

	public int getTime() {
		return time;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public void setTime(int time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "RankingLog [id=" + id + ", type=" + type + ", info=" + info
				+ ", time=" + time + "]";
	}
}
