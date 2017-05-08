package com.yaowan.server.game.model.struct;

import java.util.ArrayList;
import java.util.List;

import com.yaowan.model.struct.GameRole;
import com.yaowan.protobuf.game.GGame.PlayerState;

/**
 * @author zane
 *
 */
public class ZTDoudizhuRole {

	public ZTDoudizhuRole(GameRole role) {
		this.role = role;
	}

	public void reset() {
		setCurrentPower(1);
		setLookedPai(0);
		setPai(new ArrayList<Integer>());
		this.recyclePai.clear();
		passCount = 0;
		outCount = 0;

		wangzhaCount = 0;
		zhadanCount = 0;
		shunziCount = 0;
		lianduiCount = 0;
		feijiCount = 0;
		
		if (role != null) {
			getRole().setStatus(PlayerState.PS_SEAT_VALUE);
			if (!getRole().isRobot()) {
				getRole().setAuto(false);
			}
		}

		currentPower = 1;
	}

	/**
	 * 初始角色
	 */
	private GameRole role = null;

	/**
	 * 牌
	 */
	private List<Integer> pai = new ArrayList<Integer>();

	/**
	 * 牌内部结构
	 */
	private List<Card> cards = new ArrayList<Card>();
	// 弃牌堆(倒计时结束则丢弃这张牌)
	private List<Integer> recyclePai = new ArrayList<Integer>();
	// 个人倍数
	private int currentPower = 1;

	// 是否看牌
	private int lookedPai;

	// 过牌次数
	private int passCount;

	// 出牌次数
	private int outCount;

	// 王炸次数
	private int wangzhaCount;

	// 炸弹次数
	private int zhadanCount;

	private int shunziCount;

	private int lianduiCount;

	private int feijiCount;

	public int getWangzhaCount() {
		return wangzhaCount;
	}

	public void setWangzhaCount(int wangzhaCount) {
		this.wangzhaCount = wangzhaCount;
	}

	public int getZhadanCount() {
		return zhadanCount;
	}

	public void setZhadanCount(int zhadanCount) {
		this.zhadanCount = zhadanCount;
	}

	public GameRole getRole() {
		return role;
	}

	public void setRole(GameRole role) {
		this.role = role;
	}

	public List<Integer> getPai() {
		return pai;
	}

	public void setPai(List<Integer> pai) {
		this.pai = pai;
	}

	public List<Integer> getRecyclePai() {
		return recyclePai;
	}

	public void setRecyclePai(List<Integer> recyclePai) {
		this.recyclePai = recyclePai;
	}

	public int getCurrentPower() {
		return currentPower;
	}

	public void setCurrentPower(int currentPower) {
		this.currentPower = currentPower;
	}

	public int getLookedPai() {
		return lookedPai;
	}

	public void setLookedPai(int lookedPai) {
		this.lookedPai = lookedPai;
	}

	public List<Card> getCards() {
		return cards;
	}

	public void setCards(List<Card> cards) {
		this.cards = cards;
	}

	public int getPassCount() {
		return passCount;
	}

	public void setPassCount(int passCount) {
		this.passCount = passCount;
	}

	public int getOutCount() {
		return outCount;
	}

	public void setOutCount(int outCount) {
		this.outCount = outCount;
	}

	public int getShunziCount() {
		return shunziCount;
	}

	public int getLianduiCount() {
		return lianduiCount;
	}

	public int getFeijiCount() {
		return feijiCount;
	}

	public void setShunziCount(int shunziCount) {
		this.shunziCount = shunziCount;
	}

	public void setLianduiCount(int lianduiCount) {
		this.lianduiCount = lianduiCount;
	}

	public void setFeijiCount(int feijiCount) {
		this.feijiCount = feijiCount;
	}

}
