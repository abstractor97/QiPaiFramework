package com.yaowan.model.struct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.yaowan.framework.util.MathUtil;
import com.yaowan.server.game.model.data.entity.Role;


/**
 * @author zane
 *
 */
public  class GameRole {
	
	public GameRole(Role role,long roomId){
		this.role = role;
		this.roomId = roomId;
		this.status = 1;
		this.createTime = System.currentTimeMillis();
		for(Map.Entry<Integer, List<Integer>> entry:role.getAvatarMap().entrySet()){
			if(entry.getValue().get(1)==1){
				this.avatarId = entry.getKey();
			}
		}
		
	}
	/**
	 * 
	 */
	private long roomId;
	
	/**
	 * 初始角色
	 */
	private Role role;
	
	/**
	 * 座位
	 */
	private int seat;
	
	/**
	 * 角色模型
	 */
	private int avatarId;
	
	/**
	 * 是否机器人
	 */
	private boolean isRobot;
	
	/**
	 * 是否自动
	 */
	private boolean isAuto;
	
	/**
	 * 是否作弊
	 */
	private boolean isCheat;
	
	/**
	 * 表达欲
	 */
	private int speak;
	
	/**
	 * AI打的局数
	 */
	private int AICount;
	
	
	/**
	 * 0-未准备
	 * 
	PS_SEAT	= 1;// 入座
	
	PS_PREPARE = 2;// 准备
	
	PS_FLASH = 3;// 动画完成
	PS_PLAY = 4;//开局
	PS_EXIT = 5;// 退出
	
	
	PS_WATCH = 6;// 旁观
	 */
	private int status;
	
	//专门针对ai信息的显示（每个游戏中这个值需要改变）
	//砖石
	private int diamond;
	//本周胜场
	private int winWeek;
	//本周总场
	private int countWeek;
	//生涯胜场
	private int winTotal;
	//生涯总场
	private int countTotal;
	//专门针对ai信息的显示
	
	//生涯总场
	private long createTime;
	/**
	 * 牌
	 */
	private List<Integer> pai = new ArrayList<Integer>();
	
	
	
	public int getDiamond() {
		if (isRobot == true && diamond == 0) {
			diamond = MathUtil.randomNumber(0, 5000);
		}
		return diamond;
	}
	
	public void setRobotWin(){
		this.winWeek = this.winWeek + 1;
		this.countWeek = this.countWeek + 1;
		this.winTotal = this.winTotal + 1;
		this.countTotal = this.countTotal + 1;
	}
	
	public void setRobotLost(){
		this.countWeek = this.countWeek + 1;
		this.countTotal = this.countTotal + 1;
	}

	public void setDiamond(int diamond) {
		this.diamond = diamond;
	}
	
	/**
	 * 输赢的钱
	 */
	private int gain;


	public int getWinWeek() {
		return winWeek;
	}

	public void setWinWeek(int winWeek) {
		this.winWeek = winWeek;
	}

	public int getCountWeek() {
		return countWeek;
	}

	public void setCountWeek(int countWeek) {
		this.countWeek = countWeek;
	}

	public int getWinTotal() {
		return winTotal;
	}

	public void setWinTotal(int winTotal) {
		this.winTotal = winTotal;
	}

	public int getCountTotal() {
		return countTotal;
	}

	public void setCountTotal(int countTotal) {
		this.countTotal = countTotal;
	}

	
	
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public List<Integer> getPai() {
		return pai;
	}

	public void setPai(List<Integer> pai) {
		this.pai = pai;
	}

	

	public boolean isRobot() {
		return isRobot;
	}

	public void setRobot(boolean isRobot) {
		this.isRobot = isRobot;
	}

	public boolean isAuto() {
		return isAuto;
	}

	public void setAuto(boolean isAuto) {
		this.isAuto = isAuto;
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getRoomId() {
		return roomId;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}

	public int getAvatarId() {
		return avatarId;
	}

	public void setAvatarId(int avatarId) {
		this.avatarId = avatarId;
	}

	public int getGain() {
		return gain;
	}

	public void setGain(int gain) {
		this.gain = gain;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public int getSpeak() {
		return speak;
	}

	public void setSpeak(int speak) {
		this.speak = speak;
	}

	public int getAICount() {
		return AICount;
	}

	public void setAICount(int aICount) {
		AICount = aICount;
	}
	
	public boolean isCheat() {
		return isCheat;
	}

	public void setCheat(boolean isCheat) {
		this.isCheat = isCheat;
	}
	
}
