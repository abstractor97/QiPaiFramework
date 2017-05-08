package com.yaowan.server.game.model.data.entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Table;
import com.yaowan.framework.database.annotation.Transient;

@Table(name = "friend_room", comment = "好友房表")
public class FriendRoom implements ISingleData {
	
	@Column(comment = "房间Id")
	private long id;//房间Id
	
	@Column(comment = "房主Id")
	private long owner;//房主
	
	@Column(comment = "游戏类型")
	private int gameType;//游戏类型
	
	@Column(comment = "底注")
	private int baseChip;//底注
	
	@Transient
	private long startTime;//开始时间

	@Column(comment = "总局数")
	private int allRound;//总局数
	
	@Column(comment = "创建时间")
	private int createTime;//创建时间
	
	@Column(comment = "账号")
	private String openId; // 账号
	
	@Transient
	private boolean ownerClear;//房主是否同意解散房间
	
	@Column(comment = "最高番数")
	private int highestPower;//最高番数
	
	@Column(comment = "初始积分")
	private int initScore;//初始积分
	
	@Column(comment = "房主是否退出，0否1是")
	private byte ownerExit;//房主是否退出
	
	@Column(comment = "是否开始，0否1是")
	private byte isStart;
	
	@Column(comment = "当前局数")
	private int nowRound;//当前局数
	
	@Column(comment = "需要的房卡或钻石数量")
	private int cardNum;//需要的房卡或钻石数量
	
	@Column(comment = "房卡或钻石Id")
	private int cardId;//房卡或钻石Id
	
	@Column(comment = "付费类型")
	private int payType;//房卡类型 1包房卡2多人付费
	
	@Transient
	private long voteStartTime; //投票开始时间
	
	@Transient
	private int roundCreateTime; //每局的开始时间
	
	@Column(comment = "房间类型")
	private int roomType;//房间类型
	
	@Column(comment = "玩家对应积分")
	private String spriteToString;//玩家对应积分
	
	@Transient
	private List<Long> prepareList = new ArrayList<Long>();
	
	/**
	 * 离线用户
	 */
	@Transient
	private List<Long> loginOutList = new ArrayList<Long>();

	/**
	 * 玩家是否同意解散房间 0未选择，1同意2不同意
	 */
	@Transient
	private final LinkedHashMap<Long, Integer> agreeMap = new LinkedHashMap<Long, Integer>();
	
	/**
	 * 玩家对应积分
	 */
	@Transient
	private LinkedHashMap<Long, Integer> spriteMap = new LinkedHashMap<Long, Integer>();
	
	/**
	 * 每一局每个玩家输赢的积分
	 */
	@Transient
	private List<Map<String,Integer>> playerScoreList = new ArrayList<Map<String,Integer>>();
	
	/**
	 * 每次开局对应的时间
	 */
	@Transient
	private List<Integer> roundTime = new ArrayList<Integer>();

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

	public byte isOwnerExit() {
		return ownerExit;
	}

	public void setOwnerExit(byte ownerExit) {
		this.ownerExit = ownerExit;
	}

	public byte isStart() {
		return isStart;
	}

	public void setStart(byte isStart) {
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

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getSpriteToString() {
		return spriteToString;
	}

	public void setSpriteToString(String spriteToString) {
		this.spriteToString = spriteToString;
	}

	public int getRoundCreateTime() {
		return roundCreateTime;
	}

	public void setRoundCreateTime(int roundCreateTime) {
		this.roundCreateTime = roundCreateTime;
	}

	public List<Map<String, Integer>> getPlayerScoreList() {
		return playerScoreList;
	}

	public void setPlayerScoreList(List<Map<String, Integer>> playerScoreList) {
		this.playerScoreList = playerScoreList;
	}

	public List<Integer> getRoundTime() {
		return roundTime;
	}

	public void setRoundTime(List<Integer> roundTime) {
		this.roundTime = roundTime;
	}

	

}
