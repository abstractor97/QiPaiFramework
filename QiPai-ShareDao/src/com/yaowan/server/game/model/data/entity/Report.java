package com.yaowan.server.game.model.data.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;

@Table(name = "report", comment="举报表")
public class Report {
	
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "id，流水id")
	private long id;
	
	@Column(comment = "举报id")
	private long rid1;
	
	@Column(comment = "被举报id")
	private long rid2;
	
	@Column(comment = "举报类型，目前没有，策划配置")
	private int reportType;
	
	@Column(comment = "举报时间")
	private int time;

	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getRid1() {
		return rid1;
	}

	public void setRid1(long rid1) {
		this.rid1 = rid1;
	}

	public long getRid2() {
		return rid2;
	}

	public void setRid2(long rid2) {
		this.rid2 = rid2;
	}

	public int getReportType() {
		return reportType;
	}

	public void setReportType(int reportType) {
		this.reportType = reportType;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
		
}
