/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.model.struct;

import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.protobuf.game.GBaseMahJong;
import com.yaowan.protobuf.game.GMenJi.GMenJiTable;

/**
 * 昭通焖鸡牌局
 *
 * @author zane
 *
 */
public class ZTMenjiTable implements ISingleData {

	public ZTMenjiTable() {

	}

	public ZTMenjiTable(Game game) {
		this.game = game;
	}
	public void reset(){	
		if(System.currentTimeMillis()>game.getStartTime()){
			betChips = 1;
			round=1;
			lastPlaySeat = 0;
			betsNum = 0;
			allBetsNum = 0;
			param = 0;
			waiter.clear();
			nextSeat = 0;
			queueWaitType = 0;
			coolDownTime = -1;
			this.game.setStartTime(System.currentTimeMillis());
			for(ZTMenjiRole m:members){
				if(m == null){
					continue;
				}
				m.reset();
			}
			getMenjiBillList().clear();
		}
		
	}

	// 牌的类型
	// 一万-九万 11 - 19
	// 一筒-九筒 21 - 29
	// 一索-九索 31 - 39
	// 座位号从1开始按照逆时针递增, 1是东的方向
	private Game game;

	private int owner;// 庄家

	private int laiZiNum = 0;// 赖子牌
	// 牌局玩家
	private List<ZTMenjiRole> members = new ArrayList<>(5);
	// 玩家可进行的操作
	private Map<Long, List<GBaseMahJong.OptionsType>> canOptions = new ConcurrentHashMapV8<>();
	// 本轮牌局玩家已经进行的操作
	private Map<Long, GBaseMahJong.OptionsType> yetOptions = new ConcurrentHashMapV8<>();
	// 牌信息(剩余的牌)
	private List<Integer> pais = new LinkedList<>();
	private int betChips;// 
	private int round=1;// 
	private int lastPlaySeat;// 当前出牌玩家座位
	private int betsNum;	//注数
	private int allBetsNum;//总注数
	private int param;//前后端通传参数
	private int num;//轮数
	private int firstOperation;//第一个操作的座位
	private boolean isOperation;//第一个操作的人是否已经操作过别的操作，没有不能进行看牌
	
	//当前可操作玩家 同时操作玩家 要判断优先级
    private List<Integer> waiter = new ArrayList<Integer>();
	
	private int nextSeat;//下个出牌玩家座位
	
	private volatile boolean inited = false;

	// 队列等待类型
	private int queueWaitType = 0;//
	// 出牌倒计时
	private long coolDownTime = -1;
	
	private List<MenjiBill> menjiBillList = new LinkedList<MenjiBill>(); 
	
	private List<Integer> roundList = new LinkedList<Integer>(); 
	
	//退出的人的rid
	private Map<Long, Integer> exitMap = new HashMap<Long, Integer>();

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	public int getLaiZiNum() {
		return laiZiNum;
	}

	public void setLaiZiNum(int laiZiNum) {
		this.laiZiNum = laiZiNum;
	}

	public List<ZTMenjiRole> getMembers() {
		return members;
	}

	public void setMembers(List<ZTMenjiRole> members) {
		this.members = members;
	}

	public List<Integer> getPais() {
		return pais;
	}

	public void setPais(List<Integer> pais) {
		this.pais = pais;
	}




	public int getLastPlaySeat() {
		return lastPlaySeat;
	}

	public void setLastPlaySeat(int lastPlaySeat) {
		this.lastPlaySeat = lastPlaySeat;
	}



	public long getCoolDownTime() {
		return coolDownTime;
	}

	public void setCoolDownTime(long coolDownTime) {
		this.coolDownTime = coolDownTime;
	}

	public int getQueueWaitType() {
		return queueWaitType;
	}

	public void setQueueWaitType(int queueWaitType) {
		this.queueWaitType = queueWaitType;
	}

	public Map<Long, List<GBaseMahJong.OptionsType>> getCanOptions() {
		return canOptions;
	}

	public void setCanOptions(Map<Long, List<GBaseMahJong.OptionsType>> canOptions) {
		this.canOptions = canOptions;
	}

	public Map<Long, GBaseMahJong.OptionsType> getYetOptions() {
		return yetOptions;
	}

	public void setYetOptions(Map<Long, GBaseMahJong.OptionsType> yetOptions) {
		this.yetOptions = yetOptions;
	}


//	public int getNeedCompleteNum() {
//		return needCompleteNum;
//	}
//
//	public void setNeedCompleteNum(int needCompleteNum) {
//		this.needCompleteNum = needCompleteNum;
//	}
//
//	public int getCompleteNum() {
//		return completeNum;
//	}
//
//	public void setCompleteNum(int completeNum) {
//		this.completeNum = completeNum;
//	}

	/**
	 * 获取正常出牌情况下 对应的出牌座位
	 *
	 * @return
	 */
	public int getNextPlaySeat() {
		List<Integer> list = new ArrayList<Integer>();
    	for(int i = 1;i<=members.size();i++){
    		if(!waiter.contains(i)){
    			list.add(i);
    		}
    	}
		Integer seat = lastPlaySeat;
		int index = list.indexOf(seat);
		if(index==-1){
			index = 0;
		}
		if (index == list.size() - 1) {

			return list.get(0);
		} else {
			if (list.size() == 1) {
				return list.get(0);
			}
			if (list.size() == 0) {
				return 0;
			} else {
				return list.get(index + 1);
			}
		}
	}

	@Override
	public Number getSingleId() {
		return game.getRoomId();
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public int getNextSeat() {
		return nextSeat;
	}

	public void setNextSeat(int nextSeat) {
		this.nextSeat = nextSeat;
	}

	public int getBetChips() {
		return betChips;
	}

	public void setBetChips(int betChips) {
		this.betChips = betChips;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}
	
	public int getBetsNum() {
		return betsNum;
	}

	public void setBetsNum(int betsNum) {
		this.betsNum = betsNum;
	}

	public int getAllBetsNum() {
		return allBetsNum;
	}

	public void setAllBetsNum(int allBetsNum) {
		this.allBetsNum = allBetsNum;
	}

	public int getParam() {
		return param;
	}

	public void setParam(int param) {
		this.param = param;
	}

	public List<Integer> getWaiter() {
		return waiter;
	}

	public void setWaiter(List<Integer> waiter) {
		this.waiter = waiter;
	}
	
	

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public GMenJiTable.Builder serialize() {
		GMenJiTable.Builder tbuilder = GMenJiTable.newBuilder();
		tbuilder.setBetChips(allBetsNum);
		tbuilder.setCurrentChips(betsNum);
		tbuilder.setCurrentSeat(lastPlaySeat);
		tbuilder.setRound(round);
		tbuilder.setWaitTime((int)(coolDownTime/1000));

		return tbuilder;
	}

	public List<MenjiBill> getMenjiBillList() {
		return menjiBillList;
	}

	public void setMenjiBillList(List<MenjiBill> menjiBillList) {
		this.menjiBillList = menjiBillList;
	}

	public List<Integer> getRoundList() {
		return roundList;
	}

	public void setRoundList(List<Integer> roundList) {
		this.roundList = roundList;
	}
	

	public int getFirstOperation() {
		return firstOperation;
	}

	public void setFirstOperation(int firstOperation) {
		this.firstOperation = firstOperation;
	}

	public boolean isOperation() {
		return isOperation;
	}

	public void setOperation(boolean isOperation) {
		this.isOperation = isOperation;
	}

	public boolean isInited() {
		return inited;
	}

	public void setInited(boolean inited) {
		this.inited = inited;
	}

	public Map<Long, Integer> getExitMap() {
		return exitMap;
	}

	public void setExitMap(Map<Long, Integer> exitMap) {
		this.exitMap = exitMap;
	}

	public boolean setRoundLists(ZTMenjiRole member){
		boolean falg = false;
		for (int i = 0; i < roundList.size(); i++) {
			if(roundList.get(i) == member.getRole().getSeat()){
				roundList.remove(i);
				break;
			}
		}
		if(roundList.isEmpty() || roundList == null){
			round = getRound() + 1;
			falg = true;
			for(int i = 1;i<=members.size();i++){
	    		if(!waiter.contains(i)){
	    			roundList.add(i);
	    		}
	    	}
		}
		return falg;
	}
	

}
