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
@Table(name = "doudizhu_data", comment = "玩家斗地主数据")
public class DoudizhuData {
	
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
	
	@Column(comment = "地主总场（每周）")
	private int hostWeek;
	
	@Column(comment = "地主胜场（每周）")
	private int hostWinWeek;
	
	@Column(comment = "农民总场（每周）")
	private int farmerWeek;
	
	@Column(comment = "农民胜场（每周）")
	private int farmerWinWeek;
	
	@Column(comment = "炸弹次数（每周）")
	private int bombWeek;
	
	@Column(comment = "王炸次数（每周）")
	private int kingBombWeek;
	
	@Column(comment = "最高倍率（每周）")
	private int highPowerWeek;
	
	@Column(comment = "最多赢注（每周）")
	private int maxWinWeek;
	
	@Column(comment = "总场数（生涯）")
	private int countTotal;
	
	@Column(comment = "胜场数（生涯）")
	private int winTotal;
	
	
	@Column(comment = "最高连胜（生涯）")
	private int maxcontinueTotal;
	
	@Column(comment = "地主总场（生涯）")
	private int hostTotal;
	
	@Column(comment = "地主胜场（生涯）")
	private int hostWinTotal;
	
	@Column(comment = "农民总场（生涯）")
	private int farmerTotal;
	
	@Column(comment = "农民胜场（生涯）")
	private int farmerWinTotal;
	
	@Column(comment = "炸弹次数（生涯）")
	private int bombTotal;
	
	@Column(comment = "王炸次数（生涯）")
	private int kingBombTotal;
	
	@Column(comment = "最高倍率（生涯）")
	private int highPowerTotal;
	
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

	public int getHostWeek() {
		return hostWeek;
	}

	public void setHostWeek(int hostWeek) {
		this.hostWeek = hostWeek;
	}

	public int getMaxcontinue() {
		return maxcontinue;
	}

	public void setMaxcontinue(int maxcontinue) {
		this.maxcontinue = maxcontinue;
	}

	public int getHostWinWeek() {
		return hostWinWeek;
	}

	public void setHostWinWeek(int hostWinWeek) {
		this.hostWinWeek = hostWinWeek;
	}

	public int getFarmerWeek() {
		return farmerWeek;
	}

	public void setFarmerWeek(int farmerWeek) {
		this.farmerWeek = farmerWeek;
	}

	public int getFarmerWinWeek() {
		return farmerWinWeek;
	}

	public void setFarmerWinWeek(int farmerWinWeek) {
		this.farmerWinWeek = farmerWinWeek;
	}

	public int getBombWeek() {
		return bombWeek;
	}

	public void setBombWeek(int bombWeek) {
		this.bombWeek = bombWeek;
	}

	public int getKingBombWeek() {
		return kingBombWeek;
	}

	public void setKingBombWeek(int kingBombWeek) {
		this.kingBombWeek = kingBombWeek;
	}

	public int getHighPowerWeek() {
		return highPowerWeek;
	}

	public void setHighPowerWeek(int highPowerWeek) {
		this.highPowerWeek = highPowerWeek;
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

	public int getHostTotal() {
		return hostTotal;
	}

	public void setHostTotal(int hostTotal) {
		this.hostTotal = hostTotal;
	}

	public int getHostWinTotal() {
		return hostWinTotal;
	}

	public void setHostWinTotal(int hostWinTotal) {
		this.hostWinTotal = hostWinTotal;
	}

	public int getFarmerTotal() {
		return farmerTotal;
	}

	public void setFarmerTotal(int farmerTotal) {
		this.farmerTotal = farmerTotal;
	}

	public int getFarmerWinTotal() {
		return farmerWinTotal;
	}

	public void setFarmerWinTotal(int farmerWinTotal) {
		this.farmerWinTotal = farmerWinTotal;
	}

	public int getBombTotal() {
		return bombTotal;
	}

	public void setBombTotal(int bombTotal) {
		this.bombTotal = bombTotal;
	}

	public int getKingBombTotal() {
		return kingBombTotal;
	}

	public void setKingBombTotal(int kingBombTotal) {
		this.kingBombTotal = kingBombTotal;
	}

	public int getHighPowerTotal() {
		return highPowerTotal;
	}

	public void setHighPowerTotal(int highPowerTotal) {
		this.highPowerTotal = highPowerTotal;
	}

	public int getMaxWinTotal() {
		return maxWinTotal;
	}

	public void setMaxWinTotal(int maxWinTotal) {
		this.maxWinTotal = maxWinTotal;
	}
}
