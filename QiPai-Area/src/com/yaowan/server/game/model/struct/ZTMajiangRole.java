package com.yaowan.server.game.model.struct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yaowan.model.struct.GameRole;
import com.yaowan.protobuf.game.GBaseMahJong.OptionsType;
import com.yaowan.protobuf.game.GGame.PlayerState;

/**
 * @author zane
 *
 */
public class ZTMajiangRole {

	public ZTMajiangRole(GameRole role) {
		this.role = role;
	}

	public void reset (){
		setCurrentPower(1);
		setPai(new ArrayList<Integer>());
		showPai.clear();
		recyclePai.clear();
		getRole().setStatus(PlayerState.PS_SEAT_VALUE);
		huType = 0;
		timeOutNum=0;
		queType = 0;
	}

	/**
	 * 初始角色
	 */
	private GameRole role = null;

	/**
	 * 牌
	 */
	private List<Integer> pai = new ArrayList<Integer>();
	
	// 弃牌堆(倒计时结束则丢弃这张牌)
	private List<Integer> recyclePai = new ArrayList<Integer>();

	// 摆出来的牌
	private Map<Integer,List<Integer>> showPai = new HashMap<Integer, List<Integer>>();
	

	// 个人倍数
	private int currentPower = 0;
	
	// 抢碰操作
	private OptionsType optionsType = null;
	
	// 胡的类型
	private int huType = 0;
	
	// 缺一门的类型
	private int queType = 0;
	
	//麻将玩家超时次数
	private int timeOutNum=0;	
	
	// 本轮玩家能胡没胡的番数, 有0番的牌型，这里要用-1
	private int huFan = -1;
	
	// 弃牌标志，镇雄麻将不能 “先弃后要”
	// 碰：如果有人打出了可碰的牌但是没碰，如果在没有碰\杠其他牌或者摸牌的情况下，接着下一家再有人打出这张牌，不能碰；
	// 但是过了你自己，其他玩家再有人打出这张牌就可以碰。
	private int lastQiPai = 0;

	public int getTimeOutNum() {
		return timeOutNum;
	}

	public void setTimeOutNum(int timeOutNum) {
		this.timeOutNum = timeOutNum;
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

	public Map<Integer,List<Integer>> getShowPai() {
		return showPai;
	}

	public void setShowPai(Map<Integer,List<Integer>> showPai) {
		this.showPai = showPai;
	}

	public OptionsType getOptionsType() {
		return optionsType;
	}

	public void setOptionsType(OptionsType optionsType) {
		this.optionsType = optionsType;
	}

	public int getHuType() {
		return huType;
	}

	public void setHuType(int huType) {
		this.huType = huType;
	}

	public int getQueType() {
		return queType;
	}

	public void setQueType(int queType) {
		this.queType = queType;
	}

	public Integer getHuFan() {
		return huFan;
	}

	public void setHuFan(Integer huFan) {
		this.huFan = huFan;
	}

	public int getLastQiPai() {
		return lastQiPai;
	}

	public void setLastQiPai(int lastQiPai) {
		this.lastQiPai = lastQiPai;
	}
}
