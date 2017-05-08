package com.yaowan.server.game.model.struct;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.yaowan.framework.core.thread.ISingleData;

public class FriendRoom implements ISingleData {
	
	private long id;//房间Id
	
	private int gameType;//游戏类型
	
	private int baseChip;//底注
	
	private long startTime;//开始时间

	private int allRound;//总局数
	
	private long owner;//房主
	
	private boolean ownerClear;//房主是否同意解散房间
	
	private int highestPower;//最高番数
	
	private int initScore;//初始积分
	
	private boolean ownerExit;//房主是否退出
	
	private boolean isStart;
	
	private int nowRound;//当前局数
	
	private int cardNum;//需要的房卡数量
	
	private int cardId;//房卡Id
	
	private int payType;//房卡类型 1包房卡2多人付费
	
	private long voteStartTime; //投票开始时间
	
	private int roomType;//房间类型
	
	private int createTime;//创建时间
	
	private List<Long> prepareList = new ArrayList<Long>();
	
	/**
	 * 离线用户
	 */
	private List<Long> loginOutList = new ArrayList<Long>();

	/**
	 * 玩家是否同意解散房间 0未选择，1同意2不同意
	 */
	private final LinkedHashMap<Long, Integer> agreeMap = new LinkedHashMap<Long, Integer>();
	
	/**
	 * 玩家对应积分
	 */
	private LinkedHashMap<Long, Integer> spriteMap = new LinkedHashMap<Long, Integer>();

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getGameType() {
		return gameType;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public int getBaseChip() {
		return baseChip;
	}

	public void setBaseChip(int baseChip) {
		this.baseChip = baseChip;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	public LinkedHashMap<Long, Integer> getSpriteMap() {
		return spriteMap;
	}

	public void setSpriteMap(LinkedHashMap<Long, Integer> spriteMap) {
		this.spriteMap = spriteMap;
	}

	public long getOwner() {
		return owner;
	}

	public void setOwner(long owner) {
		this.owner = owner;
	}

	public boolean isOwnerClear() {
		return ownerClear;
	}

	public void setOwnerClear(boolean ownerClear) {
		this.ownerClear = ownerClear;
	}

	public LinkedHashMap<Long, Integer> getAgreeMap() {
		return agreeMap;
	}
	
	

	public int getHighestPower() {
		return highestPower;
	}

	public void setHighestPower(int highestPower) {
		this.highestPower = highestPower;
	}

	@Override
	public Number getSingleId() {
		// TODO Auto-generated method stub
		 return id;
	}

	public int getInitScore() {
		return initScore;
	}

	public void setInitScore(int initScore) {
		this.initScore = initScore;
	}

	public boolean isOwnerExit() {
		return ownerExit;
	}

	public void setOwnerExit(boolean ownerExit) {
		this.ownerExit = ownerExit;
	}

	public boolean isStart() {
		return isStart;
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}

	public List<Long> getPrepareList() {
		return prepareList;
	}

	public void setPrepareList(List<Long> prepareList) {
		this.prepareList = prepareList;
	}

	public int getAllRound() {
		return allRound;
	}

	public void setAllRound(int allRound) {
		this.allRound = allRound;
	}

	public int getNowRound() {
		return nowRound;
	}

	public void setNowRound(int nowRound) {
		this.nowRound = nowRound;
	}

	public int getCardNum() {
		return cardNum;
	}

	public void setCardNum(int cardNum) {
		this.cardNum = cardNum;
	}

	public int getCardId() {
		return cardId;
	}

	public void setCardId(int cardId) {
		this.cardId = cardId;
	}

	public int getPayType() {
		return payType;
	}

	public void setPayType(int payType) {
		this.payType = payType;
	}

	public List<Long> getLoginOutList() {
		return loginOutList;
	}

	public void setLoginOutList(List<Long> loginOutList) {
		this.loginOutList = loginOutList;
	}

	public long getVoteStartTime() {
		return voteStartTime;
	}

	public void setVoteStartTime(long voteStartTime) {
		this.voteStartTime = voteStartTime;
	}

	public int getRoomType() {
		return roomType;
	}

	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}

	public int getCreateTime() {
		return createTime;
	}

	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}


	

}
