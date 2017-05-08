/**
 * 
 */
package com.yaowan.server.game.model.data.entity;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Table;



/**
 * @author yangbin
 *
 */
@Table(name = "chengdu_majiang_data", comment = "玩家成都麻将数据")
public class CDMajiangData {
	
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
	
	@Column(comment = "头彩数（每周）")
	private int firstWeek;
	
	@Column(comment = "清一色（每周）")
	private int sameCardsWeek;
	
	@Column(comment = "巧七对（每周）")
	private int sevenWeek;
	
	@Column(comment = "杠上花（每周）")
	private int gangAndHuWeek;
	
	@Column(comment = "大对子（每周）")
	private int bigPairWeek;
	
	@Column(comment = "最高番数（每周）")
	private int maxPowerWeek;
	
	
	@Column(comment = "最多赢注（每周）")
	private int maxWinWeek;
	
	@Column(comment = "总场数（生涯）")
	private int countTotal;
	
	@Column(comment = "胜场数（生涯）")
	private int winTotal;
	
	
	@Column(comment = "头彩数（生涯）")
	private int firstTotal;
	
	@Column(comment = "清一色（生涯）")
	private int sameCardsTotal;
	
	@Column(comment = "巧七对（生涯）")
	private int sevenTotal;
	
	@Column(comment = "杠上花（生涯）")
	private int gangAndHuTotal;
	
	@Column(comment = "大对子（生涯）")
	private int bigPairTotal;
	
	@Column(comment = "最高番数（生涯）")
	private int maxPowerTotal;
	
	@Column(comment = "最多赢注（生涯）")
	private int maxWinTotal;
	
	@Column(comment = "最高连胜（生涯）")
	private int maxcontinueTotal;

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

	public int getFirstWeek() {
		return firstWeek;
	}

	public void setFirstWeek(int firstWeek) {
		this.firstWeek = firstWeek;
	}

	public int getSameCardsWeek() {
		return sameCardsWeek;
	}

	public void setSameCardsWeek(int sameCardsWeek) {
		this.sameCardsWeek = sameCardsWeek;
	}

	public int getSevenWeek() {
		return sevenWeek;
	}

	public int getMaxcontinue() {
		return maxcontinue;
	}

	public void setMaxcontinue(int maxcontinue) {
		this.maxcontinue = maxcontinue;
	}

	public void setSevenWeek(int sevenWeek) {
		this.sevenWeek = sevenWeek;
	}

	public int getGangAndHuWeek() {
		return gangAndHuWeek;
	}

	public void setGangAndHuWeek(int gangAndHuWeek) {
		this.gangAndHuWeek = gangAndHuWeek;
	}

	public int getBigPairWeek() {
		return bigPairWeek;
	}

	public void setBigPairWeek(int bigPairWeek) {
		this.bigPairWeek = bigPairWeek;
	}

	public int getMaxPowerWeek() {
		return maxPowerWeek;
	}

	public void setMaxPowerWeek(int maxPowerWeek) {
		this.maxPowerWeek = maxPowerWeek;
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

	public int getFirstTotal() {
		return firstTotal;
	}

	public int getMaxcontinueTotal() {
		return maxcontinueTotal;
	}

	public void setMaxcontinueTotal(int maxcontinueTotal) {
		this.maxcontinueTotal = maxcontinueTotal;
	}

	public void setFirstTotal(int firstTotal) {
		this.firstTotal = firstTotal;
	}

	public int getSameCardsTotal() {
		return sameCardsTotal;
	}

	public void setSameCardsTotal(int sameCardsTotal) {
		this.sameCardsTotal = sameCardsTotal;
	}

	public int getSevenTotal() {
		return sevenTotal;
	}

	public void setSevenTotal(int sevenTotal) {
		this.sevenTotal = sevenTotal;
	}

	public int getGangAndHuTotal() {
		return gangAndHuTotal;
	}

	public void setGangAndHuTotal(int gangAndHuTotal) {
		this.gangAndHuTotal = gangAndHuTotal;
	}

	public int getBigPairTotal() {
		return bigPairTotal;
	}

	public void setBigPairTotal(int bigPairTotal) {
		this.bigPairTotal = bigPairTotal;
	}

	public int getMaxPowerTotal() {
		return maxPowerTotal;
	}

	public void setMaxPowerTotal(int maxPowerTotal) {
		this.maxPowerTotal = maxPowerTotal;
	}

	public int getMaxWinTotal() {
		return maxWinTotal;
	}

	public void setMaxWinTotal(int maxWinTotal) {
		this.maxWinTotal = maxWinTotal;
	}
}
