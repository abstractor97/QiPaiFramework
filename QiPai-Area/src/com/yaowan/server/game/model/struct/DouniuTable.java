package com.yaowan.server.game.model.struct;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.model.struct.Game;
import com.yaowan.protobuf.game.GDouniu.GDouniuPai;
import com.yaowan.protobuf.game.GDouniu.GDouniuRecord;
import com.yaowan.server.game.rule.DouniuRule;

/**
 * 斗牛
 *
 * @author zane
 *
 */
public class DouniuTable implements ISingleData {

	public DouniuTable() {

	}

	public DouniuTable(Game game) {
		this.game = game;
		for (int i = 1; i <= 4; i++) {
			DouniuXian xian = new DouniuXian(i);
			xians.add(xian);
		}
	}
	public void reset(){	
		nextSeat = 0;
		queueWaitType = 0;
		coolDownTime = -1;
		xianTotalChip = 0;
		this.game.setStartTime(System.currentTimeMillis());
		for(DouniuXian xian:xians){
			xian.getChips().clear();
			xian.getPai().clear();
			xian.setTotalGold(0);
		}
		getOwnerPai().clear();
		for (Map.Entry<Long,DouniuRole> entry : getMembers().entrySet()) {
			DouniuRole member =entry.getValue();
			member.getChips().clear();
			member.setWinGold(0);
			member.setWinPower(0);
		}
	}


	private Game game;
	
	private Map<Long,DouniuRole> members = new ConcurrentHashMap<>(); 	// 房间内的所有玩家

	private List<Long> fightOwner = new CopyOnWriteArrayList<>();		// 庄家
	
	private List<Long> waitOwner = new CopyOnWriteArrayList<>();;		// 排队等待的庄家
	
	private List<Long> players = new CopyOnWriteArrayList<>();			// 已选择加入游戏的玩家(不包括当王)
	
	private GDouniuPai.Builder ownerPai = GDouniuPai.newBuilder();
	
	private int ownerHp = 0; 
	// 闲家情况
	private List<DouniuXian> xians = new ArrayList<>();

	// 胜负记录
	private List<GDouniuRecord> records = new LinkedList<>();
	
	// 闲家下注和
	private int xianTotalChip = 0;

	
	private int nextSeat;//下个出牌玩家座位

	// 队列等待类型
	private int queueWaitType = 0;//
	// 出牌倒计时
	private long coolDownTime = -1;
	
	// 上一次没有人玩的时间
	private long lastNoMemberTime = 0;

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

	public List<Long> getFightOwner() {
		return fightOwner;
	}

	public void setFightOwner(List<Long> fightOwner) {
		this.fightOwner = fightOwner;
	}

	public List<Long> getWaitOwner() {
		return waitOwner;
	}

	public void setWaitOwner(List<Long> waitOwner) {
		this.waitOwner = waitOwner;
	}

	public GDouniuPai.Builder getOwnerPai() {
		return ownerPai;
	}

	public void setOwnerPai(GDouniuPai.Builder ownerPai) {
		this.ownerPai = ownerPai;
	}
	
	public void faPai(List<Integer> pais) {
		for (int card:pais) {
			getOwnerPai().addPaiValue(DouniuRule.numToPaiValue(card));
			getOwnerPai().addPaiColor(DouniuRule.numToPaiColor(card));
		}
		getOwnerPai().setPaiType(DouniuRule.getCardType(getOwnerPai()));
	}

	public int getOwnerHp() {
		return ownerHp;
	}

	public void setOwnerHp(int ownerHp) {
		this.ownerHp = ownerHp;
	}

	public List<DouniuXian> getXians() {
		return xians;
	}

	public void setXians(List<DouniuXian> xians) {
		this.xians = xians;
	}

	public List<GDouniuRecord> getRecords() {
		return records;
	}

	public void setRecords(List<GDouniuRecord> records) {
		this.records = records;
	}

	public int getNextSeat() {
		return nextSeat;
	}

	public void setNextSeat(int nextSeat) {
		this.nextSeat = nextSeat;
	}

	public int getQueueWaitType() {
		return queueWaitType;
	}

	public void setQueueWaitType(int queueWaitType) {
		this.queueWaitType = queueWaitType;
	}

	public long getCoolDownTime() {
		return coolDownTime;
	}

	public void setCoolDownTime(long coolDownTime) {
		this.coolDownTime = coolDownTime;
	}
	
	public int getXianTotalChip() {
		return xianTotalChip;
	}

	public void addXianTotalChip(int addXianChip) {
		this.xianTotalChip += addXianChip;
	}

	public Map<Long,DouniuRole> getMembers() {
		return members;
	}

	public void setMembers(Map<Long,DouniuRole> members) {
		this.members = members;
	}

	public List<Long> getPlayers() {
		return players;
	}

	public void setPlayers(List<Long> players) {
		this.players = players;
	} 


	public long getLastNoMemberTime() {
		return lastNoMemberTime;
	}

	public void setLastNoMemberTime(long lastNoMemberTime) {
		this.lastNoMemberTime = lastNoMemberTime;
	} 
}
