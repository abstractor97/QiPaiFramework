package com.yaowan.server.game.model.struct;

import java.util.ArrayList;
import java.util.List;

import com.yaowan.protobuf.game.GDouniu.GDouniuPai;
import com.yaowan.server.game.rule.DouniuRule;


/**
 * 闲家信息
 * @author zane
 *
 */
public  class DouniuXian {
	
	public DouniuXian(){
		
	}
	
	public DouniuXian(int index){
		this.index =  index;
	}
	/**
	 * 索引
	 */
	private int index;
	/**
	 * 
	 */
	private GDouniuPai.Builder pai = GDouniuPai.newBuilder();
	
	
	
	/**
	 * 下注情况
	 */
	private List<Integer> chips = new ArrayList<Integer>();
	
	/**
	 * 下注情况
	 */
	private long totalGold;
	
	/**
	 * 输赢倍数
	 */
	private int winPower;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public GDouniuPai.Builder getPai() {
		return pai;
	}

	public void setPai(GDouniuPai.Builder pai) {
		this.pai = pai;
	}

	public List<Integer> getChips() {
		return chips;
	}

	public void setChips(List<Integer> chips) {
		this.chips = chips;
	}

	public long getTotalGold() {
		return totalGold;
	}

	public void setTotalGold(long totalGold) {
		this.totalGold = totalGold;
	}
	
	public void faPai(List<Integer> pais) {
		for (int card:pais) {
			getPai().addPaiValue(DouniuRule.numToPaiValue(card));
			getPai().addPaiColor(DouniuRule.numToPaiColor(card));
		}
		getPai().setPaiType(DouniuRule.getCardType(getPai()));
	}

	public int getWinPower() {
		return winPower;
	}

	public void setWinPower(int winPower) {
		this.winPower = winPower;
	}

	
}
