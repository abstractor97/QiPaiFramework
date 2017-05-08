package com.yaowan.server.game.model.struct;

import java.util.ArrayList;
import java.util.List;

import com.yaowan.model.struct.GameRole;
import com.yaowan.protobuf.game.GGame.PlayerState;


/**
 * @author zane
 *
 */
public  class ZTMenjiRole {
	
	public ZTMenjiRole() {
		
	}
	
	public ZTMenjiRole(GameRole role) {
		this.role = role;
	}
	public void reset() {
		pai.clear();
		recyclePai = 0;
		lookedPai = 0;
		chip = 0;
		if(getRole()!=null){
			getRole().setStatus(PlayerState.PS_SEAT_VALUE);
		}
		look = 0;
	}

	/**
	 * 初始角色 焖鸡离开座位可以为空
	 */
	private GameRole role = null;

	/**
	 * 牌
	 */
	private List<Integer> pai = new ArrayList<Integer>();

	// 摆出来的牌
	private int[][] showPai = new int[27][2];
	// 弃牌堆(倒计时结束则丢弃这张牌)
	private int recyclePai;
	// 是否看牌 1 弃牌 2
    private int lookedPai;
    
    // 筹码
    private int chip;
    
    //1已看牌0未看牌
    private int look;
    
    //1已比牌0未比牌
    private int compete;
    
    private boolean isWatch;//是否是旁观者
    
    /**
	 * 焖鸡机器人数值
	 */
	private ZTMenjiAI ztMenjiAI;
	
	/**
	 * 本局机器人是否拿到最好牌
	 */
	private boolean isGoodPai;
    
    
	public int getCompete() {
		return compete;
	}
	public void setCompete(int compete) {
		this.compete = compete;
	}
	public int getLook() {
		return look;
	}
	public void setLook(int look) {
		this.look = look;
	}
	public GameRole getRole() {
		return role;
	}

	public void setRole(GameRole role) {
		this.role = role;
	}

	public List<Integer> getPai() {
		if(pai.size()>3){
			int dd = 8;
		}
		return pai;
	}

	public void setPai(List<Integer> pai) {
		this.pai = pai;
	}

	public int[][] getShowPai() {
		return showPai;
	}

	public void setShowPai(int[][] showPai) {
		this.showPai = showPai;
	}

	public int getRecyclePai() {
		return recyclePai;
	}

	public void setRecyclePai(int recyclePai) {
		this.recyclePai = recyclePai;
	}

	public int getLookedPai() {
		return lookedPai;
	}

	public void setLookedPai(int lookedPai) {
		this.lookedPai = lookedPai;
	}

	public int getChip() {
		return chip;
	}

	public void setChip(int chip) {
		this.chip = chip;
	}

	public ZTMenjiAI getZtMenjiAI() {
		return ztMenjiAI;
	}

	public void setZtMenjiAI(ZTMenjiAI ztMenjiAI) {
		this.ztMenjiAI = ztMenjiAI;
	}

	public boolean isGoodPai() {
		return isGoodPai;
	}

	public void setGoodPai(boolean isGoodPai) {
		this.isGoodPai = isGoodPai;
	}

	public boolean isWatch() {
		return isWatch;
	}

	public void setWatch(boolean isWatch) {
		this.isWatch = isWatch;
	}


	
}
