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
@Table(name = "ncmenji_data", comment = "南充玩家焖鸡数据")
public class NCMenjiData {
	
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
	
	@Column(comment = "豹子（每周）")
	private int baoziWeek;
	
	@Column(comment = "同花顺（每周）")
	private int tonghuashunWeek;
	
	@Column(comment = "同花（每周）")
	private int tonghuaWeek;
	
	@Column(comment = "顺子（每周）")
	private int shunziWeek;
	
	@Column(comment = "最多赢注（每周）")
	private int maxWinWeek;
	
	@Column(comment = "总场数（生涯）")
	private int countTotal;
	
	@Column(comment = "胜场数（生涯）")
	private int winTotal;
	
	
	@Column(comment = "最高连胜（生涯）")
	private int maxcontinueTotal;
	
	@Column(comment = "豹子（生涯）")
	private int baoziTotal;
	
	@Column(comment = "同花顺（生涯）")
	private int tonghuashunTotal;
	
	@Column(comment = "同花（生涯）")
	private int tonghuaTotal;
	
	@Column(comment = "顺子（生涯）")
	private int shunziTotal;
	
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

	public int getBaoziWeek() {
		return baoziWeek;
	}

	public void setBaoziWeek(int baoziWeek) {
		this.baoziWeek = baoziWeek;
	}

	public int getTonghuashunWeek() {
		return tonghuashunWeek;
	}

	public void setTonghuashunWeek(int tonghuashunWeek) {
		this.tonghuashunWeek = tonghuashunWeek;
	}

	public int getTonghuaWeek() {
		return tonghuaWeek;
	}

	public void setTonghuaWeek(int tonghuaWeek) {
		this.tonghuaWeek = tonghuaWeek;
	}

	public int getShunziWeek() {
		return shunziWeek;
	}

	public void setShunziWeek(int shunziWeek) {
		this.shunziWeek = shunziWeek;
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

	public int getBaoziTotal() {
		return baoziTotal;
	}

	public void setBaoziTotal(int baoziTotal) {
		this.baoziTotal = baoziTotal;
	}

	public int getTonghuashunTotal() {
		return tonghuashunTotal;
	}

	public void setTonghuashunTotal(int tonghuashunTotal) {
		this.tonghuashunTotal = tonghuashunTotal;
	}

	public int getTonghuaTotal() {
		return tonghuaTotal;
	}

	public void setTonghuaTotal(int tonghuaTotal) {
		this.tonghuaTotal = tonghuaTotal;
	}

	public int getShunziTotal() {
		return shunziTotal;
	}

	public void setShunziTotal(int shunziTotal) {
		this.shunziTotal = shunziTotal;
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
