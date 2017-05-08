/**
 * 
 */
package com.yaowan.server.game.model.data.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Table;



/**
 * @author zane
 *
 */
@Table(name = "douniu_data", comment = "玩家斗牛数据")
public class DouniuData {
	
	@Id
	@Column(comment = "玩家id")
	private long rid;
	
	@Column(comment = "总场数（每周)")
	private int countWeek;
	
	@Column(comment = "胜场数（每周）")
	private int winWeek;
	
	@Column(comment = "最高连胜（每周）")
	private int maxcontinueWeek;
	
	@Column(comment = "当前最高连胜")
	private int maxcontinue;
	
	
	
	@Column(comment = "最多赢注（每周）")
	private int maxWinWeek;
	
	@Column(comment = "总场数（生涯）")
	private int countTotal;
	
	@Column(comment = "胜场数（生涯）")
	private int winTotal;
	
	
	@Column(comment = "最高连胜（生涯）")
	private int maxcontinueTotal;
	
	
	@Column(comment = "最多赢注（生涯）")
	private int maxWinTotal;

	public long getRid() {
		return rid;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public int getCountWeek() {
		return countWeek;
	}

	public void setCountWeek(int countWeek) {
		this.countWeek = countWeek;
	}

	public int getWinWeek() {
		return winWeek;
	}

	public void setWinWeek(int winWeek) {
		this.winWeek = winWeek;
	}

	public int getMaxcontinueWeek() {
		return maxcontinueWeek;
	}

	public void setMaxcontinueWeek(int maxcontinueWeek) {
		this.maxcontinueWeek = maxcontinueWeek;
	}



	public int getMaxWinWeek() {
		return maxWinWeek;
	}

	public void setMaxWinWeek(int maxWinWeek) {
		this.maxWinWeek = maxWinWeek;
	}

	public int getCountTotal() {
		return countTotal;
	}

	public void setCountTotal(int countTotal) {
		this.countTotal = countTotal;
	}

	public int getWinTotal() {
		return winTotal;
	}

	public void setWinTotal(int winTotal) {
		this.winTotal = winTotal;
	}

	public int getMaxcontinueTotal() {
		return maxcontinueTotal;
	}

	public void setMaxcontinueTotal(int maxcontinueTotal) {
		this.maxcontinueTotal = maxcontinueTotal;
	}



	public int getMaxWinTotal() {
		return maxWinTotal;
	}

	public void setMaxWinTotal(int maxWinTotal) {
		this.maxWinTotal = maxWinTotal;
	}

	public int getMaxcontinue() {
		return maxcontinue;
	}

	public void setMaxcontinue(int maxcontinue) {
		this.maxcontinue = maxcontinue;
	}
	
}
